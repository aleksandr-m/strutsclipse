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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	public static TagRegion getTagRegion(final IDocument document,
			final int offset) {
		IDocumentPartitioner partitioner = null;
		TagRegion result = null;
		try {
			Token actionToken = new Token(StrutsXmlConstants.ACTION_TAG);
			Token resultToken = new Token(StrutsXmlConstants.RESULT_TAG);
			IPredicateRule actionRule = new MultiLineRule("<"
					+ StrutsXmlConstants.ACTION_TAG, ">", actionToken);
			IPredicateRule resultRule = new MultiLineRule("<"
					+ StrutsXmlConstants.RESULT_TAG, ">", resultToken);

			RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
			scanner.setPredicateRules(new IPredicateRule[] { actionRule,
					resultRule });

			partitioner = new FastPartitioner(scanner, new String[] {
					StrutsXmlConstants.ACTION_TAG,
					StrutsXmlConstants.RESULT_TAG });

			partitioner.connect(document);

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			if (StrutsXmlConstants.ACTION_TAG.equalsIgnoreCase(tagRegion
					.getType())
					|| StrutsXmlConstants.RESULT_TAG.equalsIgnoreCase(tagRegion
							.getType())) {
				List<String> legalContentTypes = new ArrayList<String>();
				legalContentTypes.add(StrutsXmlConstants.NAME_ATTR);
				legalContentTypes.add(StrutsXmlConstants.TYPE_ATTR);
				legalContentTypes.add(StrutsXmlConstants.METHOD_ATTR);
				legalContentTypes.add(StrutsXmlConstants.CLASS_ATTR);

				IPredicateRule[] rules = new IPredicateRule[legalContentTypes
						.size()];

				for (int i = 0; i < legalContentTypes.size(); i++) {
					Token token = new Token(legalContentTypes.get(i));
					IPredicateRule rule = new PatternRule(
							legalContentTypes.get(i) + "=\"", "\"", token, ' ',
							true);
					rules[i] = rule;
				}

				scanner.setPredicateRules(rules);

				partitioner = new FastPartitioner(scanner,
						legalContentTypes.toArray(new String[legalContentTypes
								.size()]));

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
						if (legalContentTypes.contains(r.getType())) {
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
}
