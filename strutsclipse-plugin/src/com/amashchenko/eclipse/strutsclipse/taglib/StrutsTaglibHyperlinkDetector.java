/*
 * Copyright 2015-2018 Aleksandr Mashchenko.
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
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.amashchenko.eclipse.strutsclipse.AbstractStrutsHyperlinkDetector;
import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;
import com.amashchenko.eclipse.strutsclipse.ParseUtil;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.PropertiesParser;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.java.AnnotationParser;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlConstants;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibHyperlinkDetector extends
		AbstractStrutsHyperlinkDetector implements StrutsTaglibLocations {
	private final StrutsTaglibParser strutsTaglibParser;
	private final StrutsXmlParser strutsXmlParser;
	private final PropertiesParser propertiesParser;
	private final AnnotationParser annotationParser;

	public StrutsTaglibHyperlinkDetector() {
		strutsTaglibParser = new StrutsTaglibParser();
		strutsXmlParser = new StrutsXmlParser();
		propertiesParser = new PropertiesParser();
		annotationParser = new AnnotationParser();
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

				linksList.addAll(createAnnotationActionLinks(document,
						elementValue, elementRegion));
				break;
			case TEXT_NAME:
				linksList.addAll(createPropertiesKeysLinks(document,
						elementValue, elementRegion));
				break;
			}
		}

		// getText
		final TagRegion getTextRegion = strutsTaglibParser.getGetTextRegion(
				document, region.getOffset());
		if (getTextRegion != null && getTextRegion.getCurrentElement() != null) {
			linksList.addAll(createPropertiesKeysLinks(document, getTextRegion
					.getCurrentElement().getValue(), getTextRegion
					.getCurrentElement().getValueRegion()));
		}

		return linksListToArray(linksList);
	}

	private List<IHyperlink> createActionLinks(final IDocument document,
			final String elementValue, final IRegion elementRegion,
			final String namespaceParamValue) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		final Set<String> namespaces = new HashSet<String>();
		if (namespaceParamValue != null) {
			namespaces.add(namespaceParamValue);
		}

		// find struts resources
		List<ResourceDocument> resources = ProjectUtil
				.findStrutsResources(document);

		if (namespaceParamValue == null) {
			for (ResourceDocument rd : resources) {
				List<IRegion> regions = strutsXmlParser.getActionRegions(
						rd.getDocument(), elementValue);
				if (regions != null) {
					for (IRegion region : regions) {
						if (rd.getResource().getType() == IResource.FILE
								&& rd.getResource().exists()) {
							links.add(new FileHyperlink(elementRegion,
									(IFile) rd.getResource(), region));
						}
					}
				}
			}
		} else {
			for (ResourceDocument rd : resources) {
				IRegion region = strutsXmlParser.getActionRegion(
						rd.getDocument(), namespaces, elementValue);
				if (region != null) {
					if (rd.getResource().getType() == IResource.FILE
							&& rd.getResource().exists()) {
						links.add(new FileHyperlink(elementRegion, (IFile) rd
								.getResource(), region));
					}
				}
			}
		}

		return links;
	}

	private List<IHyperlink> createAnnotationActionLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		List<IHyperlink> links = new ArrayList<IHyperlink>();
		List<IJavaElement> elements = annotationParser
				.findAnnotationsActionElements(document, elementValue);
		for (IJavaElement element : elements) {
			if (element != null) {
				links.add(new JavaElementHyperlink(elementRegion, element));
			}
		}
		return links;
	}

	private List<IHyperlink> createPropertiesKeysLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		// get bundle names
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

		// local
		List<ResourceDocument> resources = ProjectUtil.findPropertiesResources(
				document, bundleNames);
		for (ResourceDocument rd : resources) {
			IRegion keyRegion = propertiesParser.getKeyRegion(rd.getDocument(),
					elementValue);
			if (keyRegion != null) {
				if (rd.getResource().getType() == IResource.FILE
						&& rd.getResource().exists()) {
					links.add(new FileHyperlink(elementRegion, (IFile) rd
							.getResource(), keyRegion));
				}
			}
		}

		// jars
		List<JarEntryStorage> jarStorages = ProjectUtil
				.findJarEntryPropertyResources(document, bundleNames);
		for (JarEntryStorage jarStorage : jarStorages) {
			IRegion keyRegion = propertiesParser.getKeyRegion(
					jarStorage.toDocument(), elementValue);
			if (keyRegion != null) {
				links.add(new StorageHyperlink(elementRegion, jarStorage,
						keyRegion));
			}
		}

		return links;
	}
}
