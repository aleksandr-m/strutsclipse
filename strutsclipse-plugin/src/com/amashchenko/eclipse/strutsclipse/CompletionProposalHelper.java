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
package com.amashchenko.eclipse.strutsclipse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public class CompletionProposalHelper {
	public CompletionProposalHelper() {
	}

	public static List<ICompletionProposal> createAttrCompletionProposals(
			String[][] proposalsData, String prefix, IRegion region,
			String valueSeparator, String attrvalue,
			CompletionProposalComparator proposalComparator) {
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		if (proposalsData != null && region != null) {
			ElementRegion parsedValue = ParseUtil.parseElementValue(attrvalue,
					prefix, valueSeparator, region.getOffset());

			List<String> excludes = new ArrayList<String>();
			// multivalue
			if (valueSeparator != null && !valueSeparator.isEmpty()
					&& attrvalue.contains(valueSeparator)) {
				// exclude already defined values except current value
				String[] valArr = attrvalue.split(valueSeparator);
				for (String val : valArr) {
					if (!parsedValue.getValue().equalsIgnoreCase(val.trim())) {
						excludes.add(val.trim());
					}
				}
			}

			String prefixLowCase = parsedValue.getName().toLowerCase(
					Locale.ROOT);

			for (String[] proposal : proposalsData) {
				String propLowCase = proposal[0].toLowerCase(Locale.ROOT);
				if (!excludes.contains(proposal[0])
						&& (propLowCase.startsWith(prefixLowCase))) {
					list.add(new CompletionProposal(proposal[0], parsedValue
							.getValueRegion().getOffset(), parsedValue
							.getValueRegion().getLength(),
							proposal[0].length(), null, null, null, proposal[1]));
				}
			}
		}

		if (proposalComparator != null) {
			Collections.sort(list, proposalComparator);
		}

		return list;
	}

	public static String[][] proposalDataFromSet(Set<String> set) {
		String[][] proposals = null;
		if (set != null && !set.isEmpty()) {
			proposals = new String[set.size()][2];
			int indx = 0;
			for (String p : set) {
				proposals[indx++][0] = p;
			}
		}
		return proposals;
	}

	public static String[][] proposalDataFromList(List<String[]> list) {
		String[][] proposals = null;
		if (list != null && !list.isEmpty()) {
			proposals = new String[list.size()][2];
			int indx = 0;
			for (String[] p : list) {
				proposals[indx][0] = p[0];
				proposals[indx++][1] = p[1];
			}
		}
		return proposals;
	}

	public static String[][] proposalDataFromMap(Map<String, String> map) {
		String[][] proposals = null;
		if (map != null && !map.isEmpty()) {
			proposals = new String[map.size()][2];
			int indx = 0;
			for (Entry<String, String> entr : map.entrySet()) {
				proposals[indx][0] = entr.getKey();
				proposals[indx++][1] = entr.getValue();
			}
		}
		return proposals;
	}
}
