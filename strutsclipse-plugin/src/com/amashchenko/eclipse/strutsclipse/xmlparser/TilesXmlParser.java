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
package com.amashchenko.eclipse.strutsclipse.xmlparser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class TilesXmlParser extends AbstractXmlParser {
	private static final String DEFINITION_TAG = "definition";
	private static final String NAME_ATTR = "name";

	public Set<String> getDefinitionNames(final IDocument document) {
		List<ElementRegion> attrRegions = findAllTagAttr(document,
				DEFINITION_TAG, NAME_ATTR);
		Set<String> result = new HashSet<String>();
		if (attrRegions != null) {
			for (ElementRegion r : attrRegions) {
				result.add(r.getValue());
			}
		}
		return result;
	}

	public IRegion getDefinitionRegion(final IDocument document,
			final String definitionName) {
		ElementRegion attrRegion = findTagAttrByValue(document, DEFINITION_TAG,
				NAME_ATTR, definitionName);
		IRegion region = null;
		if (attrRegion != null) {
			region = attrRegion.getValueRegion();
		}
		return region;
	}
}
