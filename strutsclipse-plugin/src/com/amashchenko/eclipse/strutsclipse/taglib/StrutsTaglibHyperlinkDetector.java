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
package com.amashchenko.eclipse.strutsclipse.taglib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.amashchenko.eclipse.strutsclipse.AbstractStrutsHyperlinkDetector;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibHyperlinkDetector extends
		AbstractStrutsHyperlinkDetector implements StrutsTaglibLocations {
	private final StrutsTaglibParser strutsTaglibParser;
	private final StrutsXmlParser strutsXmlParser;

	public StrutsTaglibHyperlinkDetector() {
		strutsTaglibParser = new StrutsTaglibParser();
		strutsXmlParser = new StrutsXmlParser();
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		List<IHyperlink> linksList = new ArrayList<IHyperlink>();

		final TagRegion tagRegion = strutsTaglibParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case URL_ACTION:
			case FORM_ACTION:
			case LINK_ACTION:
			case ACTION_NAME:
			case SUBMIT_ACTION:
				linksList.addAll(createActionLinks(document, elementValue,
						elementRegion, tagRegion.getAttrValue(
								StrutsTaglibConstants.NAMESPACE_ATTR, null)));
				break;
			}
		}

		return linksListToArray(linksList);
	}

	private List<IHyperlink> createActionLinks(final IDocument document,
			final String elementValue, final IRegion elementRegion,
			final String namespaceParamValue) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		final Set<String> namespaces = new HashSet<String>();
		String namespace = namespaceParamValue;
		if (namespace == null) {
			namespaces.add("");
			namespaces.add("/");
		} else {
			namespaces.add(namespace);
		}

		IProject project = ProjectUtil.getCurrentProject(document);
		if (project != null && project.exists()) {
			// find struts resources
			List<ResourceDocument> resources = ProjectUtil
					.findStrutsResources(document);
			for (ResourceDocument rd : resources) {
				IRegion region = strutsXmlParser.getActionRegion(
						rd.getDocument(), namespaces, elementValue);
				if (region != null) {
					IFile file = project.getFile(rd.getResource()
							.getProjectRelativePath());
					if (file.exists()) {
						links.add(new FileHyperlink(elementRegion, file, region));
					}
				}
			}
		}

		return links;
	}
}
