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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlConstants;
import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public abstract class AbstractXmlCompletionProposalComputer implements
		ICompletionProposalComputer {
	private static final List<String> JSP_HTML_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "jsp", "html", "htm" });
	private static final List<String> FREEMARKER_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "ftl" });
	private static final String XML_FILE_EXTENSION = "xml";
	private static final String STRUTS_XML_CONTENT_TYPE_ID = "com.amashchenko.eclipse.strutsclipse.strutsxml";
	private static final String TILES_XML_CONTENT_TYPE_ID = "com.amashchenko.eclipse.strutsclipse.tilesxml";

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

	protected List<IDocument> findTilesDocuments(final IDocument currentDocument) {
		return findDocuments(currentDocument, XML_FILE_EXTENSION,
				TILES_XML_CONTENT_TYPE_ID, StrutsXmlConstants.TILES_RESULT);
	}

	protected List<IDocument> findStrutsDocuments(
			final IDocument currentDocument) {
		return findDocuments(currentDocument, XML_FILE_EXTENSION,
				STRUTS_XML_CONTENT_TYPE_ID, StrutsXmlConstants.STRUTS_FILE_NAME);
	}

	/**
	 * Searches project for files with given file extension and content type. If
	 * given content type isn't known by the platform search will check if
	 * resource name contains given file name.
	 * 
	 * @param currentDocument
	 *            Document in the project to search.
	 * @param fileExtension
	 *            File extension search criteria.
	 * @param contentTypeId
	 *            Content type identifier, if the platform doesn't know it then
	 *            <code>fileName</code> parameter will be used.
	 * @param fileName
	 *            File name search criteria.
	 * @return List of the documents meeting the search criteria, or empty list
	 *         if no documents are found.
	 */
	private List<IDocument> findDocuments(final IDocument currentDocument,
			final String fileExtension, final String contentTypeId,
			final String fileName) {
		final List<IDocument> documents = new ArrayList<IDocument>();

		IContentTypeManager contentTypeManager = Platform
				.getContentTypeManager();
		final IContentType contentType = contentTypeManager
				.getContentType(contentTypeId);

		try {
			final IDocumentProvider provider = new TextFileDocumentProvider();
			final IJavaProject javaProject = ProjectUtil
					.getCurrentJavaProject(currentDocument);
			if (javaProject != null && javaProject.exists()) {
				final IProject project = javaProject.getProject();
				final String outputFolder = javaProject.getOutputLocation()
						.makeRelativeTo(project.getFullPath()).segment(0);
				project.accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource)
							throws CoreException {
						// don't visit output folder
						if (resource.getType() == IResource.FOLDER
								&& resource.getProjectRelativePath().segment(0)
										.equals(outputFolder)) {
							return false;
						}
						if (resource.isAccessible()
								&& resource.getType() == IResource.FILE
								&& resource.getFileExtension()
										.equalsIgnoreCase(fileExtension)) {
							boolean addToList = false;
							if (contentType == null) {
								addToList = resource.getName()
										.toLowerCase(Locale.ROOT)
										.contains(fileName);
							} else {
								IFile file = project.getFile(resource
										.getProjectRelativePath());
								IContentDescription descrp = file
										.getContentDescription();
								addToList = descrp.getContentType().isKindOf(
										contentType);
							}

							if (addToList) {
								provider.connect(resource);
								IDocument document = provider
										.getDocument(resource);
								provider.disconnect(resource);

								documents.add(document);
							}
						}
						return true;
					}
				});
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return documents;
	}
}
