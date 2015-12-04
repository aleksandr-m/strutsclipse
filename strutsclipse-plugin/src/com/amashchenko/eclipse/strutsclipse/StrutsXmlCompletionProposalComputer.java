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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
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

import com.amashchenko.eclipse.strutsclipse.java.ActionMethodProposalComparator;
import com.amashchenko.eclipse.strutsclipse.java.JavaClassCompletion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TilesXmlParser;

public class StrutsXmlCompletionProposalComputer implements
		ICompletionProposalComputer, StrutsXmlLocations {
	private static final List<String> DISPATCHER_EXTENSIONS = Arrays
			.asList(new String[] { "jsp", "html", "htm" });
	private static final List<String> FREEMARKER_EXTENSIONS = Arrays
			.asList(new String[] { "ftl" });

	private final StrutsXmlParser strutsXmlParser;
	private final TilesXmlParser tilesXmlParser;

	private final CompletionProposalComparator proposalComparator;
	private final ActionMethodProposalComparator methodProposalComparator;

	public StrutsXmlCompletionProposalComputer() {
		strutsXmlParser = new StrutsXmlParser();
		tilesXmlParser = new TilesXmlParser();
		proposalComparator = new CompletionProposalComparator();
		proposalComparator.setOrderAlphabetically(true);
		methodProposalComparator = new ActionMethodProposalComparator();
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		final TagRegion tagRegion = strutsXmlParser.getTagRegion(
				context.getDocument(), context.getInvocationOffset());

		List<ICompletionProposal> proposals = null;
		String[][] proposalsData = null;

		IRegion proposalRegion = null;
		String elementValuePrefix = null;
		String elementValue = null;
		String multiValueSeparator = null;
		boolean sortProposals = false;

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			proposalRegion = tagRegion.getCurrentElement().getValueRegion();
			elementValuePrefix = tagRegion.getCurrentElementValuePrefix();
			elementValue = tagRegion.getCurrentElement().getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case PACKAGE_EXTENDS:
				proposalsData = computePackageExtendsProposals(
						context.getDocument(), tagRegion.getAttrValue(
								StrutsXmlConstants.NAME_ATTR, null));
				// extends attribute can have multiple values separated by ,
				multiValueSeparator = ",";
				break;
			case CONSTANT_NAME:
				proposalsData = StrutsXmlConstants.DEFAULT_CONSTANTS;
				break;
			case ACTION_NAME:
			case ACTION_METHOD:
				final String classAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.CLASS_ATTR, null);

				if (classAttrValue == null) {
					proposalsData = StrutsXmlConstants.DEFAULT_METHODS;
				} else {
					List<ICompletionProposal> methodProposals = JavaClassCompletion
							.getActionMethodProposals(elementValuePrefix,
									classAttrValue, context.getDocument(),
									proposalRegion);
					// sort
					Collections.sort(methodProposals, methodProposalComparator);

					// real proposals
					proposals = methodProposals;
				}
				break;
			case ACTION_CLASS:
				// real proposals
				proposals = JavaClassCompletion.getSimpleJavaProposals(
						elementValuePrefix, context.getDocument(),
						proposalRegion);
				break;
			case RESULT_NAME:
				proposalsData = StrutsXmlConstants.DEFAULT_RESULT_NAMES;
				break;
			case RESULT_TYPE:
				proposalsData = StrutsXmlConstants.DEFAULT_RESULT_TYPES;
				break;
			case RESULT_BODY:
				proposalsData = computeResultBodyProposals(
						context.getDocument(), context.getInvocationOffset(),
						tagRegion.getAttrValue(StrutsXmlConstants.TYPE_ATTR,
								null), null);
				sortProposals = true;
				break;
			case PARAM_BODY:
				final String nameAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.NAME_ATTR, null);
				if (nameAttrValue != null) {
					TagRegion resultTagRegion = strutsXmlParser
							.getResultTagRegion(context.getDocument(),
									context.getInvocationOffset());
					if (resultTagRegion != null) {
						// name is type value, here
						final String typeAttrValue = resultTagRegion.getName();

						boolean redirectAction = typeAttrValue != null
								&& StrutsXmlConstants.REDIRECT_ACTION_RESULT
										.equals(typeAttrValue);

						// param name="namespace"
						if (redirectAction
								&& StrutsXmlConstants.NAMESPACE_ATTR
										.equals(nameAttrValue)) {
							Set<String> packageNames = strutsXmlParser
									.getPackageNamespaces(context.getDocument());
							packageNames.remove("");
							if (packageNames != null && !packageNames.isEmpty()) {
								proposalsData = new String[packageNames.size()][2];
								int indx = 0;
								for (String p : packageNames) {
									proposalsData[indx++][0] = p;
								}
							}
						} else {
							boolean correctTypeAndName = (StrutsXmlConstants.LOCATION_PARAM
									.equals(nameAttrValue) && !redirectAction)
									|| (redirectAction && StrutsXmlConstants.ACTION_NAME_PARAM
											.equals(nameAttrValue));
							if (correctTypeAndName) {
								final String namespaceParamValue = resultTagRegion
										.getAttrValue(
												StrutsXmlConstants.NAMESPACE_ATTR,
												null);
								proposalsData = computeResultBodyProposals(
										context.getDocument(),
										context.getInvocationOffset(),
										typeAttrValue, namespaceParamValue);
								sortProposals = true;
							}
						}
					}
				}
				break;
			}
		}

		if (proposals == null && proposalsData != null) {
			proposals = createAttrCompletionProposals(proposalsData,
					elementValuePrefix, proposalRegion, multiValueSeparator,
					elementValue, sortProposals);
		}
		if (proposals == null) {
			proposals = new ArrayList<ICompletionProposal>();
		}

		return proposals;
	}

	private String[][] computePackageExtendsProposals(final IDocument document,
			final String currentPackageName) {
		Set<String> packageNames = strutsXmlParser.getPackageNames(document);

		// remove current package name
		if (currentPackageName != null
				&& packageNames.contains(currentPackageName)) {
			packageNames.remove(currentPackageName);
		}

		final int defaultsLength = StrutsXmlConstants.DEFAULT_PACKAGE_NAMES.length;

		String[][] proposals = new String[defaultsLength + packageNames.size()][2];

		for (int i = 0; i < defaultsLength; i++) {
			proposals[i][0] = StrutsXmlConstants.DEFAULT_PACKAGE_NAMES[i][0];
		}

		int indx = defaultsLength;
		for (String p : packageNames) {
			proposals[indx++][0] = p;
		}
		return proposals;
	}

	private String[][] computeResultBodyProposals(final IDocument document,
			final int offset, final String typeAttrValue,
			final String namespaceParamValue) {
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
		} else if (StrutsXmlConstants.REDIRECT_ACTION_RESULT
				.equals(typeAttrValue)) {
			set = findRedirectActionNames(document, offset, namespaceParamValue);
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
			String valueSeparator, String attrvalue, boolean sort) {
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

		if (sort) {
			Collections.sort(list, proposalComparator);
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

							names.addAll(tilesXmlParser
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

	private Set<String> findRedirectActionNames(final IDocument document,
			final int offset, final String namespaceParamValue) {
		Set<String> namespaces = new HashSet<String>();

		// if there is a namespaceParamValue then used it, else get
		// namespace from parent package
		String namespace = namespaceParamValue;
		if (namespace == null) {
			TagRegion packageTagRegion = strutsXmlParser.getParentTagRegion(
					document, offset, StrutsXmlConstants.PACKAGE_TAG);
			if (packageTagRegion != null) {
				namespace = packageTagRegion.getAttrValue(
						StrutsXmlConstants.NAMESPACE_ATTR, "");
			} else {
				namespace = "";
			}

			// if namespace came NOT from namespaceParamValue then add special
			// namespaces
			namespaces.add("");
			namespaces.add("/");
		}

		namespaces.add(namespace);

		return strutsXmlParser.getActionNames(document, namespaces);
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
