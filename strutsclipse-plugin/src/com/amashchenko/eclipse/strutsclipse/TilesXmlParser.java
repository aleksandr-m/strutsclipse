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

import java.util.HashSet;
import java.util.Set;

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

public class TilesXmlParser {
	private static final String DOUBLE_QUOTES_TOKEN = "double_quotes_token";
	private static final String SINGLE_QUOTES_TOKEN = "single_quotes_token";

	private static final String[] TAGS = { "definition" };
	private static final String[] ATTRS = { "name", DOUBLE_QUOTES_TOKEN,
			SINGLE_QUOTES_TOKEN };

	private TilesXmlParser() {
	}

	public static Set<String> getDefinitionNames(final IDocument document) {
		IDocumentPartitioner partitioner = null;
		Set<String> result = new HashSet<String>();
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

			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(0,
					document.getLength());

			// create attribute partitioner
			IPredicateRule[] attrRules = new IPredicateRule[ATTRS.length];
			for (int i = 0; i < ATTRS.length; i++) {
				if (DOUBLE_QUOTES_TOKEN.equals(ATTRS[i])) {
					attrRules[i] = new SingleLineRule("\"", "\"", new Token(
							ATTRS[i]));
				} else if (SINGLE_QUOTES_TOKEN.equals(ATTRS[i])) {
					attrRules[i] = new SingleLineRule("'", "'", new Token(
							ATTRS[i]));
				} else {
					attrRules[i] = new MultiLineRule(ATTRS[i], "=", new Token(
							ATTRS[i]));
				}
			}

			scanner.setPredicateRules(attrRules);
			partitioner = new FastPartitioner(scanner, ATTRS);
			partitioner.connect(document);

			for (ITypedRegion tagRegion : tagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())) {
					ITypedRegion[] regions = partitioner.computePartitioning(
							tagRegion.getOffset(), tagRegion.getLength());
					if (regions != null) {
						String attrKey = null;
						for (ITypedRegion r : regions) {
							// only legal types
							if (!IDocument.DEFAULT_CONTENT_TYPE.equals(r
									.getType())) {
								boolean quotesToken = DOUBLE_QUOTES_TOKEN
										.equals(r.getType())
										|| SINGLE_QUOTES_TOKEN.equals(r
												.getType());

								if (attrKey != null && quotesToken) {
									try {
										// get value w/o quotes
										String val = document.get(
												r.getOffset() + 1,
												r.getLength() - 2);

										result.add(val);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
									// set key to null
									attrKey = null;
								} else if (!quotesToken) {
									attrKey = r.getType();
								}
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

	public static IRegion getDefinitionRegion(final IDocument document,
			final String definitionName) {
		IDocumentPartitioner partitioner = null;
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

			// get tags regions
			ITypedRegion[] tagRegions = partitioner.computePartitioning(0,
					document.getLength());

			// create attribute partitioner
			IPredicateRule[] attrRules = new IPredicateRule[ATTRS.length];
			for (int i = 0; i < ATTRS.length; i++) {
				if (DOUBLE_QUOTES_TOKEN.equals(ATTRS[i])) {
					attrRules[i] = new SingleLineRule("\"", "\"", new Token(
							ATTRS[i]));
				} else if (SINGLE_QUOTES_TOKEN.equals(ATTRS[i])) {
					attrRules[i] = new SingleLineRule("'", "'", new Token(
							ATTRS[i]));
				} else {
					attrRules[i] = new MultiLineRule(ATTRS[i], "=", new Token(
							ATTRS[i]));
				}
			}

			scanner.setPredicateRules(attrRules);
			partitioner = new FastPartitioner(scanner, ATTRS);
			partitioner.connect(document);

			for (ITypedRegion tagRegion : tagRegions) {
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())) {
					ITypedRegion[] regions = partitioner.computePartitioning(
							tagRegion.getOffset(), tagRegion.getLength());
					if (regions != null) {
						String attrKey = null;
						for (ITypedRegion r : regions) {
							// only legal types
							if (!IDocument.DEFAULT_CONTENT_TYPE.equals(r
									.getType())) {
								boolean quotesToken = DOUBLE_QUOTES_TOKEN
										.equals(r.getType())
										|| SINGLE_QUOTES_TOKEN.equals(r
												.getType());

								if (attrKey != null && quotesToken) {
									try {
										// get value w/o quotes
										String val = document.get(
												r.getOffset() + 1,
												r.getLength() - 2);

										// definition found return it
										if (val.equals(definitionName)) {
											return new Region(
													r.getOffset() + 1,
													r.getLength() - 2);
										}
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
									// set key to null
									attrKey = null;
								} else if (!quotesToken) {
									attrKey = r.getType();
								}
							}
						}
					}
				}
			}

			return null;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}
}
