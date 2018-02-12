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
package com.amashchenko.eclipse.strutsclipse;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class PropertiesParser {
	private static final String KEY = "key_token";
	private static final IToken KEY_TOKEN = new Token(KEY);

	public IRegion getKeyRegion(IDocument document, final String searchKey) {
		IPredicateRule[] rules = new IPredicateRule[4];
		rules[0] = new EndOfLineRule("#", Token.UNDEFINED);
		rules[1] = new EndOfLineRule("!", Token.UNDEFINED);
		rules[2] = new WordPatternRule(new PropertyKeyDetector(), "\n", "=",
				KEY_TOKEN);
		rules[3] = new WordPatternRule(new PropertyKeyDetector(), "\n", ":",
				KEY_TOKEN);

		RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
		scanner.setPredicateRules(rules);

		IDocumentPartitioner partitioner = new FastPartitioner(scanner,
				new String[] { KEY });

		IRegion region = null;
		try {
			partitioner.connect(document);

			ITypedRegion[] tagRegions = partitioner.computePartitioning(0,
					document.getLength());

			boolean firstRegion = true;
			for (ITypedRegion tagRegion : tagRegions) {
				if (KEY.equals(tagRegion.getType()) || firstRegion) {
					String key = null;
					try {
						// -1 remove =
						key = document.get(tagRegion.getOffset(),
								tagRegion.getLength() - 1);
						key = key.trim();
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					// first line isn't caught by the key_token rule
					if (firstRegion && key != null) {
						int indx = key.indexOf('=');
						if (indx != -1) {
							key = key.substring(0, indx).trim();
						}
					}

					if (key != null) {
						key = key.replace("\\ ", " ");
						if (key.equals(searchKey)) {
							region = tagRegion;
							break;
						}
					}
				}
				firstRegion = false;
			}
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
		return region;
	}

	private static class PropertyKeyDetector implements IWordDetector {
		private char prevChar;

		@Override
		public boolean isWordStart(char c) {
			return false;
		}

		@Override
		public boolean isWordPart(char c) {
			boolean res = Character.MAX_VALUE != c && c != '\r' && c != '\n'
					&& prevChar != '=' && prevChar != ':';
			prevChar = c;
			return res;
		}
	}
}
