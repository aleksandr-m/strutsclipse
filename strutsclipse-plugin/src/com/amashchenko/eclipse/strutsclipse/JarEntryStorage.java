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

import java.io.InputStream;
import java.util.Scanner;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class JarEntryStorage implements IStorage {
	private final IPath fFullPath;
	private final IJarEntryResource fJarEntryResource;

	public JarEntryStorage(IPath fullPath, IJarEntryResource jarEntryResource) {
		fFullPath = fullPath;
		fJarEntryResource = jarEntryResource;
	}

	public IDocument toDocument() {
		IDocument document = null;
		try (Scanner scanner = new Scanner(fJarEntryResource.getContents())) {
			scanner.useDelimiter("\\A");
			String str = scanner.hasNext() ? scanner.next() : null;
			if (str != null) {
				document = new Document(str);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return document;
	}

	@Override
	public InputStream getContents() throws CoreException {
		return fJarEntryResource.getContents();
	}

	@Override
	public IPath getFullPath() {
		return fFullPath;
	}

	@Override
	public String getName() {
		return fJarEntryResource.getName();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
}
