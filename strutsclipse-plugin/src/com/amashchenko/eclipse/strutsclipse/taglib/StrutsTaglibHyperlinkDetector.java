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
package com.amashchenko.eclipse.strutsclipse.taglib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.amashchenko.eclipse.strutsclipse.AbstractStrutsHyperlinkDetector;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlConstants;
import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibHyperlinkDetector extends
		AbstractStrutsHyperlinkDetector implements StrutsTaglibLocations {
	private final StrutsTaglibParser strutsTaglibParser;
	private final StrutsXmlParser strutsXmlParser;

	public StrutsTaglibHyperlinkDetector() {
		strutsTaglibParser = new StrutsTaglibParser();
		strutsXmlParser = new StrutsXmlParser();
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		List<IHyperlink> linksList = new ArrayList<IHyperlink>();

		final TagRegion tagRegion = strutsTaglibParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case URL_ACTION:
			case FORM_ACTION:
			case LINK_ACTION:
				linksList.addAll(createActionLinks(document, elementValue,
						elementRegion, tagRegion.getAttrValue(
								StrutsTaglibConstants.NAMESPACE_ATTR, null)));
				break;
			}
		}

		return linksListToArray(linksList);
	}

	private List<IHyperlink> createActionLinks(final IDocument document,
			final String elementValue, final IRegion elementRegion,
			final String namespaceParamValue) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		final Set<String> namespaces = new HashSet<String>();
		String namespace = namespaceParamValue;
		if (namespace == null) {
			namespaces.add("");
			namespaces.add("/");
		} else {
			namespaces.add(namespace);
		}

		try {
			final IDocumentProvider provider = new TextFileDocumentProvider();
			final IJavaProject javaProject = ProjectUtil
					.getCurrentJavaProject(document);
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
								&& "xml".equalsIgnoreCase(resource
										.getFileExtension())
								&& resource
										.getName()
										.toLowerCase(Locale.ROOT)
										.contains(
												StrutsXmlConstants.STRUTS_FILE_NAME)) {
							provider.connect(resource);
							IDocument document = provider.getDocument(resource);
							provider.disconnect(resource);

							IRegion region = strutsXmlParser.getActionRegion(
									document, namespaces, elementValue);
							if (region != null) {
								IFile file = project.getFile(resource
										.getProjectRelativePath());
								if (file.exists()) {
									links.add(new FileHyperlink(elementRegion,
											file, region));
								}
							}
						}
						return true;
					}
				});
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return links;
	}
}
