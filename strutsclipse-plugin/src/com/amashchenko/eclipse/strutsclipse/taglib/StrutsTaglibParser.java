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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import com.amashchenko.eclipse.strutsclipse.xmlparser.AbstractXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibParser extends AbstractXmlParser {
	// action tag must be before link (a) tag
	private static final String[] TAGS = { StrutsTaglibConstants.URL_TAG,
			StrutsTaglibConstants.FORM_TAG, StrutsTaglibConstants.ACTION_TAG,
			StrutsTaglibConstants.LINK_TAG, StrutsTaglibConstants.SUBMIT_TAG,
			StrutsTaglibConstants.INCLUDE_TAG, StrutsTaglibConstants.TEXT_TAG,
			StrutsTaglibConstants.TAGLIB_PREFIX, CLOSE_TAG_TOKEN };

	private static final String[] ATTRS = { StrutsTaglibConstants.ACTION_ATTR,
			StrutsTaglibConstants.NAMESPACE_ATTR,
			StrutsTaglibConstants.NAME_ATTR, StrutsTaglibConstants.VALUE_ATTR,
			StrutsTaglibConstants.THEME_ATTR };

	private static final String GET_TEXT_TOKEN = "getText_token";
	private static final String GET_TEXT_ESCAPED_TOKEN = "getText_escaped_token";

	public TagRegion getTagRegion(final IDocument document, final int offset) {
		return getTagRegion(document, offset, TAGS, ATTRS);
	}

	public TagRegion getGetTextRegion(final IDocument document, final int offset) {
		IDocumentPartitioner partitioner = null;
		try {
			TagRegion result = null;

			IPredicateRule[] rules = new IPredicateRule[3];
			rules[0] = new SingleLineRule("getText('", "')", new Token(
					GET_TEXT_TOKEN));
			rules[1] = new SingleLineRule("getText(\"", "\")", new Token(
					GET_TEXT_TOKEN));
			rules[2] = new SingleLineRule("getText(\\\"", "\\\")", new Token(
					GET_TEXT_ESCAPED_TOKEN));

			RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
			scanner.setPredicateRules(rules);
			partitioner = new FastPartitioner(scanner, new String[] {
					GET_TEXT_TOKEN, GET_TEXT_ESCAPED_TOKEN });
			partitioner.connect(document);

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			final int textOffset;
			final int textLength;
			if (GET_TEXT_ESCAPED_TOKEN.equals(tagRegion.getType())) {
				textOffset = tagRegion.getOffset() + 10;
				textLength = tagRegion.getLength() - 13;
			} else {
				textOffset = tagRegion.getOffset() + 9;
				textLength = tagRegion.getLength() - 11;
			}

			// getText and inside quotes
			if ((GET_TEXT_TOKEN.equals(tagRegion.getType()) || GET_TEXT_ESCAPED_TOKEN
					.equals(tagRegion.getType()))
					&& offset >= textOffset
					&& offset < tagRegion.getOffset() + tagRegion.getLength()
							- 1) {
				final int prefixOffset = offset - textOffset;
				try {
					String value = document.get(textOffset, textLength);
					String prefix = "";
					if (!value.isEmpty() && prefixOffset > 0
							&& prefixOffset < value.length()) {
						prefix = value.substring(0, prefixOffset);
					}

					result = new TagRegion("", new ElementRegion("", value,
							textOffset), prefix, null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}
}
