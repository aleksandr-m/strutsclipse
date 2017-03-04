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

import org.eclipse.jface.text.IDocument;

import com.amashchenko.eclipse.strutsclipse.xmlparser.AbstractXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibParser extends AbstractXmlParser {
	// action tag must be before link (a) tag
	private static final String[] TAGS = { StrutsTaglibConstants.URL_TAG,
			StrutsTaglibConstants.FORM_TAG, StrutsTaglibConstants.ACTION_TAG,
			StrutsTaglibConstants.LINK_TAG, StrutsTaglibConstants.SUBMIT_TAG,
			StrutsTaglibConstants.INCLUDE_TAG, StrutsTaglibConstants.TEXT_TAG,
			CLOSE_TAG_TOKEN };

	private static final String[] ATTRS = { StrutsTaglibConstants.ACTION_ATTR,
			StrutsTaglibConstants.NAMESPACE_ATTR,
			StrutsTaglibConstants.NAME_ATTR, StrutsTaglibConstants.VALUE_ATTR };

	public TagRegion getTagRegion(final IDocument document, final int offset) {
		return getTagRegion(document, offset, TAGS, ATTRS);
	}
}
