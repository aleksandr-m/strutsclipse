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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public abstract class AbstractXmlParser {
	protected static final String CLOSE_TAG_TOKEN = "close_tag_token";

	private static final String DOUBLE_QUOTES_TOKEN = "double_quotes_token";
	private static final String SINGLE_QUOTES_TOKEN = "single_quotes_token";

	/**
	 * Finds the concrete attribute in the concrete tag. Returns multiple
	 * attribute {@link ElementRegion}-s if there are more than one tag with
	 * such name in the document. This method searches the whole document. If
	 * there is need to search specific region of the document use
	 * {@link #findAllTagAttr(IDocument, String, String, int, int)} method.
	 * 
	 * @param document
	 *            Document to search.
	 * @param tag
	 *            Tag name to search.
	 * @param attr
	 *            Attribute name to search.
	 * @return Found attributes.
	 */
	protected List<ElementRegion> findAllTagAttr(IDocument document,
			String tag, String attr) {
		return findAllTagAttr(document, tag, attr, 0, document.getLength());
	}

	protected List<ElementRegion> findAllTagAttr(IDocument document,
			String tag, String attr, int offset, int length) {
		IDocumentPartitioner partitioner = null;
		try {
			List<ElementRegion> attrRegions = new ArrayList<ElementRegion>();

			// create tag partitioner
			partitioner = createTagPartitioner(document, new String[] { tag });
			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(offset,
					length);

			// create attribute partitioner
			partitioner = createAttrPartitioner(document, new String[] { attr });

			for (ITypedRegion tagRegion : tagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())) {
					ITypedRegion[] regions = partitioner.computePartitioning(
							tagRegion.getOffset(), tagRegion.getLength());
					attrRegions.addAll(fetchAttrsRegions(document, regions));
				}
			}

			return attrRegions;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	protected List<IRegion> findTagsBodyRegionByAttrValue(IDocument document,
			String tag, String attr, String attrValue) {
		IDocumentPartitioner partitioner = null;
		try {
			List<IRegion> result = new ArrayList<IRegion>();

			final String closeTag = "/" + tag;

			// create tag partitioner
			partitioner = createTagPartitioner(document, new String[] { tag,
					closeTag });
			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(0,
					document.getLength());

			// create attribute partitioner
			partitioner = createAttrPartitioner(document, new String[] { attr });

			int startTagOffset = 0;
			for (ITypedRegion tagRegion : tagRegions) {
				if (closeTag.equals(tagRegion.getType()) && startTagOffset > 0) {
					result.add(new Region(startTagOffset, tagRegion.getOffset()
							- startTagOffset));
					startTagOffset = 0;
				} else if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion
						.getType())) {
					ITypedRegion[] regions = partitioner.computePartitioning(
							tagRegion.getOffset(), tagRegion.getLength());
					List<ElementRegion> attrregs = fetchAttrsRegions(document,
							regions);
					if (attrregs != null) {
						for (ElementRegion r : attrregs) {
							if (attrValue.equals(r.getValue())) {
								startTagOffset = tagRegion.getOffset()
										+ tagRegion.getLength();
								break;
							}
						}
					}
				}
			}

			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	protected ElementRegion findTagAttrByValue(IDocument document, String tag,
			String attr, String attrValue) {
		return findTagAttrByValue(document, tag, attr, attrValue, 0,
				document.getLength());
	}

	protected ElementRegion findTagAttrByValue(IDocument document, String tag,
			String attr, String attrValue, int offset, int length) {
		IDocumentPartitioner partitioner = null;
		try {
			ElementRegion attrRegion = null;

			// create tag partitioner
			partitioner = createTagPartitioner(document, new String[] { tag });
			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(offset,
					length);

			// create attribute partitioner
			partitioner = createAttrPartitioner(document, new String[] { attr });

			for (ITypedRegion tagRegion : tagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())) {
					ITypedRegion[] regions = partitioner.computePartitioning(
							tagRegion.getOffset(), tagRegion.getLength());
					List<ElementRegion> attrregs = fetchAttrsRegions(document,
							regions);
					if (attrregs != null && !attrregs.isEmpty()
							&& attrValue.equals(attrregs.get(0).getValue())) {
						attrRegion = attrregs.get(0);
						break;
					}
				}
			}

			return attrRegion;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	protected List<ElementRegion> parseTag(IDocument document,
			ITypedRegion tagRegion, String[] attrs) {
		IDocumentPartitioner partitioner = null;
		try {
			// create attribute partitioner
			partitioner = createAttrPartitioner(document, attrs);

			ITypedRegion[] regions = partitioner.computePartitioning(
					tagRegion.getOffset(), tagRegion.getLength());

			return fetchAttrsRegions(document, regions);
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	private IDocumentPartitioner createConnectPartitioner(IDocument document,
			IPredicateRule[] rules, String[] types) {
		RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
		scanner.setPredicateRules(rules);
		IDocumentPartitioner partitioner = new FastPartitioner(scanner, types);
		partitioner.connect(document);
		return partitioner;
	}

	protected IDocumentPartitioner createTagPartitioner(IDocument document,
			String[] tags) {
		IPredicateRule[] tagRules = new IPredicateRule[tags.length];
		for (int i = 0; i < tags.length; i++) {
			if (CLOSE_TAG_TOKEN.equals(tags[i])) {
				tagRules[i] = new MultiLineRule("</", ">", new Token(tags[i]));
			} else {
				tagRules[i] = new MultiLineRule("<" + tags[i], ">", new Token(
						tags[i]));
			}
		}
		return createConnectPartitioner(document, tagRules, tags);
	}

	private IDocumentPartitioner createAttrPartitioner(IDocument document,
			String[] attrs) {
		IPredicateRule[] attrRules = new IPredicateRule[attrs.length + 2];
		attrRules[0] = new SingleLineRule("\"", "\"", new Token(
				DOUBLE_QUOTES_TOKEN));
		attrRules[1] = new SingleLineRule("'", "'", new Token(
				SINGLE_QUOTES_TOKEN));

		String[] types = new String[attrs.length + 2];
		types[0] = DOUBLE_QUOTES_TOKEN;
		types[1] = SINGLE_QUOTES_TOKEN;

		for (int i = 0; i < attrs.length; i++) {
			attrRules[i + 2] = new MultiLineRule(attrs[i], "=", new Token(
					attrs[i]));

			types[i + 2] = attrs[i];
		}

		return createConnectPartitioner(document, attrRules, types);
	}

	private List<ElementRegion> fetchAttrsRegions(IDocument document,
			ITypedRegion[] regions) {
		List<ElementRegion> attrRegions = new ArrayList<ElementRegion>();
		if (regions != null) {
			String attrName = null;
			for (ITypedRegion r : regions) {
				// only legal types
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(r.getType())) {
					boolean quotesToken = DOUBLE_QUOTES_TOKEN.equals(r
							.getType())
							|| SINGLE_QUOTES_TOKEN.equals(r.getType());

					if (attrName != null && quotesToken) {
						try {
							// get value w/o quotes
							String val = document.get(r.getOffset() + 1,
									r.getLength() - 2);

							attrRegions.add(new ElementRegion(attrName, val, r
									.getOffset() + 1));
						} catch (BadLocationException e) {
							e.printStackTrace();
						}

						// set name to null
						attrName = null;
					} else if (!quotesToken) {
						attrName = r.getType();
					}
				}
			}
		}
		return attrRegions;
	}
}
