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
package com.amashchenko.eclipse.strutsclipse;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
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

	public static class JavaElementHyperlink implements IHyperlink {
		private final IJavaElement fElement;
		private final IRegion fRegion;

		public JavaElementHyperlink(IRegion region, IJavaElement element) {
			fRegion = region;
			fElement = element;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			String name = null;
			if (fElement != null) {
				if (fElement.getParent() == null) {
					name = fElement.getElementName();
				} else {
					name = fElement.getParent().getElementName() + "#"
							+ fElement.getElementName();
				}
			}
			return name;
		}

		@Override
		public String getTypeLabel() {
			return null;
		}

		@Override
		public void open() {
			try {
				JavaUI.openInEditor(fElement);
			} catch (PartInitException e) {
			} catch (JavaModelException e) {
			}
		}
	}

	public static class StorageHyperlink implements IHyperlink {
		private final IStorage fStorage;
		private final IRegion fRegion;
		private final IRegion fHighlightRange;

		public StorageHyperlink(IRegion region, IStorage storage, IRegion range) {
			fRegion = region;
			fStorage = storage;
			fHighlightRange = range;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return fStorage == null ? null : fStorage.getFullPath().toString();
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

				IEditorDescriptor editorDescriptor = IDE
						.getEditorDescriptor(fStorage.getName());

				IEditorPart editor = page.openEditor(new StorageEditorInput(
						fStorage), editorDescriptor.getId());

				selectAndReveal(editor, fHighlightRange);
			} catch (PartInitException e) {
			}
		}
	}

	private static class StorageEditorInput implements IStorageEditorInput {
		private final IStorage fStorage;

		private StorageEditorInput(IStorage storage) {
			fStorage = storage;
		}

		@Override
		public boolean exists() {
			return fStorage != null;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return fStorage.getName();
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public IStorage getStorage() {
			return fStorage;
		}

		@Override
		public String getToolTipText() {
			return fStorage.getFullPath() != null ? fStorage.getFullPath()
					.toString() : fStorage.getName();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StorageEditorInput) {
				return fStorage.equals(((StorageEditorInput) obj).fStorage);
			}
			return super.equals(obj);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object getAdapter(Class adapter) {
			return null;
		}
	}
}
