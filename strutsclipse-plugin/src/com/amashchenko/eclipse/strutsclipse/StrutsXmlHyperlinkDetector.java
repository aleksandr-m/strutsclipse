/*
 * Copyright 2015 Aleksandr Mashchenko.
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
package com.amashchenko.eclipse.strutsclipse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TilesXmlParser;

public class StrutsXmlHyperlinkDetector extends AbstractHyperlinkDetector {
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
			final String elementName = tagRegion.getCurrentElement().getName();
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			if (StrutsXmlConstants.ACTION_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				final ElementRegion classAttr = tagRegion.getAttrs().get(
						StrutsXmlConstants.CLASS_ATTR);
				if (StrutsXmlConstants.METHOD_ATTR
						.equalsIgnoreCase(elementName) && classAttr != null) {
					linksList.add(createClassMethodLink(document, elementValue,
							elementRegion, classAttr.getValue()));
				}
			} else if (StrutsXmlConstants.RESULT_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (elementName == null) { // result tag body
					final ElementRegion typeAttr = tagRegion.getAttrs().get(
							StrutsXmlConstants.TYPE_ATTR);
					linksList.addAll(createResultLocationLinks(document,
							elementValue, elementRegion,
							typeAttr == null ? null : typeAttr.getValue()));
				}
			} else if (StrutsXmlConstants.PARAM_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (elementName == null) { // param tag body
					final ElementRegion nameAttr = tagRegion.getAttrs().get(
							StrutsXmlConstants.NAME_ATTR);
					if (nameAttr != null) {
						final TagRegion parentResultTagRegion = strutsXmlParser
								.getParentTagRegion(document,
										region.getOffset(),
										StrutsXmlConstants.RESULT_TAG);
						if (parentResultTagRegion != null
								&& parentResultTagRegion.getAttrs() != null) {
							final ElementRegion typeAttr = parentResultTagRegion
									.getAttrs().get(
											StrutsXmlConstants.TYPE_ATTR);
							boolean correctTypeAndName = (StrutsXmlConstants.LOCATION_PARAM
									.equals(nameAttr.getValue()) && (typeAttr == null || !StrutsXmlConstants.REDIRECT_ACTION_RESULT
									.equals(typeAttr.getValue())))
									|| (typeAttr != null
											&& StrutsXmlConstants.REDIRECT_ACTION_RESULT
													.equals(typeAttr.getValue()) && StrutsXmlConstants.ACTION_NAME_PARAM
												.equals(nameAttr.getValue()));
							if (correctTypeAndName) {
								linksList.addAll(createResultLocationLinks(
										document,
										elementValue,
										elementRegion,
										typeAttr == null ? null : typeAttr
												.getValue()));
							}
						}
					}
				}
			}
		}

		IHyperlink[] links = null;
		if (linksList != null && !linksList.isEmpty()) {
			// remove null-s
			Iterator<IHyperlink> itr = linksList.iterator();
			while (itr.hasNext()) {
				if (itr.next() == null) {
					itr.remove();
				}
			}

			if (!linksList.isEmpty()) {
				links = linksList.toArray(new IHyperlink[linksList.size()]);
			}
		}

		return links;
	}

	private List<IHyperlink> createResultLocationLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion, final String typeAttrValue) {
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
			// TODO handle namespace param
			TagRegion packageTagRegion = strutsXmlParser.getParentTagRegion(
					document, elementRegion.getOffset(),
					StrutsXmlConstants.PACKAGE_TAG);
			if (packageTagRegion != null && packageTagRegion.getAttrs() != null) {
				ElementRegion namespaceAttr = packageTagRegion.getAttrs().get(
						StrutsXmlConstants.NAMESPACE_ATTR);
				IRegion region = strutsXmlParser.getActionRegion(document,
						namespaceAttr == null ? "" : namespaceAttr.getValue(),
						elementValue);
				if (region != null) {
					ITextFileBuffer textFileBuffer = FileBuffers
							.getTextFileBufferManager().getTextFileBuffer(
									document);
					if (textFileBuffer != null) {
						IFile file = ResourcesPlugin.getWorkspace().getRoot()
								.getFile(textFileBuffer.getLocation());
						if (file.exists()) {
							links.add(new FileHyperlink(elementRegion, file,
									region));
						}
					}
				}
			}
		} else if (StrutsXmlConstants.TILES_RESULT.equals(typeAttrValue)) {
			try {
				final IDocumentProvider provider = new TextFileDocumentProvider();
				final IJavaProject javaProject = ProjectUtil
						.getCurrentJavaProject(document);
				if (javaProject != null && javaProject.exists()) {
					final IProject project = javaProject.getProject();
					final String outputFolder = javaProject.getOutputLocation()
							.makeRelativeTo(project.getFullPath()).segment(0);
					project.accept(new IResourceVisitor() {
						@Override
						public boolean visit(IResource resource)
								throws CoreException {
							// don't visit output folder
							if (resource.getType() == IResource.FOLDER
									&& resource.getProjectRelativePath()
											.segment(0).equals(outputFolder)) {
								return false;
							}
							if (resource.isAccessible()
									&& resource.getType() == IResource.FILE
									&& "xml".equalsIgnoreCase(resource
											.getFileExtension())
									&& resource
											.getName()
											.toLowerCase()
											.contains(
													StrutsXmlConstants.TILES_RESULT)) {
								provider.connect(resource);
								IDocument document = provider
										.getDocument(resource);
								provider.disconnect(resource);

								IRegion region = tilesXmlParser
										.getDefinitionRegion(document,
												elementValue);
								if (region != null) {
									IFile file = project.getFile(resource
											.getProjectRelativePath());
									if (file.exists()) {
										links.add(new FileHyperlink(
												elementRegion, file, region));
									}
								}
							}
							return true;
						}
					});
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return links;
	}

	private IHyperlink createClassMethodLink(final IDocument document,
			final String elementValue, final IRegion elementRegion,
			final String className) {
		IHyperlink link = null;
		try {
			IJavaProject javaProject = ProjectUtil
					.getCurrentJavaProject(document);
			if (javaProject != null && javaProject.exists()) {
				IType type = javaProject.findType(className);
				if (type != null && type.exists()) {
					IMethod method = type.getMethod(elementValue, null);
					if (method != null && method.exists()) {
						link = new JavaElementHyperlink(elementRegion, method);
					} else {
						// try super classes
						IType[] superClasses = type.newSupertypeHierarchy(null)
								.getAllSuperclasses(type);
						for (IType superType : superClasses) {
							method = superType.getMethod(elementValue, null);
							if (method != null && method.exists()) {
								link = new JavaElementHyperlink(elementRegion,
										method);
								break;
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return link;
	}

	private static class FileHyperlink implements IHyperlink {
		private final IFile fFile;
		private final IRegion fRegion;
		private final IRegion fHighlightRange;

		private FileHyperlink(IRegion region, IFile file) {
			this(region, file, null);
		}

		public FileHyperlink(IRegion region, IFile file, IRegion range) {
			fRegion = region;
			fFile = file;
			fHighlightRange = range;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return fFile == null ? null : fFile.getProjectRelativePath()
					.toString();
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
				IEditorPart editor = IDE.openEditor(page, fFile, true);

				ITextEditor textEditor = null;

				if (editor instanceof MultiPageEditorPart) {
					MultiPageEditorPart part = (MultiPageEditorPart) editor;

					Object editorPage = part.getSelectedPage();
					if (editorPage != null && editorPage instanceof ITextEditor) {
						textEditor = (ITextEditor) editorPage;
					}
				} else if (editor instanceof ITextEditor) {
					textEditor = (ITextEditor) editor;
				}

				// highlight range in editor if possible
				if (fHighlightRange != null && textEditor != null) {
					textEditor.selectAndReveal(fHighlightRange.getOffset(),
							fHighlightRange.getLength());
				}
			} catch (PartInitException e) {
			}
		}
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
}
