/*
 * Copyright 2015-2016 Aleksandr Mashchenko.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amashchenko.eclipse.strutsclipse.strutsxml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

import com.amashchenko.eclipse.strutsclipse.AbstractStrutsHyperlinkDetector;
import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;
import com.amashchenko.eclipse.strutsclipse.ParseUtil;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.tilesxml.TilesXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsXmlHyperlinkDetector extends AbstractStrutsHyperlinkDetector
		implements StrutsXmlLocations {
	private final StrutsXmlParser strutsXmlParser;
	private final TilesXmlParser tilesXmlParser;

	public StrutsXmlHyperlinkDetector() {
		strutsXmlParser = new StrutsXmlParser();
		tilesXmlParser = new TilesXmlParser();
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		List<IHyperlink> linksList = new ArrayList<IHyperlink>();

		final TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case PACKAGE_EXTENDS:
				ElementRegion parsedValue = ParseUtil.parseElementValue(
						elementValue, tagRegion.getCurrentElementValuePrefix(),
						StrutsXmlConstants.MULTI_VALUE_SEPARATOR,
						elementRegion.getOffset());

				// find in current document
				IHyperlink localLink = createPackageExtendsLocalLink(document,
						parsedValue.getValue(), parsedValue.getValueRegion());

				if (localLink == null) {
					// find in jars
					linksList.addAll(createPackageExtendsJarLinks(document,
							parsedValue.getValue(),
							parsedValue.getValueRegion()));
				} else {
					linksList.add(localLink);
				}
				break;
			case DEFAULT_ACTION_REF_NAME:
				// same as for the result location, but with concrete namespace
				TagRegion packageTagRegion = strutsXmlParser
						.getParentTagRegion(document,
								elementRegion.getOffset(),
								StrutsXmlConstants.PACKAGE_TAG);
				if (packageTagRegion != null) {
					String namespace = packageTagRegion.getAttrValue(
							StrutsXmlConstants.NAMESPACE_ATTR, "");
					linksList.addAll(createResultLocationLinks(document,
							elementValue, elementRegion,
							StrutsXmlConstants.REDIRECT_ACTION_RESULT,
							namespace));
				}
				break;
			case ACTION_METHOD:
				final String classAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.CLASS_ATTR, null);
				if (classAttrValue != null) {
					linksList.add(createClassMethodLink(document, elementValue,
							elementRegion, classAttrValue));
				}
				break;
			case RESULT_BODY:
				linksList.addAll(createResultLocationLinks(document,
						elementValue, elementRegion, tagRegion.getAttrValue(
								StrutsXmlConstants.TYPE_ATTR, null), null));
				break;
			case PARAM_BODY:
				final String nameAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.NAME_ATTR, null);
				if (nameAttrValue != null) {
					TagRegion resultTagRegion = strutsXmlParser
							.getResultTagRegion(document, region.getOffset());
					if (resultTagRegion != null) {
						// name is type value, here
						final String typeAttrValue = resultTagRegion.getName();
						boolean correctTypeAndName = (StrutsXmlConstants.LOCATION_PARAM
								.equals(nameAttrValue) && (typeAttrValue == null || !StrutsXmlConstants.REDIRECT_ACTION_RESULT
								.equals(typeAttrValue)))
								|| (typeAttrValue != null
										&& StrutsXmlConstants.REDIRECT_ACTION_RESULT
												.equals(typeAttrValue) && StrutsXmlConstants.ACTION_NAME_PARAM
											.equals(nameAttrValue));
						if (correctTypeAndName) {
							final String namespaceParamValue = resultTagRegion
									.getAttrValue(
											StrutsXmlConstants.NAMESPACE_ATTR,
											null);
							linksList.addAll(createResultLocationLinks(
									document, elementValue, elementRegion,
									typeAttrValue, namespaceParamValue));
						}
					}
				}
				break;
			}
		}

		return linksListToArray(linksList);
	}

	private List<IHyperlink> createResultLocationLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion, final String typeAttrValue,
			final String namespaceParamValue) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		// assume that default is dispatcher for now, TODO improve
		// that
		if (typeAttrValue == null
				|| StrutsXmlConstants.DISPATCHER_RESULT.equals(typeAttrValue)
				|| StrutsXmlConstants.FREEMARKER_RESULT.equals(typeAttrValue)) {
			IProject project = ProjectUtil.getCurrentProject(document);
			if (project != null && project.exists()) {
				IVirtualComponent rootComponent = ComponentCore
						.createComponent(project);
				final IVirtualFolder rootFolder = rootComponent.getRootFolder();
				IPath path = rootFolder.getProjectRelativePath().append(
						elementValue);

				IFile file = project.getFile(path);
				if (file.exists()) {
					links.add(new FileHyperlink(elementRegion, file));
				}
			}
		} else if (StrutsXmlConstants.REDIRECT_ACTION_RESULT
				.equals(typeAttrValue)) {
			Set<String> namespaces = new HashSet<String>();

			// if there is a namespaceParamValue then used it, else get
			// namespace from parent package
			String namespace = namespaceParamValue;
			if (namespace == null) {
				TagRegion packageTagRegion = strutsXmlParser
						.getParentTagRegion(document,
								elementRegion.getOffset(),
								StrutsXmlConstants.PACKAGE_TAG);
				if (packageTagRegion != null) {
					namespace = packageTagRegion.getAttrValue(
							StrutsXmlConstants.NAMESPACE_ATTR, "");
				} else {
					namespace = "";
				}

				// if namespace came NOT from namespaceParamValue then add
				// special namespaces
				namespaces.add("");
				namespaces.add("/");
			}

			namespaces.add(namespace);

			IRegion region = strutsXmlParser.getActionRegion(document,
					namespaces, elementValue);
			if (region != null) {
				ITextFileBuffer textFileBuffer = FileBuffers
						.getTextFileBufferManager().getTextFileBuffer(document);
				if (textFileBuffer != null) {
					IFile file = ResourcesPlugin.getWorkspace().getRoot()
							.getFile(textFileBuffer.getLocation());
					if (file.exists()) {
						links.add(new FileHyperlink(elementRegion, file, region));
					}
				}
			}
		} else if (StrutsXmlConstants.TILES_RESULT.equals(typeAttrValue)) {
			IProject project = ProjectUtil.getCurrentProject(document);
			if (project != null && project.exists()) {
				// find tiles resources
				List<ResourceDocument> resources = ProjectUtil
						.findTilesResources(document);
				for (ResourceDocument rd : resources) {
					IRegion region = tilesXmlParser.getDefinitionRegion(
							rd.getDocument(), elementValue);
					if (region != null) {
						IFile file = project.getFile(rd.getResource()
								.getProjectRelativePath());
						if (file.exists()) {
							links.add(new FileHyperlink(elementRegion, file,
									region));
						}
					}
				}
			}
		}
		return links;
	}

	private IHyperlink createClassMethodLink(final IDocument document,
			final String elementValue, final IRegion elementRegion,
			final String className) {
		IHyperlink link = null;

		IType clazz = ProjectUtil.findClass(document, className);
		if (clazz != null) {
			IMethod method = ProjectUtil.findClassParameterlessMethod(clazz,
					elementValue);
			if (method != null) {
				link = new JavaElementHyperlink(elementRegion, method);
			}
		}

		return link;
	}

	private IHyperlink createPackageExtendsLocalLink(final IDocument document,
			final String elementValue, final IRegion elementRegion) {
		IHyperlink link = null;

		IRegion packageNameRegion = strutsXmlParser.getPackageNameRegion(
				document, elementValue);
		if (packageNameRegion != null) {
			ITextFileBuffer textFileBuffer = FileBuffers
					.getTextFileBufferManager().getTextFileBuffer(document);
			if (textFileBuffer != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(textFileBuffer.getLocation());
				if (file.exists()) {
					link = new FileHyperlink(elementRegion, file,
							packageNameRegion);
				}
			}
		}

		return link;
	}

	private List<IHyperlink> createPackageExtendsJarLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		List<JarEntryStorage> jarStorages = ProjectUtil
				.findJarEntryStrutsResources(document);
		for (JarEntryStorage jarStorage : jarStorages) {
			IRegion nameRegion = strutsXmlParser.getPackageNameRegion(
					jarStorage.toDocument(), elementValue);
			if (nameRegion != null) {
				links.add(new StorageHyperlink(elementRegion, jarStorage,
						nameRegion));
			}
		}

		return links;
	}

	private static class JavaElementHyperlink implements IHyperlink {
		private final IJavaElement fElement;
		private final IRegion fRegion;

		private JavaElementHyperlink(IRegion region, IJavaElement element) {
			fRegion = region;
			fElement = element;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return null;
		}

		@Override
		public String getTypeLabel() {
			return null;
		}

		@Override
		public void open() {
			try {
				JavaUI.openInEditor(fElement);
			} catch (PartInitException e) {
			} catch (JavaModelException e) {
			}
		}
	}

	private static class StorageHyperlink implements IHyperlink {
		private final IStorage fStorage;
		private final IRegion fRegion;
		private final IRegion fHighlightRange;

		private StorageHyperlink(IRegion region, IStorage storage, IRegion range) {
			fRegion = region;
			fStorage = storage;
			fHighlightRange = range;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return null;
		}

		@Override
		public String getTypeLabel() {
			return null;
		}

		@Override
		public void open() {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();

				IEditorDescriptor editorDescriptor = IDE
						.getEditorDescriptor(fStorage.getName());

				IEditorPart editor = page.openEditor(new StorageEditorInput(
						fStorage), editorDescriptor.getId());

				selectAndReveal(editor, fHighlightRange);
			} catch (PartInitException e) {
			}
		}
	}

	private static class StorageEditorInput implements IStorageEditorInput {
		private final IStorage fStorage;

		private StorageEditorInput(IStorage storage) {
			fStorage = storage;
		}

		@Override
		public boolean exists() {
			return fStorage != null;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return fStorage.getName();
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public IStorage getStorage() {
			return fStorage;
		}

		@Override
		public String getToolTipText() {
			return fStorage.getFullPath() != null ? fStorage.getFullPath()
					.toString() : fStorage.getName();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StorageEditorInput) {
				return fStorage.equals(((StorageEditorInput) obj).fStorage);
			}
			return super.equals(obj);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object getAdapter(Class adapter) {
			return null;
		}
	}
}
