package com.amashchenko.eclipse.strutsclipse;

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
	public void testPackageTag() throws Exception {
		final String attrValue = "tiles-d";
		final String content = "<package extends=\"" + attrValue
				+ "\"></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content.lastIndexOf("\"");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(1, proposals.size());

		for (ICompletionProposal p : proposals) {
			Assert.assertTrue(p.getDisplayString().startsWith(attrValue));
		}
	}

	@Test
	public void testPackageTag2() throws Exception {
		final String attrValue = "tiles-default, struts-default, json-default";
		final String content = "<package extends=\"" + attrValue
				+ "\"></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content
				.indexOf("efault, struts-default, json-default");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(1, proposals.size());

		for (ICompletionProposal p : proposals) {
			Assert.assertTrue(p.getDisplayString().startsWith("tiles-default"));
		}
	}

	@Test
	public void testPackageTag3() throws Exception {
		final String attrValue = "tiles-default, struts-default, json-default";
		final String content = "<package extends=\"" + attrValue
				+ "\"></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content
				.indexOf("ts-default, json-default");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(3, proposals.size());

		for (ICompletionProposal p : proposals) {
			Assert.assertTrue(p.getDisplayString().startsWith("stru"));
		}
	}

	@Test
	public void testPackageTag4() throws Exception {
		final String attrValue = "tiles-default, struts-default, json-default";
		final String content = "<package extends=\"" + attrValue
				+ "\"></package>";
		IDocument document = new Document(content);

		final int invocationOffset = content.indexOf("son-default");

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(
				new MockTextViewer(document), invocationOffset);

		List<ICompletionProposal> proposals = computer
				.computeCompletionProposals(context, null);

		Assert.assertNotNull(proposals);
		Assert.assertFalse(proposals.isEmpty());

		Assert.assertEquals(2, proposals.size());

		for (ICompletionProposal p : proposals) {
			Assert.assertTrue(p.getDisplayString().startsWith("j"));
		}
	}
}
