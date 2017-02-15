/*
 * Copyright 2015-2017 Aleksandr Mashchenko.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;

import com.amashchenko.eclipse.strutsclipse.AbstractXmlCompletionProposalComputer;
import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;
import com.amashchenko.eclipse.strutsclipse.ParseUtil;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.java.ActionMethodProposalComparator;
import com.amashchenko.eclipse.strutsclipse.java.JavaClassCompletion;
import com.amashchenko.eclipse.strutsclipse.tilesxml.TilesXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagGroup;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

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
				multiValueSeparator = StrutsXmlConstants.MULTI_VALUE_SEPARATOR;
				sortProposals = true;
				break;
			case BEAN_SCOPE:
				proposalsData = StrutsXmlConstants.DEFAULT_BEAN_SCOPES;
				break;
			case CONSTANT_NAME:
				proposalsData = StrutsXmlConstants.DEFAULT_CONSTANTS;
				break;
			case INCLUDE_FILE:
				proposalsData = computeIncludeFileProposals(context
						.getDocument());
				break;
			case INTERCEPTOR_REF_NAME:
			case DEFAULT_INTERCEPTOR_REF_NAME:
				proposalsData = computeInterceptorRefProposals(
						context.getDocument(), context.getInvocationOffset());
				sortProposals = true;
				break;
			case DEFAULT_ACTION_REF_NAME:
				// same as for the result body, but with concrete namespace
				TagRegion packageTagRegion = strutsXmlParser
						.getParentTagRegion(context.getDocument(),
								context.getInvocationOffset(),
								StrutsXmlConstants.PACKAGE_TAG);
				if (packageTagRegion != null) {
					String namespace = packageTagRegion.getAttrValue(
							StrutsXmlConstants.NAMESPACE_ATTR, "");
					proposalsData = proposalDataFromSet(findRedirectActionNames(
							context.getDocument(),
							context.getInvocationOffset(), namespace));
					sortProposals = true;
				}
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
				if (strutsXmlParser.atLeast2_5(context.getDocument())) {
					// from 2.5 result name attribute can have multiple values
					// separated by ,
					multiValueSeparator = StrutsXmlConstants.MULTI_VALUE_SEPARATOR;
				}
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
		// current document
		Set<String> packageNames = strutsXmlParser.getPackageNames(document);

		// other struts files
		IPath currentPath = ProjectUtil.getCurrentDocumentPath(document);
		List<ResourceDocument> resources = ProjectUtil
				.findStrutsResources(document);
		for (ResourceDocument rd : resources) {
			if (!rd.getResource().getFullPath().equals(currentPath)) {
				packageNames.addAll(strutsXmlParser.getPackageNames(rd
						.getDocument()));
			}
		}

		List<JarEntryStorage> jarStorages = ProjectUtil
				.findJarEntryStrutsResources(document);
		for (JarEntryStorage jarStorage : jarStorages) {
			packageNames.addAll(strutsXmlParser.getPackageNames(jarStorage
					.toDocument()));
		}

		// remove current package name
		if (currentPackageName != null
				&& packageNames.contains(currentPackageName)) {
			packageNames.remove(currentPackageName);
		}

		return proposalDataFromSet(packageNames);
	}

	private String[][] computeResultBodyProposals(final IDocument document,
			final int offset, final String typeAttrValue,
			final String namespaceParamValue) {
		Set<String> set = null;
		// assume that default is dispatcher for now, TODO improve
		// that
		if (typeAttrValue == null
				|| StrutsXmlConstants.DISPATCHER_RESULT.equals(typeAttrValue)) {
			set = ProjectUtil.findJspHtmlFilesPaths(document);
		} else if (StrutsXmlConstants.TILES_RESULT.equals(typeAttrValue)) {
			set = findTilesDefinitionNames(document);
		} else if (StrutsXmlConstants.FREEMARKER_RESULT.equals(typeAttrValue)) {
			set = ProjectUtil.findFreeMarkerFilesPaths(document);
		} else if (StrutsXmlConstants.REDIRECT_ACTION_RESULT
				.equals(typeAttrValue)) {
			set = findRedirectActionNames(document, offset, namespaceParamValue);
		}

		return proposalDataFromSet(set);
	}

	private Set<String> findTilesDefinitionNames(final IDocument currentDocument) {
		final Set<String> names = new HashSet<String>();

		IProject project = ProjectUtil.getCurrentProject(currentDocument);
		if (project != null && project.exists()) {
			// find tiles resources
			List<ResourceDocument> resources = ProjectUtil
					.findTilesResources(currentDocument);
			for (ResourceDocument rd : resources) {
				names.addAll(tilesXmlParser.getDefinitionNames(rd.getDocument()));
			}
		}

		return names;
	}

	private String[][] computeIncludeFileProposals(
			final IDocument currentDocument) {
		IPath currentPath = ProjectUtil.getCurrentDocumentPath(currentDocument);

		Set<String> paths = new HashSet<String>();
		List<ResourceDocument> resources = ProjectUtil
				.findStrutsResources(currentDocument);

		if (resources != null) {
			for (ResourceDocument r : resources) {
				if (!r.getResource().getFullPath().equals(currentPath)) {
					paths.add(r.getRelativePath());
				}
			}
		}

		return proposalDataFromSet(paths);
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

	private String[][] computeInterceptorRefProposals(final IDocument document,
			final int offset) {
		List<String[]> results = new ArrayList<String[]>();

		TagRegion packageTagRegion = strutsXmlParser.getParentTagRegion(
				document, offset, StrutsXmlConstants.PACKAGE_TAG);
		if (packageTagRegion != null) {
			String packageExtends = packageTagRegion.getAttrValue(
					StrutsXmlConstants.EXTENDS_ATTR, null);

			// local interceptors
			Map<String, TagGroup> interceptorsMap = strutsXmlParser
					.getPackageInterceptorsTagRegions(document);

			if (packageExtends != null) {
				// external interceptors
				interceptorsMap.putAll(createInterceptorsMapFromJars(document));
			}

			String packageName = packageTagRegion.getAttrValue(
					StrutsXmlConstants.NAME_ATTR, null);

			collectInterceptorsNames(interceptorsMap, packageName,
					new HashSet<String>(), results);
		}

		return proposalDataFromList(results);
	}

	private void collectInterceptorsNames(Map<String, TagGroup> map,
			String packageName, Set<String> scanedPackages,
			List<String[]> results) {
		if (map.containsKey(packageName)
				&& !scanedPackages.contains(packageName)) {
			TagGroup tagGroup = map.get(packageName);
			if (tagGroup != null) {
				List<TagRegion> tagRegions = tagGroup.getTagRegions();
				if (tagRegions != null) {
					for (TagRegion tr : tagRegions) {
						String val = tr.getAttrValue(
								StrutsXmlConstants.NAME_ATTR, null);
						if (val != null) {
							results.add(new String[] {
									val,
									tr.getName() + " from " + packageName
											+ " package" });
						}
					}
				}

				scanedPackages.add(packageName);

				TagRegion parentTagRegion = tagGroup.getParentTagRegion();
				if (parentTagRegion != null) {
					String extendsValue = parentTagRegion.getAttrValue(
							StrutsXmlConstants.EXTENDS_ATTR, null);

					if (extendsValue != null) {
						Set<String> extendsSet = ParseUtil
								.delimitedStringToSet(
										extendsValue,
										StrutsXmlConstants.MULTI_VALUE_SEPARATOR);

						for (String extnd : extendsSet) {
							collectInterceptorsNames(map, extnd,
									scanedPackages, results);
						}
					}
				}
			}
		}
	}

	private Map<String, TagGroup> createInterceptorsMapFromJars(
			final IDocument document) {
		Map<String, TagGroup> jarsInterceptorsMap = new HashMap<String, TagGroup>();
		List<JarEntryStorage> jarStorages = ProjectUtil
				.findJarEntryStrutsResources(document);
		for (JarEntryStorage jarStorage : jarStorages) {
			jarsInterceptorsMap.putAll(strutsXmlParser
					.getPackageInterceptorsTagRegions(jarStorage.toDocument()));
		}
		return jarsInterceptorsMap;
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
