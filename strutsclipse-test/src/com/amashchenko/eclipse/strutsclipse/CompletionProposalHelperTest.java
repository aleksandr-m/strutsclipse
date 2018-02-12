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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Assert;
import org.junit.Test;

import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlConstants;

public class CompletionProposalHelperTest {
	@Test
	public void testSimple() throws Exception {
		final String[][] data = { { "some_text", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "some", new Region(0, 4),
						null, "some", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[0][0], results.get(0).getDisplayString());
	}

	@Test
	public void testSimple2() throws Exception {
		final String[][] data = { { "some_text", null },
				{ "another_some_text", null },
				{ "yet_another_some_text", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "anot", new Region(0, 4),
						null, "anot", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[1][0], results.get(0).getDisplayString());
	}

	@Test
	public void testSimpleEmpty() throws Exception {
		final String[][] data = { { "some_text", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "", new Region(0, 0),
						null, "", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[0][0], results.get(0).getDisplayString());
	}

	@Test
	public void testSimpleNotMatch() throws Exception {
		final String[][] data = { { "some_text", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "not_in_list", new Region(
						0, 11), null, "not_in_list", null);
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}

	@Test
	public void testMultipleMatches() throws Exception {
		final String[][] data = { { "some_text", null },
				{ "another_some_text", null },
				{ "some_yet_another_text", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "some", new Region(0, 4),
						null, "some", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(data[0][0], results.get(0).getDisplayString());
		Assert.assertEquals(data[2][0], results.get(1).getDisplayString());
	}

	@Test
	public void testMultiValue() throws Exception {
		final String[][] data = { { "first", null }, { "second", null },
				{ "third", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "first, second, ",
						new Region(0, 14),
						StrutsXmlConstants.MULTI_VALUE_SEPARATOR,
						"first, second, ", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[2][0], results.get(0).getDisplayString());
	}

	@Test
	public void testPathLikeWithSlash() throws Exception {
		final String[][] data = { { "/some/path/file", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "/path", new Region(0, 5),
						null, "/path", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[0][0], results.get(0).getDisplayString());
	}

	@Test
	public void testPathLikeWithoutSlash() throws Exception {
		final String[][] data = { { "/some/path/file", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "path", new Region(0, 5),
						null, "path", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[0][0], results.get(0).getDisplayString());
	}

	@Test
	public void testPathLikeEmpty() throws Exception {
		final String[][] data = { { "/some/path/file", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "", new Region(0, 0),
						null, "", null);
		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(data[0][0], results.get(0).getDisplayString());
	}

	@Test
	public void testPathLikeNotMatch() throws Exception {
		final String[][] data = { { "/some/path/file", null } };
		List<ICompletionProposal> results = CompletionProposalHelper
				.createAttrCompletionProposals(data, "not_in_list", new Region(
						0, 11), null, "not_in_list", null);
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}

	// proposalDataFromSet
	@Test
	public void testProposalDataFromSet() throws Exception {
		Set<String> set = new HashSet<String>();
		set.add("first");
		set.add("second");
		set.add("third");
		String[][] proposals = CompletionProposalHelper
				.proposalDataFromSet(set);
		Assert.assertNotNull(proposals);
		Assert.assertEquals(set.size(), proposals.length);
		Assert.assertEquals(2, proposals[0].length);
	}

	@Test
	public void testProposalDataFromSetEmpty() throws Exception {
		Assert.assertNull(CompletionProposalHelper
				.proposalDataFromSet(new HashSet<String>()));
	}

	@Test
	public void testProposalDataFromSetNull() throws Exception {
		Assert.assertNull(CompletionProposalHelper.proposalDataFromSet(null));
	}

	// proposalDataFromList
	@Test
	public void testProposalDataFromList() throws Exception {
		List<String[]> list = new ArrayList<String[]>();
		list.add(new String[] { "first", "info_1" });
		list.add(new String[] { "second", "info_2", "info_2.2" });
		list.add(new String[] { "third", "info_3" });
		String[][] proposals = CompletionProposalHelper
				.proposalDataFromList(list);
		Assert.assertNotNull(proposals);
		Assert.assertEquals(list.size(), proposals.length);
		Assert.assertEquals(2, proposals[1].length);
		Assert.assertEquals(list.get(1)[0], proposals[1][0]);
		Assert.assertEquals(list.get(1)[1], proposals[1][1]);
	}

	@Test
	public void testProposalDataFromListEmpty() throws Exception {
		Assert.assertNull(CompletionProposalHelper
				.proposalDataFromList(new ArrayList<String[]>()));
	}

	@Test
	public void testProposalDataFromListNull() throws Exception {
		Assert.assertNull(CompletionProposalHelper.proposalDataFromList(null));
	}

	// proposalDataFromMap
	@Test
	public void testProposalDataFromMap() throws Exception {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("first", "info_1");
		map.put("second", "info_2");
		map.put("third", "info_3");
		String[][] proposals = CompletionProposalHelper
				.proposalDataFromMap(map);
		Assert.assertNotNull(proposals);
		Assert.assertEquals(map.size(), proposals.length);
		Assert.assertEquals(2, proposals[0].length);
		Assert.assertEquals("third", proposals[2][0]);
		Assert.assertEquals(map.get("third"), proposals[2][1]);
	}

	@Test
	public void testProposalDataFromMapEmpty() throws Exception {
		Assert.assertNull(CompletionProposalHelper
				.proposalDataFromMap(new HashMap<String, String>()));
	}

	@Test
	public void testProposalDataFromMapNull() throws Exception {
		Assert.assertNull(CompletionProposalHelper.proposalDataFromMap(null));
	}
}
