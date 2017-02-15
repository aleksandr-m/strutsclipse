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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.amashchenko.eclipse.strutsclipse.xmlparser.AbstractXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class TilesXmlParser extends AbstractXmlParser {
	private static final String[] TAGS = { TilesXmlConstants.DEFINITION_TAG,
			TilesXmlConstants.PUT_ATTRIBUTE_TAG, CLOSE_TAG_TOKEN };

	private static final String[] ATTRS = { TilesXmlConstants.EXTENDS_ATTR,
			TilesXmlConstants.TEMPLATE_ATTR, TilesXmlConstants.NAME_ATTR,
			TilesXmlConstants.VALUE_ATTR };

	public Set<String> getDefinitionNames(final IDocument document) {
		return getAttrsValues(document, TilesXmlConstants.DEFINITION_TAG,
				TilesXmlConstants.NAME_ATTR);
	}

	public IRegion getDefinitionRegion(final IDocument document,
			final String definitionName) {
		ElementRegion attrRegion = findTagAttrByValue(document,
				TilesXmlConstants.DEFINITION_TAG, TilesXmlConstants.NAME_ATTR,
				definitionName);
		IRegion region = null;
		if (attrRegion != null) {
			region = attrRegion.getValueRegion();
		}
		return region;
	}

	public List<ElementRegion> getDefinitionNameRegions(final IDocument document) {
		return findAllTagAttr(document, TilesXmlConstants.DEFINITION_TAG,
				TilesXmlConstants.NAME_ATTR);
	}

	public TagRegion getTagRegion(final IDocument document, final int offset) {
		return getTagRegion(document, offset, TAGS, ATTRS);
	}
}
