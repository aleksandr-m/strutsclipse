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
package com.amashchenko.eclipse.strutsclipse.java;

import java.util.Comparator;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class ActionMethodProposalComparator implements
		Comparator<ICompletionProposal> {
	@Override
	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		int compr = 0;
		if (p1 != null && p1.getDisplayString() != null && p2 != null
				&& p2.getDisplayString() != null) {
			boolean p1IsGet = p1.getDisplayString().startsWith("get");
			boolean p2IsGet = p2.getDisplayString().startsWith("get");

			if (p1IsGet && !p2IsGet) {
				compr = Integer.MAX_VALUE;
			} else if (p2IsGet && !p1IsGet) {
				compr = Integer.MIN_VALUE;
			} else {
				compr = p1.getDisplayString().compareToIgnoreCase(
						p2.getDisplayString());

			}
		}
		return compr;
	}
}