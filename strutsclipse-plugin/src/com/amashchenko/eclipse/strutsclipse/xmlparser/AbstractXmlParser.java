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
package com.amashchenko.eclipse.strutsclipse.xmlparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public abstract class AbstractXmlParser {
	public static final String CLOSE_TAG_TOKEN = "close_tag_token";
	public static final String COMMENT_TOKEN = "comment_token";

	private static final String DOUBLE_QUOTES_TOKEN = "double_quotes_token";
	private static final String SINGLE_QUOTES_TOKEN = "single_quotes_token";

	private static final String DOCTYPE = "!DOCTYPE";
	private static final String DOCTYPE_LOWER_CASE = "!doctype";

	protected TagRegion getTagRegion(final IDocument document,
			final int offset, final String[] tags, final String[] attrs) {
		IDocumentPartitioner partitioner = null;
		try {
			TagRegion result = null;

			// create tag partitioner
			partitioner = createTagPartitioner(document, tags);

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			ElementRegion currentElement = null;
			String elementValuePrefix = null;

			// check if offset is between start and end tags
			int bodyOffset = -1;
			int bodyLength = -1;
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())) {
				ITypedRegion nextRegion = partitioner.getPartition(tagRegion
						.getOffset() + tagRegion.getLength());
				if (CLOSE_TAG_TOKEN.equals(nextRegion.getType())) {
					bodyOffset = tagRegion.getOffset();
					bodyLength = tagRegion.getLength();
				}
			} else if (CLOSE_TAG_TOKEN.equals(tagRegion.getType())
					&& tagRegion.getOffset() == offset) {
				ITypedRegion prevRegion = partitioner.getPartition(tagRegion
						.getOffset() - 1);
				if (IDocument.DEFAULT_CONTENT_TYPE.equals(prevRegion.getType())) {
					bodyOffset = prevRegion.getOffset();
					bodyLength = prevRegion.getLength();
				} else {
					bodyOffset = tagRegion.getOffset();
				}
			}
			if (bodyOffset != -1) {
				if (bodyLength == -1) {
					currentElement = new ElementRegion(null, "",
							tagRegion.getOffset());
					elementValuePrefix = "";
				} else {
					try {
						currentElement = new ElementRegion(null, document.get(
								bodyOffset, bodyLength), bodyOffset);
						elementValuePrefix = document.get(bodyOffset, offset
								- bodyOffset);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}

				// get start tag of current tag body
				tagRegion = partitioner.getPartition(bodyOffset - 1);
			}

			if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())
					&& !CLOSE_TAG_TOKEN.equals(tagRegion.getType())) {
				List<ElementRegion> attrRegions = parseTag(document, tagRegion,
						attrs);

				// all attributes
				if (attrRegions != null) {
					for (ElementRegion r : attrRegions) {
						try {
							final int valDocOffset = r.getValueRegion()
									.getOffset();

							// if not in tag body and current attribute
							if (currentElement == null
									&& valDocOffset - 1 <= offset
									&& valDocOffset
											+ r.getValueRegion().getLength()
											+ 1 > offset) {
								currentElement = r;

								// attribute value to invocation offset
								elementValuePrefix = document.get(valDocOffset,
										offset - valDocOffset);
							}
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}

				result = new TagRegion(tagRegion.getType(), currentElement,
						elementValuePrefix, attrRegions);
			}

			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	/**
	 * Finds the concrete attribute in the concrete tag. Returns multiple
	 * attribute {@link ElementRegion}-s if there are more than one tag with
	 * such name in the document. This method searches the whole document and
	 * doesn't get body of the tag. If there is need to search specific region
	 * of the document or to get body of the tag use
	 * {@link #findAllTagAttr(IDocument, String, String, boolean, int, int)}
	 * method.
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
		return findAllTagAttr(document, tag, attr, false, 0,
				document.getLength());
	}

	protected List<ElementRegion> findAllTagAttr(IDocument document,
			String tag, String attr, boolean fetchBody, int offset, int length) {
		IDocumentPartitioner partitioner = null;
		try {
			List<ElementRegion> attrRegions = new ArrayList<ElementRegion>();

			final String closeTag = "/" + tag;

			final String[] tags;
			if (fetchBody) {
				tags = new String[] { tag, COMMENT_TOKEN, closeTag };
			} else {
				tags = new String[] { tag, COMMENT_TOKEN };
			}

			// create tag partitioner
			partitioner = createTagPartitioner(document, tags);
			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(offset,
					length);

			// create attribute partitioner
			partitioner = createAttrPartitioner(document, new String[] { attr });

			int bodyOffset = -1;
			for (ITypedRegion tagRegion : tagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())
						&& !COMMENT_TOKEN.equals(tagRegion.getType())) {
					if (closeTag.equals(tagRegion.getType())) {
						if (bodyOffset != -1) {
							try {
								ElementRegion region = new ElementRegion(null,
										document.get(bodyOffset,
												tagRegion.getOffset()
														- bodyOffset),
										bodyOffset);
								attrRegions.add(region);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							bodyOffset = -1;
						}
					} else {
						ITypedRegion[] regions = partitioner
								.computePartitioning(tagRegion.getOffset(),
										tagRegion.getLength());
						attrRegions
								.addAll(fetchAttrsRegions(document, regions));
						bodyOffset = tagRegion.getOffset()
								+ tagRegion.getLength();
					}
				}
			}
			return attrRegions;
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

	private ElementRegion findTagAttrByValue(IDocument document, String tag,
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
			} else if (COMMENT_TOKEN.equals(tags[i])) {
				tagRules[i] = new MultiLineRule("<!--", "-->", new Token(
						tags[i]));
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
			attrRules[i + 2] = new WordPatternRule(new AttributeDetector(),
					attrs[i], "=", new Token(attrs[i]));

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

	protected Set<String> getAttrsValues(final IDocument document,
			final String tag, final String attr) {
		List<ElementRegion> attrRegions = findAllTagAttr(document, tag, attr);
		Set<String> result = new HashSet<String>();
		if (attrRegions != null) {
			for (ElementRegion r : attrRegions) {
				if (r != null && r.getValue() != null) {
					result.add(r.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * Gets tag regions in parent tag grouped by parent tag attribute. <br/>
	 * <br/>
	 * E.g. If parentTagName is 'package', parentTagAttrName is `namespace`,
	 * tagName is 'action' and attrNames are attributes names of action tag;
	 * this method will produce map of action tags regions where key is a
	 * package namespace.
	 * 
	 * @param document
	 *            Document to parse.
	 * @param parentTagName
	 *            Parent tag name to get tags from.
	 * @param parentTagAttrNames
	 *            Attributes names of parent tag to get.
	 * @param tagNames
	 *            Tags names to get.
	 * @param attrNames
	 *            Attributes names of tag to get.
	 * @param groupByParentTagAttrName
	 *            Group by this parent tag attribute.
	 * @return Map of tags regions where key is a parent tag attribute.
	 */
	protected Map<String, TagGroup> getGroupedTagRegions(
			final IDocument document, final String parentTagName,
			final String[] parentTagAttrNames, final String[] tagNames,
			final String[] attrNames, final String groupByParentTagAttrName) {
		IDocumentPartitioner tagPartitioner = null;
		IDocumentPartitioner attrPartitioner = null;
		try {
			Map<String, TagGroup> results = new HashMap<String, TagGroup>();

			final String closeTagName = "/" + parentTagName;

			// create parent tag partitioner
			tagPartitioner = createTagPartitioner(document, new String[] {
					parentTagName, closeTagName, COMMENT_TOKEN });

			ITypedRegion[] parentTagRegions = tagPartitioner
					.computePartitioning(0, document.getLength());

			// create tag partitioner
			String[] tags = Arrays.copyOf(tagNames, tagNames.length + 1);
			tags[tagNames.length] = COMMENT_TOKEN;
			tagPartitioner = createTagPartitioner(document, tags);

			// create attribute partitioner
			attrPartitioner = createAttrPartitioner(document, attrNames);

			String key = null;
			int parentBodyOffset = 0;
			List<ElementRegion> parentTagAttrRegions = null;
			for (ITypedRegion parentTagRegion : parentTagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(parentTagRegion
						.getType())
						&& !COMMENT_TOKEN.equals(parentTagRegion.getType())) {
					if (closeTagName.equals(parentTagRegion.getType())) {
						// get tags regions
						ITypedRegion[] tagRegions = tagPartitioner
								.computePartitioning(parentBodyOffset,
										parentTagRegion.getOffset()
												- parentBodyOffset);

						List<TagRegion> tagRegionsList = new ArrayList<TagRegion>();
						// all attributes
						for (ITypedRegion tagRegion : tagRegions) {
							if (!IDocument.DEFAULT_CONTENT_TYPE
									.equals(tagRegion.getType())
									&& !COMMENT_TOKEN.equals(tagRegion
											.getType())) {
								ITypedRegion[] regions = attrPartitioner
										.computePartitioning(
												tagRegion.getOffset(),
												tagRegion.getLength());

								tagRegionsList.add(new TagRegion(tagRegion
										.getType(), null, null,
										fetchAttrsRegions(document, regions)));
							}
						}

						if (results.containsKey(key)) {
							results.get(key).getTagRegions()
									.addAll(tagRegionsList);
						} else {
							results.put(key, new TagGroup(new TagRegion(
									parentTagName, null, null,
									parentTagAttrRegions), tagRegionsList));
						}
					} else {
						// parse parent start tag
						parentTagAttrRegions = parseTag(document,
								parentTagRegion, parentTagAttrNames);

						key = "";
						for (ElementRegion ptr : parentTagAttrRegions) {
							if (ptr.getName().equals(groupByParentTagAttrName)) {
								key = ptr.getValue();
							}
						}

						parentBodyOffset = parentTagRegion.getOffset()
								+ parentTagRegion.getLength();
					}
				}
			}

			return results;
		} finally {
			if (tagPartitioner != null) {
				tagPartitioner.disconnect();
			}
			if (attrPartitioner != null) {
				attrPartitioner.disconnect();
			}
		}
	}

	protected ElementRegion getDoctype(final IDocument document) {
		IDocumentPartitioner partitioner = null;
		try {
			ElementRegion elementRegion = null;

			final String[] tags = new String[] { DOCTYPE, DOCTYPE_LOWER_CASE,
					COMMENT_TOKEN };

			// create tag partitioner
			partitioner = createTagPartitioner(document, tags);
			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(0,
					document.getLength());

			for (ITypedRegion tagRegion : tagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())
						&& !COMMENT_TOKEN.equals(tagRegion.getType())) {
					try {
						elementRegion = new ElementRegion(null, document.get(
								tagRegion.getOffset(), tagRegion.getLength()),
								tagRegion.getOffset());
						break;
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
			return elementRegion;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	private static class AttributeDetector implements IWordDetector {
		private char prevChar;

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			boolean isPart = prevChar != '='
					&& (c == '=' || Character.isWhitespace(c));
			prevChar = c;
			return isPart;
		}
	}
}
