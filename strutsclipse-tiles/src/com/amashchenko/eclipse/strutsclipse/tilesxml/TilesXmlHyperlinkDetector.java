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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TilesXmlParser;

public class TilesXmlHyperlinkDetector extends AbstractHyperlinkDetector
		implements TilesXmlLocations {
	private final TilesXmlParser tilesXmlParser;

	public TilesXmlHyperlinkDetector() {
		tilesXmlParser = new TilesXmlParser();
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		List<IHyperlink> linksList = new ArrayList<IHyperlink>();

		final TagRegion tagRegion = tilesXmlParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case DEFINITION_EXTENDS:
				linksList.addAll(createDefinitionLocationLinks(document,
						elementValue, elementRegion));
				break;
			}
		}

		IHyperlink[] links = null;
		if (linksList != null && !linksList.isEmpty()) {
			// remove null-s
			Iterator<IHyperlink> itr = linksList.iterator();
			while (itr.hasNext()) {
				if (itr.next() == null) {
					itr.remove();
				}
			}

			if (!linksList.isEmpty()) {
				links = linksList.toArray(new IHyperlink[linksList.size()]);
			}
		}

		return links;
	}

	private List<IHyperlink> createDefinitionLocationLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		IRegion region = tilesXmlParser.getDefinitionRegion(document,
				elementValue);
		if (region != null) {
			ITextFileBuffer textFileBuffer = FileBuffers
					.getTextFileBufferManager().getTextFileBuffer(document);
			if (textFileBuffer != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(textFileBuffer.getLocation());
				if (file.exists()) {
					links.add(new FileHyperlink(elementRegion, file, region));
				}
			}
		}

		return links;
	}

	private static class FileHyperlink implements IHyperlink {
		private final IFile fFile;
		private final IRegion fRegion;
		private final IRegion fHighlightRange;

		public FileHyperlink(IRegion region, IFile file, IRegion range) {
			fRegion = region;
			fFile = file;
			fHighlightRange = range;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return fFile == null ? null : fFile.getProjectRelativePath()
					.toString();
		}

		@Override
		public String getTypeLabel() {
			return null;
		}

		@Override
		public void open() {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IEditorPart editor = IDE.openEditor(page, fFile, true);

				ITextEditor textEditor = null;

				if (editor instanceof MultiPageEditorPart) {
					MultiPageEditorPart part = (MultiPageEditorPart) editor;

					Object editorPage = part.getSelectedPage();
					if (editorPage != null && editorPage instanceof ITextEditor) {
						textEditor = (ITextEditor) editorPage;
					}
				} else if (editor instanceof ITextEditor) {
					textEditor = (ITextEditor) editor;
				}

				// highlight range in editor if possible
				if (fHighlightRange != null && textEditor != null) {
					textEditor.selectAndReveal(fHighlightRange.getOffset(),
							fHighlightRange.getLength());
				}
			} catch (PartInitException e) {
			}
		}
	}
}
