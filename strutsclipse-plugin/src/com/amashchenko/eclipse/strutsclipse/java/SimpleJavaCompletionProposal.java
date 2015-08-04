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
package com.amashchenko.eclipse.strutsclipse.java;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class SimpleJavaCompletionProposal implements IJavaCompletionProposal {
	private final String replacementString;
	private final String displayString;
	private final Image image;
	private final int relevance;

	public SimpleJavaCompletionProposal(CompletionProposal proposal,
			JavaContentAssistInvocationContext context) {
		String declSignature = String.valueOf(proposal
				.getDeclarationSignature());
		String completion = String.valueOf(proposal.getCompletion());
		// add package name
		if (declSignature != null && !completion.startsWith(declSignature)) {
			completion = declSignature + "." + completion;
		}

		replacementString = completion;
		displayString = context.getLabelProvider().createLabel(proposal);
		relevance = proposal.getRelevance();
		image = context.getLabelProvider().createImageDescriptor(proposal)
				.createImage();
	}

	public String getReplacementString() {
		return replacementString;
	}

	@Override
	public String getDisplayString() {
		return displayString;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public int getRelevance() {
		return relevance;
	}

	@Override
	public void apply(IDocument document) {
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
}
