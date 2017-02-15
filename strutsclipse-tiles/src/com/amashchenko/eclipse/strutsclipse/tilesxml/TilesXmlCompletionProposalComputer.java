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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;

import com.amashchenko.eclipse.strutsclipse.AbstractXmlCompletionProposalComputer;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class TilesXmlCompletionProposalComputer extends
		AbstractXmlCompletionProposalComputer implements TilesXmlLocations {
	private final TilesXmlParser tilesXmlParser;

	private final CompletionProposalComparator proposalComparator;

	public TilesXmlCompletionProposalComputer() {
		tilesXmlParser = new TilesXmlParser();
		proposalComparator = new CompletionProposalComparator();
		proposalComparator.setOrderAlphabetically(true);
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		final TagRegion tagRegion = tilesXmlParser.getTagRegion(
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
			case DEFINITION_TEMPLATE:
			case PUT_ATTRIBUTE_VALUE:
				proposalsData = computeFileProposals(context.getDocument());
				break;
			case DEFINITION_EXTENDS:
				proposalsData = computeDefinitionExtendsProposals(
						context.getDocument(), tagRegion.getAttrValue(
								TilesXmlConstants.NAME_ATTR, null));
				break;
			}
		}

		if (proposals == null && proposalsData != null) {
			proposals = createAttrCompletionProposals(proposalsData,
					elementValuePrefix, proposalRegion, multiValueSeparator,
					elementValue, proposalComparator);
		}
		if (proposals == null) {
			proposals = new ArrayList<ICompletionProposal>();
		}

		return proposals;
	}

	private String[][] computeDefinitionExtendsProposals(
			final IDocument document, final String currentDefinitionName) {
		Set<String> definitonNames = tilesXmlParser
				.getDefinitionNames(document);

		// remove current definiton name
		if (currentDefinitionName != null
				&& definitonNames.contains(currentDefinitionName)) {
			definitonNames.remove(currentDefinitionName);
		}

		return proposalDataFromSet(definitonNames);
	}

	private String[][] computeFileProposals(final IDocument document) {
		Set<String> set = ProjectUtil.findJspHtmlFilesPaths(document);
		return proposalDataFromSet(set);
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
