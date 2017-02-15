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

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Assert;
import org.junit.Test;

public class ActionMethodProposalComparatorTest {
	private ActionMethodProposalComparator comparator = new ActionMethodProposalComparator();

	@Test
	public void testCompare() throws Exception {
		ICompletionProposal p1 = new CompletionProposal("", 0, 10, 0);
		ICompletionProposal p2 = new CompletionProposal("", 0, 10, 0);

		int compr = comparator.compare(p1, p2);
		Assert.assertEquals(0, compr);
	}

	@Test
	public void testCompare2() throws Exception {
		ICompletionProposal p1 = new CompletionProposal("getA", 0, 10, 0);
		ICompletionProposal p2 = new CompletionProposal("a", 0, 10, 0);

		int compr = comparator.compare(p1, p2);
		Assert.assertTrue(compr > 0);
	}

	@Test
	public void testCompare3() throws Exception {
		ICompletionProposal p1 = new CompletionProposal("a", 0, 10, 0);
		ICompletionProposal p2 = new CompletionProposal("getB", 0, 10, 0);

		int compr = comparator.compare(p1, p2);
		Assert.assertTrue(compr < 0);
	}

	@Test
	public void testCompare4() throws Exception {
		ICompletionProposal p1 = new CompletionProposal("getA", 0, 10, 0);
		ICompletionProposal p2 = new CompletionProposal("getA", 0, 10, 0);

		int compr = comparator.compare(p1, p2);
		Assert.assertEquals(0, compr);
	}
}
