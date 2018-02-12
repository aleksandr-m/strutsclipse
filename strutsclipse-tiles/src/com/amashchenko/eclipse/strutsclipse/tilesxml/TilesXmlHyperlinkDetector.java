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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.amashchenko.eclipse.strutsclipse.AbstractStrutsHyperlinkDetector;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class TilesXmlHyperlinkDetector extends AbstractStrutsHyperlinkDetector
		implements TilesXmlLocations {
	private final TilesXmlParser tilesXmlParser;

	public TilesXmlHyperlinkDetector() {
		tilesXmlParser = new TilesXmlParser();
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		List<IHyperlink> linksList = new ArrayList<IHyperlink>();

		final TagRegion tagRegion = tilesXmlParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case DEFINITION_EXTENDS:
				linksList.addAll(createDefinitionLocationLinks(document,
						elementValue, elementRegion));
				break;
			}
		}

		return linksListToArray(linksList);
	}

	private List<IHyperlink> createDefinitionLocationLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		IRegion region = tilesXmlParser.getDefinitionRegion(document,
				elementValue);
		if (region != null) {
			IFile file = ProjectUtil.getCurrentDocumentFile(document);
			if (file.exists()) {
				links.add(new FileHyperlink(elementRegion, file, region));
			}
		}

		return links;
	}
}
