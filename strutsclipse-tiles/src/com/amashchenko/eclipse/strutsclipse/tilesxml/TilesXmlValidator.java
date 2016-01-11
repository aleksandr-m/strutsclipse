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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.validation.AbstractValidator;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;
import org.eclipse.wst.validation.ValidatorMessage;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TilesXmlParser;

public class TilesXmlValidator extends AbstractValidator {
	private static final String PROBLEM_MARKER_ID = "com.amashchenko.eclipse.strutsclipse.tilesxmlproblemmarker";

	private static final String DUP_DEFINITION_MESSAGE_TEXT = "Duplicate definition name.";

	private final TilesXmlParser tilesXmlParser;

	public TilesXmlValidator() {
		tilesXmlParser = new TilesXmlParser();
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

			Map<String, ElementRegion> dupDefnNameCheckMap = new HashMap<String, ElementRegion>();
			List<String> reportedDefinitions = new ArrayList<String>();

			for (ElementRegion pregion : definitionNameRegions) {
				if (dupDefnNameCheckMap.containsKey(pregion.getValue())) {
					result.add(createMessage(resource,
							pregion.getValueRegion(), IMarker.SEVERITY_WARNING,
							DUP_DEFINITION_MESSAGE_TEXT));

					if (!reportedDefinitions.contains(pregion.getValue())) {
						reportedDefinitions.add(pregion.getValue());
						result.add(createMessage(resource, dupDefnNameCheckMap
								.get(pregion.getValue()).getValueRegion(),
								IMarker.SEVERITY_WARNING,
								DUP_DEFINITION_MESSAGE_TEXT));
					}
				} else {
					dupDefnNameCheckMap.put(pregion.getValue(), pregion);
				}
			}
		}

		return result;
	}

	private ValidatorMessage createMessage(IResource resource, IRegion region,
			int severity, String text) {
		ValidatorMessage message = ValidatorMessage.create(text, resource);
		message.setType(PROBLEM_MARKER_ID);
		message.setAttribute(IMarker.SEVERITY, severity);
		message.setAttribute(IMarker.CHAR_START, region.getOffset());
		message.setAttribute(IMarker.CHAR_END,
				region.getOffset() + region.getLength());
		return message;
	}
}
