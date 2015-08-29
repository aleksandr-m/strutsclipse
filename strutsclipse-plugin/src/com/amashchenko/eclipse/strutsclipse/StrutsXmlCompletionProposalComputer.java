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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.java.JavaClassCompletion;

public class StrutsXmlCompletionProposalComputer implements
		ICompletionProposalComputer {

	private static final List<String> DISPATCHER_EXTENSIONS = Arrays
			.asList(new String[] { "jsp", "html", "htm" });
	private static final List<String> FREEMARKER_EXTENSIONS = Arrays
			.asList(new String[] { "ftl" });

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		final TagRegion tagRegion = StrutsXmlParser.getTagRegion(
				context.getDocument(), context.getInvocationOffset());

		String[][] proposals = null;
		IRegion proposalRegion = null;
		String elementValuePrefix = null;
		String elementValue = null;
		String multiValueSeparator = null;

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final String elementName = tagRegion.getCurrentElement().getName();
			proposalRegion = tagRegion.getCurrentElement().getValueRegion();
			elementValuePrefix = tagRegion.getCurrentElementValuePrefix();
			elementValue = tagRegion.getCurrentElement().getValue();

			if (StrutsXmlConstants.PACKAGE_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (StrutsXmlConstants.EXTENDS_ATTR
						.equalsIgnoreCase(elementName)) {
					proposals = StrutsXmlConstants.DEFAULT_PACKAGE_NAMES;
					// extends attribute can have multiple values separated by ,
					multiValueSeparator = ",";
				}
			} else if (StrutsXmlConstants.ACTION_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (StrutsXmlConstants.NAME_ATTR.equalsIgnoreCase(elementName)
						|| StrutsXmlConstants.METHOD_ATTR
								.equalsIgnoreCase(elementName)) {
					final ElementRegion classAttr = tagRegion.getAttrs().get(
							StrutsXmlConstants.CLASS_ATTR);

					if (classAttr == null) {
						proposals = StrutsXmlConstants.DEFAULT_METHODS;
					} else {
						// return proposals
						return JavaClassCompletion.getActionMethodProposals(
								elementValuePrefix, classAttr.getValue(),
								context.getDocument(), proposalRegion);
					}
				} else if (StrutsXmlConstants.CLASS_ATTR
						.equalsIgnoreCase(elementName)) {
					// return proposals
					return JavaClassCompletion.getSimpleJavaProposals(
							elementValuePrefix, context.getDocument(),
							proposalRegion);
				}
			} else if (StrutsXmlConstants.RESULT_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (StrutsXmlConstants.NAME_ATTR.equalsIgnoreCase(elementName)) {
					proposals = StrutsXmlConstants.DEFAULT_RESULT_NAMES;
				} else if (StrutsXmlConstants.TYPE_ATTR
						.equalsIgnoreCase(elementName)) {
					proposals = StrutsXmlConstants.DEFAULT_RESULT_TYPES;
				} else if (elementName == null) { // result tag body
					final ElementRegion typeAttr = tagRegion.getAttrs().get(
							StrutsXmlConstants.TYPE_ATTR);
					proposals = computeResultBodyProposals(
							context.getDocument(), typeAttr == null ? null
									: typeAttr.getValue());
				}
			} else if (StrutsXmlConstants.PARAM_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (elementName == null) { // param tag body
					final ElementRegion nameAttr = tagRegion.getAttrs().get(
							StrutsXmlConstants.NAME_ATTR);
					if (nameAttr != null
							&& StrutsXmlConstants.LOCATION_PARAM
									.equals(nameAttr.getValue())) {
						final TagRegion parentResultTagRegion = StrutsXmlParser
								.getParentTagRegion(context.getDocument(),
										context.getInvocationOffset(),
										StrutsXmlConstants.RESULT_TAG);
						if (parentResultTagRegion != null) {
							final ElementRegion typeAttr = parentResultTagRegion
									.getAttrs().get(
											StrutsXmlConstants.TYPE_ATTR);
							proposals = computeResultBodyProposals(
									context.getDocument(),
									typeAttr == null ? null : typeAttr
											.getValue());
						}
					}
				}
			}
		}

		return createAttrCompletionProposals(proposals, elementValuePrefix,
				proposalRegion, multiValueSeparator, elementValue);
	}

	private String[][] computeResultBodyProposals(final IDocument document,
			final String typeAttrValue) {
		Set<String> set = null;
		// assume that default is dispatcher for now, TODO improve
		// that
		if (typeAttrValue == null
				|| StrutsXmlConstants.DISPATCHER_RESULT.equals(typeAttrValue)) {
			set = findFilesPaths(document, DISPATCHER_EXTENSIONS);
		} else if (StrutsXmlConstants.TILES_RESULT.equals(typeAttrValue)) {
			set = findTilesDefinitionNames(document);
		} else if (StrutsXmlConstants.FREEMARKER_RESULT.equals(typeAttrValue)) {
			set = findFilesPaths(document, FREEMARKER_EXTENSIONS);
		}

		String[][] proposals = null;
		if (set != null && !set.isEmpty()) {
			proposals = new String[set.size()][2];
			int indx = 0;
			for (String p : set) {
				proposals[indx++][0] = p;
			}
		}
		return proposals;
	}

	private List<ICompletionProposal> createAttrCompletionProposals(
			String[][] proposalsData, String prefix, IRegion region,
			String valueSeparator, String attrvalue) {
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		if (proposalsData != null && region != null) {
			int replacementOffset = region.getOffset();
			int replacementLength = region.getLength();

			boolean multivalue = valueSeparator != null
					&& attrvalue.contains(valueSeparator);

			List<String> excludes = new ArrayList<String>();

			if (multivalue) {
				int startSeprIndx = prefix.lastIndexOf(valueSeparator) + 1;

				// spaces between valueSeparator and current value prefix
				// (one,_t|wo -> 1; one,_|two -> 1; one,__t|wo -> 2)
				int spacesCount = 0;

				String currentValue = "";

				// first value in attrvalue
				if (startSeprIndx <= 0) {
					currentValue = attrvalue.substring(0,
							attrvalue.indexOf(valueSeparator));
				} else {
					prefix = prefix.substring(startSeprIndx);
					spacesCount = prefix.length();
					prefix = prefix.trim();
					spacesCount = spacesCount - prefix.length();

					int endSeprIndx = attrvalue.indexOf(valueSeparator,
							startSeprIndx);
					if (endSeprIndx <= 0) {
						// last value in attrvalue
						currentValue = attrvalue.substring(startSeprIndx);
					} else {
						// somewhere in the middle of attrvalue
						currentValue = attrvalue.substring(startSeprIndx,
								endSeprIndx);
					}
				}

				currentValue = currentValue.trim();

				if (spacesCount < 0) {
					spacesCount = 0;
				}

				replacementOffset = replacementOffset + startSeprIndx
						+ spacesCount;
				replacementLength = currentValue.length();

				// exclude already defined values except current value
				String[] valArr = attrvalue.split(valueSeparator);
				for (String val : valArr) {
					if (!currentValue.equalsIgnoreCase(val.trim())) {
						excludes.add(val.trim());
					}
				}
			}

			for (String[] proposal : proposalsData) {
				if (proposal[0].toLowerCase().startsWith(prefix.toLowerCase())
						&& !excludes.contains(proposal[0])) {
					list.add(new CompletionProposal(proposal[0],
							replacementOffset, replacementLength, proposal[0]
									.length(), null, null, null, proposal[1]));
				}
			}
		}
		return list;
	}

	private Set<String> findFilesPaths(final IDocument currentDocument,
			final List<String> extensions) {
		final Set<String> paths = new HashSet<String>();
		try {
			IProject project = ProjectUtil.getCurrentProject(currentDocument);
			if (project != null && project.exists()) {
				IVirtualComponent rootComponent = ComponentCore
						.createComponent(project);
				final IVirtualFolder rootFolder = rootComponent.getRootFolder();

				rootFolder.getUnderlyingResource().accept(
						new IResourceVisitor() {
							@Override
							public boolean visit(IResource resource)
									throws CoreException {
								if (resource.isAccessible()
										&& resource.getType() == IResource.FILE
										&& extensions.contains(resource
												.getFileExtension()
												.toLowerCase())) {
									IPath path = resource
											.getProjectRelativePath()
											.makeRelativeTo(
													rootFolder
															.getProjectRelativePath())
											.makeAbsolute();

									paths.add(path.toString());
								}
								return true;
							}
						});
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return paths;
	}

	private Set<String> findTilesDefinitionNames(final IDocument currentDocument) {
		final Set<String> names = new HashSet<String>();
		try {
			final IDocumentProvider provider = new TextFileDocumentProvider();
			IProject project = ProjectUtil.getCurrentProject(currentDocument);
			if (project != null && project.exists()) {
				project.accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource)
							throws CoreException {
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
							IDocument document = provider.getDocument(resource);
							provider.disconnect(resource);

							names.addAll(TilesXmlParser
									.getDefinitionNames(document));
						}
						return true;
					}
				});
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return names;
	}

	@Override
	public List<IContextInformation> computeContextInformation(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionStarted() {
	}

	@Override
	public void sessionEnded() {
	}
}
