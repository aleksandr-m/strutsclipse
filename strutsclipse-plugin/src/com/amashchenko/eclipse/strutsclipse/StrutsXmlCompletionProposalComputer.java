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
package com.amashchenko.eclipse.strutsclipse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;

import com.amashchenko.eclipse.strutsclipse.java.ActionMethodProposalComparator;
import com.amashchenko.eclipse.strutsclipse.java.JavaClassCompletion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TilesXmlParser;

public class StrutsXmlCompletionProposalComputer extends
		AbstractXmlCompletionProposalComputer implements StrutsXmlLocations {
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
			case BEAN_SCOPE:
				proposalsData = StrutsXmlConstants.DEFAULT_BEAN_SCOPES;
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
			case BEAN_CLASS:
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
							proposalsData = proposalDataFromSet(packageNames);
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
					elementValue, sortProposals ? proposalComparator : null);
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
			set = findJspHtmlFilesPaths(document);
		} else if (StrutsXmlConstants.TILES_RESULT.equals(typeAttrValue)) {
			set = findTilesDefinitionNames(document);
		} else if (StrutsXmlConstants.FREEMARKER_RESULT.equals(typeAttrValue)) {
			set = findFreeMarkerFilesPaths(document);
		} else if (StrutsXmlConstants.REDIRECT_ACTION_RESULT
				.equals(typeAttrValue)) {
			set = findRedirectActionNames(document, offset, namespaceParamValue);
		}

		return proposalDataFromSet(set);
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
