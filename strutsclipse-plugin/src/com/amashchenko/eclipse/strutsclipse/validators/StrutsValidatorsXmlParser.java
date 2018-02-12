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
package com.amashchenko.eclipse.strutsclipse.validators;

import java.util.Set;

import org.eclipse.jface.text.IDocument;

import com.amashchenko.eclipse.strutsclipse.xmlparser.AbstractXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsValidatorsXmlParser extends AbstractXmlParser {
	private static final String[] TAGS = {
			StrutsValidatorsXmlConstants.FIELD_VALIDATOR_TAG,
			StrutsValidatorsXmlConstants.VALIDATOR_TAG, CLOSE_TAG_TOKEN };

	private static final String[] ATTRS = { StrutsValidatorsXmlConstants.TYPE_ATTR };

	public TagRegion getTagRegion(final IDocument document, final int offset) {
		return getTagRegion(document, offset, TAGS, ATTRS);
	}

	public Set<String> getValidatorsNames(final IDocument document) {
		return getAttrsValues(document,
				StrutsValidatorsXmlConstants.VALIDATOR_TAG,
				StrutsValidatorsXmlConstants.NAME_ATTR);
	}
}
