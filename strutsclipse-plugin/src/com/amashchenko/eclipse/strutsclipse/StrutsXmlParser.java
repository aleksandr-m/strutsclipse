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
package com.amashchenko.eclipse.strutsclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class StrutsXmlParser {
	private static final String[] TAGS = { StrutsXmlConstants.PACKAGE_TAG,
			StrutsXmlConstants.ACTION_TAG, StrutsXmlConstants.RESULT_TAG };

	private static final String[] ATTRS = { StrutsXmlConstants.EXTENDS_ATTR,
			StrutsXmlConstants.NAME_ATTR, StrutsXmlConstants.TYPE_ATTR,
			StrutsXmlConstants.METHOD_ATTR, StrutsXmlConstants.CLASS_ATTR };

	public static TagRegion getTagRegion(final IDocument document,
			final int offset) {
		IDocumentPartitioner partitioner = null;
		TagRegion result = null;
		try {
			IPredicateRule[] tagRules = new IPredicateRule[TAGS.length];
			for (int i = 0; i < TAGS.length; i++) {
				tagRules[i] = new MultiLineRule("<" + TAGS[i], ">", new Token(
						TAGS[i]));
			}

			RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
			scanner.setPredicateRules(tagRules);

			partitioner = new FastPartitioner(scanner, TAGS);

			partitioner.connect(document);

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			if (arrayContains(TAGS, tagRegion.getType())) {
				IPredicateRule[] attrRules = new IPredicateRule[ATTRS.length];
				for (int i = 0; i < ATTRS.length; i++) {
					attrRules[i] = new PatternRule(ATTRS[i] + "=\"", "\"",
							new Token(ATTRS[i]), ' ', true);
				}

				scanner.setPredicateRules(attrRules);

				partitioner = new FastPartitioner(scanner, ATTRS);

				partitioner.connect(document);

				AttrRegion currentAttr = null;
				String attrValuePrefix = null;

				// all attributes
				Map<String, AttrRegion> allAttrs = new HashMap<String, AttrRegion>();
				ITypedRegion[] regions = partitioner.computePartitioning(
						tagRegion.getOffset(), tagRegion.getLength());
				if (regions != null) {
					for (ITypedRegion r : regions) {
						// only legal types
						if (arrayContains(ATTRS, r.getType())) {
							try {
								String val = document.get(r.getOffset(),
										r.getLength());
								final int valBeginIndex = val.indexOf('"') + 1;
								final int valDocOffset = r.getOffset()
										+ valBeginIndex;
								val = val.substring(valBeginIndex,
										val.lastIndexOf('"'));

								allAttrs.put(
										r.getType(),
										new AttrRegion(r.getType(), val, r
												.getOffset() + valBeginIndex));

								// current attribute
								if (valDocOffset <= offset
										&& r.getOffset() + r.getLength() > offset) {
									currentAttr = new AttrRegion(r.getType(),
											val, valDocOffset);

									// attribute value to invocation offset
									attrValuePrefix = document
											.get(valDocOffset, offset
													- valDocOffset);
								}
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
					}
				}
				result = new TagRegion(tagRegion.getType(), currentAttr,
						attrValuePrefix, allAttrs);
			}

			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	private static boolean arrayContains(String[] arr, String val) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equalsIgnoreCase(val)) {
				return true;
			}
		}
		return false;
	}
}
