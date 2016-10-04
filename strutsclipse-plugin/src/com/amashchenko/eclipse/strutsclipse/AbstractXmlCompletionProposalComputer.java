/*
 * Copyright 2015-2016 Aleksandr Mashchenko.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public abstract class AbstractXmlCompletionProposalComputer implements
		ICompletionProposalComputer {
	private static final List<String> JSP_HTML_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "jsp", "html", "htm" });
	private static final List<String> FREEMARKER_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "ftl" });

	protected List<ICompletionProposal> createAttrCompletionProposals(
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

			final String prefixLowCase = parsedValue.getName().toLowerCase(
					Locale.ROOT);

			for (String[] proposal : proposalsData) {
				if (proposal[0].toLowerCase(Locale.ROOT).startsWith(
						prefixLowCase)
						&& !excludes.contains(proposal[0])) {
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

	protected String[][] proposalDataFromSet(Set<String> set) {
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

	protected String[][] proposalDataFromList(List<String[]> list) {
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

	protected Set<String> findJspHtmlFilesPaths(final IDocument currentDocument) {
		return findFilesPaths(currentDocument, JSP_HTML_FILE_EXTENSIONS);
	}

	protected Set<String> findFreeMarkerFilesPaths(
			final IDocument currentDocument) {
		return findFilesPaths(currentDocument, FREEMARKER_FILE_EXTENSIONS);
	}

	private Set<String> findFilesPaths(final IDocument currentDocument,
			final List<String> extensions) {
		final Set<String> paths = new HashSet<String>();
		try {
			IProject project = ProjectUtil.getCurrentProject(currentDocument);
			if (project != null && project.exists()) {
				IVirtualComponent rootComponent = ComponentCore
						.createComponent(project);
				final IVirtualFolder rootFolder = rootComponent.getRootFolder();

				rootFolder.getUnderlyingResource().accept(
						new IResourceVisitor() {
							@Override
							public boolean visit(IResource resource)
									throws CoreException {
								if (resource.isAccessible()
										&& resource.getType() == IResource.FILE
										&& extensions.contains(resource
												.getFileExtension()
												.toLowerCase(Locale.ROOT))) {
									IPath path = resource
											.getProjectRelativePath()
											.makeRelativeTo(
													rootFolder
															.getProjectRelativePath())
											.makeAbsolute();

									paths.add(path.toString());
								}
								return true;
							}
						});
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return paths;
	}
}
