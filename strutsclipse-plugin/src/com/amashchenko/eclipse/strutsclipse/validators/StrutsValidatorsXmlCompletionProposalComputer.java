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
package com.amashchenko.eclipse.strutsclipse.validators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.CompletionProposalHelper;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsValidatorsXmlCompletionProposalComputer implements
		ICompletionProposalComputer, StrutsValidatorsXmlLocations {
	private final StrutsValidatorsXmlParser strutsValidatorsXmlParser;

	public StrutsValidatorsXmlCompletionProposalComputer() {
		strutsValidatorsXmlParser = new StrutsValidatorsXmlParser();
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		final TagRegion tagRegion = strutsValidatorsXmlParser.getTagRegion(
				context.getDocument(), context.getInvocationOffset());
		List<ICompletionProposal> proposals = null;
		String[][] proposalsData = null;

		IRegion proposalRegion = null;
		String elementValuePrefix = null;
		String elementValue = null;
		String multiValueSeparator = null;

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			proposalRegion = tagRegion.getCurrentElement().getValueRegion();
			elementValuePrefix = tagRegion.getCurrentElementValuePrefix();
			elementValue = tagRegion.getCurrentElement().getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case FIELD_VALIDATOR_TYPE:
			case VALIDATOR_TYPE:
				proposalsData = StrutsValidatorsXmlConstants.DEFAULT_VALIDATORS;
				break;
			}
		}

		if (proposals == null && proposalsData != null) {
			proposals = CompletionProposalHelper.createAttrCompletionProposals(
					proposalsData, elementValuePrefix, proposalRegion,
					multiValueSeparator, elementValue, null);
		}
		if (proposals == null) {
			proposals = new ArrayList<ICompletionProposal>();
		}

		return proposals;
	}

	@Override
	public List<IContextInformation> computeContextInformation(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionStarted() {
	}

	@Override
	public void sessionEnded() {
	}
}
