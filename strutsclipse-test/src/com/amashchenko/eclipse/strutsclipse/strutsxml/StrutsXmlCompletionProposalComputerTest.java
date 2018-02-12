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
package com.amashchenko.eclipse.strutsclipse.strutsxml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.junit.Assert;
import org.junit.Test;

import com.amashchenko.eclipse.strutsclipse.mock.MockTextViewer;

public class StrutsXmlCompletionProposalComputerTest {
	private StrutsXmlCompletionProposalComputer computer = new StrutsXmlCompletionProposalComputer();

	@Test
	public void testActionTagName() throws Exception {
		final String content = "<action name=\"\"></action>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(StrutsXmlConstants.DEFAULT_METHODS.length,
				proposals.size());
	}

	@Test
	public void testActionTagMethod() throws Exception {
		final String content = "<action method=\"\"></action>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(StrutsXmlConstants.DEFAULT_METHODS.length,
				proposals.size());
	}

	@Test
	public void testResultTagName() throws Exception {
		final String content = "<result name=\"\"></result>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(StrutsXmlConstants.DEFAULT_RESULT_NAMES.length,
				proposals.size());
	}

	@Test
	public void testResultTagType() throws Exception {
		final String content = "<result type=\"\"></result>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(StrutsXmlConstants.DEFAULT_RESULT_TYPES.length,
				proposals.size());
	}

	@Test
	public void testResultTagTypeUpperCase() throws Exception {
		final String content = "<result type=\"REDIRECTA\"></result>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(1, proposals.size());
		Assert.assertEquals(StrutsXmlConstants.REDIRECT_ACTION_RESULT,
				proposals.get(0).getDisplayString());
	}

	// result body redirectAction proposals
	@Test
	public void testResultTagBody() throws Exception {
		final String thisActionName = "this";
		final String samePackActionName = "same";
		final String otherPackActionName = "other";
		final String otherNamespaceActionName = "othersome";

		final String content = "<package namespace='/'><action name='"
				+ thisActionName + "'><result type='"
				+ StrutsXmlConstants.REDIRECT_ACTION_RESULT
				+ "'></result></action><action name='" + samePackActionName
				+ "'></action></package>"
				+ "<package namespace='/'><action name='" + otherPackActionName
				+ "'></action></package>"
				+ "<package namespace='/some'><action name='"
				+ otherNamespaceActionName + "'></action></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("</result");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());
		Assert.assertEquals(3, proposals.size());

		List<String> list = new ArrayList<String>();
		for (ICompletionProposal p : proposals) {
			list.add(p.getDisplayString());
		}

		Assert.assertTrue(list.contains(thisActionName));
		Assert.assertTrue(list.contains(samePackActionName));
		Assert.assertTrue(list.contains(otherPackActionName));
		Assert.assertFalse(list.contains(otherNamespaceActionName));
	}

	@Test
	public void testResultTagParamBody() throws Exception {
		final String thisActionName = "this";
		final String samePackActionName = "same";
		final String otherPackActionName = "other";
		final String otherNamespaceActionName = "othersome";

		final String content = "<package namespace='/'><action name='"
				+ thisActionName + "'><result type='"
				+ StrutsXmlConstants.REDIRECT_ACTION_RESULT + "'><param name='"
				+ StrutsXmlConstants.ACTION_NAME_PARAM
				+ "'></param></result></action><action name='"
				+ samePackActionName + "'></action></package>"
				+ "<package namespace='/'><action name='" + otherPackActionName
				+ "'></action></package>"
				+ "<package namespace='/some'><action name='"
				+ otherNamespaceActionName + "'></action></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("</param");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());
		Assert.assertEquals(3, proposals.size());

		List<String> list = new ArrayList<String>();
		for (ICompletionProposal p : proposals) {
			list.add(p.getDisplayString());
		}

		Assert.assertTrue(list.contains(thisActionName));
		Assert.assertTrue(list.contains(samePackActionName));
		Assert.assertTrue(list.contains(otherPackActionName));
		Assert.assertFalse(list.contains(otherNamespaceActionName));
	}

	@Test
	public void testResultTagParamBodyWrongName() throws Exception {
		final String thisActionName = "this";
		final String samePackActionName = "same";
		final String otherPackActionName = "other";
		final String otherNamespaceActionName = "othersome";

		final String content = "<package namespace='/'><action name='"
				+ thisActionName + "'><result type='"
				+ StrutsXmlConstants.REDIRECT_ACTION_RESULT + "'><param name='"
				+ StrutsXmlConstants.LOCATION_PARAM
				+ "'></param></result></action><action name='"
				+ samePackActionName + "'></action></package>"
				+ "<package namespace='/'><action name='" + otherPackActionName
				+ "'></action></package>"
				+ "<package namespace='/some'><action name='"
				+ otherNamespaceActionName + "'></action></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("</param");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertTrue(proposals.isEmpty());
	}

	@Test
	public void testResultTagParamBodyWrongName2() throws Exception {
		final String thisActionName = "this";
		final String samePackActionName = "same";
		final String otherPackActionName = "other";
		final String otherNamespaceActionName = "othersome";

		final String content = "<package namespace='/'><action name='"
				+ thisActionName
				+ "'><result type='"
				+ StrutsXmlConstants.REDIRECT_ACTION_RESULT
				+ "'><param name='wrong'></param></result></action><action name='"
				+ samePackActionName + "'></action></package>"
				+ "<package namespace='/'><action name='" + otherPackActionName
				+ "'></action></package>"
				+ "<package namespace='/some'><action name='"
				+ otherNamespaceActionName + "'></action></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("</param");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertTrue(proposals.isEmpty());
	}

	@Test
	public void testConstantTagName() throws Exception {
		final String content = "<constant name=\"\" />";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(StrutsXmlConstants.DEFAULT_CONSTANTS.length,
				proposals.size());
	}

	@Test
	public void testBeanTagScope() throws Exception {
		final String content = "<bean scope=\"\" />";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(StrutsXmlConstants.DEFAULT_BEAN_SCOPES.length,
				proposals.size());
	}
}
