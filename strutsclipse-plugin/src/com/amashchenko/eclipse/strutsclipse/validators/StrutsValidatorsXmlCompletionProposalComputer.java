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
package com.amashchenko.eclipse.strutsclipse.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.CompletionProposalHelper;
import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsValidatorsXmlCompletionProposalComputer implements
		ICompletionProposalComputer, StrutsValidatorsXmlLocations {
	private final StrutsValidatorsXmlParser strutsValidatorsXmlParser;

	private final CompletionProposalComparator proposalComparator;

	public StrutsValidatorsXmlCompletionProposalComputer() {
		strutsValidatorsXmlParser = new StrutsValidatorsXmlParser();
		proposalComparator = new CompletionProposalComparator();
		proposalComparator.setOrderAlphabetically(true);
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
				List<String[]> list = new ArrayList<String[]>();

				// default validators
				JarEntryStorage defaultValidors = ProjectUtil
						.findJarEntryStrutsDefaultValidatorResource(context
								.getDocument());
				if (defaultValidors != null) {
					Set<String> names = strutsValidatorsXmlParser
							.getValidatorsNames(defaultValidors.toDocument());
					for (String s : names) {
						list.add(new String[] {
								s,
								StrutsValidatorsXmlConstants.DEFAULT_VALIDATORS
										.get(s) });
					}
				}

				// local validators
				List<ResourceDocument> resources = ProjectUtil
						.findStrutsValidatorsResources(context.getDocument());
				for (ResourceDocument rd : resources) {
					Set<String> names = strutsValidatorsXmlParser
							.getValidatorsNames(rd.getDocument());
					for (String s : names) {
						list.add(new String[] { s, null });
					}
				}

				if (list.isEmpty()) {
					proposalsData = CompletionProposalHelper
							.proposalDataFromMap(StrutsValidatorsXmlConstants.DEFAULT_VALIDATORS);
				} else {
					proposalsData = CompletionProposalHelper
							.proposalDataFromList(list);
				}

				break;
			}
		}

		if (proposals == null && proposalsData != null) {
			proposals = CompletionProposalHelper.createAttrCompletionProposals(
					proposalsData, elementValuePrefix, proposalRegion,
					multiValueSeparator, elementValue, proposalComparator);
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
