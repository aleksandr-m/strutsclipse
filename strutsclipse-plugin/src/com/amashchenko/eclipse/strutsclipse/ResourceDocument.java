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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

public class ResourceDocument {
	private final IResource resource;
	private final IDocument document;

	public ResourceDocument(IResource resource, IDocument document) {
		this.resource = resource;
		this.document = document;
	}

	public IDocument getDocument() {
		return document;
	}

	public IResource getResource() {
		return resource;
	}
}
