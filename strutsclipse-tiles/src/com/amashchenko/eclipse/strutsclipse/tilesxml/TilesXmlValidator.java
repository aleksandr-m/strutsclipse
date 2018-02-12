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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;

import com.amashchenko.eclipse.strutsclipse.AbstractXmlValidator;
import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public class TilesXmlValidator extends AbstractXmlValidator {
	private static final String PROBLEM_MARKER_ID = "com.amashchenko.eclipse.strutsclipse.tilesxmlproblemmarker";

	private static final String DUP_DEFINITION_MESSAGE_TEXT = "Duplicate definition name.";

	private final TilesXmlParser tilesXmlParser;

	public TilesXmlValidator() {
		tilesXmlParser = new TilesXmlParser();
	}

	@Override
	protected String getProblemMarkerId() {
		return PROBLEM_MARKER_ID;
	}

	@Override
	public ValidationResult validate(IResource resource, int kind,
			ValidationState state, IProgressMonitor monitor) {
		IDocument document = null;
		// get document
		try {
			final IDocumentProvider provider = new TextFileDocumentProvider();
			provider.connect(resource);
			document = provider.getDocument(resource);
			provider.disconnect(resource);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		ValidationResult result = new ValidationResult();
		if (document != null) {
			// validate definitions names
			List<ElementRegion> definitionNameRegions = tilesXmlParser
					.getDefinitionNameRegions(document);
			validateRegions(resource, document, result, definitionNameRegions,
					DUP_DEFINITION_MESSAGE_TEXT, IMarker.SEVERITY_WARNING);
		}

		return result;
	}
}
