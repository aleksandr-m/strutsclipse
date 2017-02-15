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
package com.amashchenko.eclipse.strutsclipse;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractStrutsHyperlinkDetector extends
		AbstractHyperlinkDetector {

	protected IHyperlink[] linksListToArray(List<IHyperlink> linksList) {
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

	// helper method for IHyperlink-s
	public static void selectAndReveal(IEditorPart editorPart,
			IRegion highlightRange) {
		ITextEditor textEditor = null;

		if (editorPart instanceof MultiPageEditorPart) {
			MultiPageEditorPart part = (MultiPageEditorPart) editorPart;

			Object editorPage = part.getSelectedPage();
			if (editorPage != null && editorPage instanceof ITextEditor) {
				textEditor = (ITextEditor) editorPage;
			}
		} else if (editorPart instanceof ITextEditor) {
			textEditor = (ITextEditor) editorPart;
		}

		// highlight range in editor if possible
		if (highlightRange != null && textEditor != null) {
			textEditor.selectAndReveal(highlightRange.getOffset(),
					highlightRange.getLength());
		}
	}

	public static class FileHyperlink implements IHyperlink {
		private final IFile fFile;
		private final IRegion fRegion;
		private final IRegion fHighlightRange;

		public FileHyperlink(IRegion region, IFile file) {
			this(region, file, null);
		}

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

				selectAndReveal(editor, fHighlightRange);
			} catch (PartInitException e) {
			}
		}
	}
}
