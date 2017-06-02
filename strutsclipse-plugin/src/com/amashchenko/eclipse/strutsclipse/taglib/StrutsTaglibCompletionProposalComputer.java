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
package com.amashchenko.eclipse.strutsclipse.taglib;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.CompletionProposalHelper;
import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;
import com.amashchenko.eclipse.strutsclipse.ParseUtil;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlConstants;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibCompletionProposalComputer implements
		ICompletionProposalComputer, StrutsTaglibLocations {
	private final StrutsTaglibParser strutsTaglibParser;
	private final StrutsXmlParser strutsXmlParser;

	private final CompletionProposalComparator proposalComparator;

	public StrutsTaglibCompletionProposalComputer() {
		strutsTaglibParser = new StrutsTaglibParser();
		strutsXmlParser = new StrutsXmlParser();
		proposalComparator = new CompletionProposalComparator();
		proposalComparator.setOrderAlphabetically(true);
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {

		final TagRegion tagRegion = strutsTaglibParser.getTagRegion(
				context.getDocument(), context.getInvocationOffset());

		List<ICompletionProposal> proposals = null;
		String[][] proposalsData = null;

		IRegion proposalRegion = null;
		String elementValuePrefix = null;
		String elementValue = null;

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			proposalRegion = tagRegion.getCurrentElement().getValueRegion();
			elementValuePrefix = tagRegion.getCurrentElementValuePrefix();
			elementValue = tagRegion.getCurrentElement().getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case URL_ACTION:
			case FORM_ACTION:
			case LINK_ACTION:
			case ACTION_NAME:
			case SUBMIT_ACTION:
				proposalsData = CompletionProposalHelper
						.proposalDataFromSet(findStrutsActionNames(context
								.getDocument(), tagRegion.getAttrValue(
								StrutsTaglibConstants.NAMESPACE_ATTR, null)));
				break;
			case URL_NAMESPACE:
			case FORM_NAMESPACE:
			case LINK_NAMESPACE:
			case ACTION_NAMESPACE:
				proposalsData = CompletionProposalHelper
						.proposalDataFromSet(findStrutsPackagesNamespaces(context
								.getDocument()));
				break;
			case INCLUDE_VALUE:
				proposalsData = CompletionProposalHelper
						.proposalDataFromSet(ProjectUtil
								.findJspHtmlFilesPaths(context.getDocument()));
				break;
			case TEXT_NAME:
				proposalsData = computeTextNameProposals(context.getDocument());
				break;
			}
		}

		// getText
		final TagRegion getTextRegion = strutsTaglibParser.getGetTextRegion(
				context.getDocument(), context.getInvocationOffset());
		if (getTextRegion != null && getTextRegion.getCurrentElement() != null) {
			proposalRegion = getTextRegion.getCurrentElement().getValueRegion();
			elementValuePrefix = getTextRegion.getCurrentElementValuePrefix();
			elementValue = getTextRegion.getCurrentElement().getValue();

			proposalsData = computeTextNameProposals(context.getDocument());
		}

		if (proposals == null && proposalsData != null) {
			proposals = CompletionProposalHelper.createAttrCompletionProposals(
					proposalsData, elementValuePrefix, proposalRegion, null,
					elementValue, proposalComparator);
		}
		if (proposals == null) {
			proposals = new ArrayList<ICompletionProposal>();
		}

		return proposals;
	}

	private Set<String> findStrutsActionNames(final IDocument currentDocument,
			final String namespaceParamValue) {
		Set<String> names = new HashSet<String>();

		Set<String> namespaces = new HashSet<String>();
		if (namespaceParamValue != null) {
			namespaces.add(namespaceParamValue);
		}

		IProject project = ProjectUtil.getCurrentProject(currentDocument);
		if (project != null && project.exists()) {
			// find struts resources
			List<ResourceDocument> resources = ProjectUtil
					.findStrutsResources(currentDocument);

			if (namespaceParamValue == null) {
				for (ResourceDocument rd : resources) {
					names.addAll(strutsXmlParser.getActionNames(rd
							.getDocument()));
				}
			} else {
				for (ResourceDocument rd : resources) {
					names.addAll(strutsXmlParser.getActionNames(
							rd.getDocument(), namespaces));
				}
			}
		}

		return names;
	}

	private Set<String> findStrutsPackagesNamespaces(
			final IDocument currentDocument) {
		Set<String> namespaces = new HashSet<String>();

		IProject project = ProjectUtil.getCurrentProject(currentDocument);
		if (project != null && project.exists()) {
			// find struts resources
			List<ResourceDocument> resources = ProjectUtil
					.findStrutsResources(currentDocument);
			for (ResourceDocument rd : resources) {
				namespaces.addAll(strutsXmlParser.getPackageNamespaces(rd
						.getDocument()));
			}
		}

		return namespaces;
	}

	private String[][] computeTextNameProposals(final IDocument document) {
		Set<String> bundleNames = new HashSet<String>();
		List<ResourceDocument> strutsResources = ProjectUtil
				.findStrutsResources(document);
		for (ResourceDocument rd : strutsResources) {
			Map<String, String> constants = strutsXmlParser.getConstantsMap(rd
					.getDocument());
			bundleNames
					.addAll(ParseUtil.delimitedStringToSet(constants
							.get(StrutsXmlConstants.CONSTANT_CUSTOM_RESOURCES),
							StrutsXmlConstants.MULTI_VALUE_SEPARATOR));
		}
		return CompletionProposalHelper.proposalDataFromMap(findPropertiesKeys(
				document, bundleNames));
	}

	private Map<String, String> findPropertiesKeys(IDocument currentDocument,
			Set<String> bundleNames) {
		Map<String, String> map = new HashMap<String, String>();

		// local
		List<ResourceDocument> resources = ProjectUtil.findPropertiesResources(
				currentDocument, bundleNames);
		for (ResourceDocument rd : resources) {
			try {
				ResourceBundle bundle = new PropertyResourceBundle(
						new StringReader(rd.getDocument().get()));
				fillMap(map, rd.getRelativePath(), bundle);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// jars
		List<JarEntryStorage> jarStorages = ProjectUtil
				.findJarEntryPropertyResources(currentDocument, bundleNames);
		for (JarEntryStorage jarStorage : jarStorages) {
			try {
				ResourceBundle bundle = new PropertyResourceBundle(
						jarStorage.getContents());
				fillMap(map, jarStorage.getFullPath().toString(), bundle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return map;
	}

	private void fillMap(Map<String, String> map, String path,
			ResourceBundle bundle) {
		if (bundle != null) {
			Set<String> keys = bundle.keySet();
			for (String k : keys) {
				if (!map.containsKey(k)) {
					map.put(k, "");
				}
				map.put(k, map.get(k) + path + ":<br/>" + bundle.getString(k)
						+ "<br/><br/>");
			}
		}
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
