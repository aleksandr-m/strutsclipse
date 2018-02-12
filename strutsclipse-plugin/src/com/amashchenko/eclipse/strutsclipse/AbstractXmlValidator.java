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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.wst.validation.AbstractValidator;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidatorMessage;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public abstract class AbstractXmlValidator extends AbstractValidator {

	protected abstract String getProblemMarkerId();

	protected void validateRegions(IResource resource, IDocument document,
			ValidationResult result, List<ElementRegion> regions,
			String message, int severity) {
		Map<String, ElementRegion> duplicateCheckMap = new HashMap<String, ElementRegion>();
		List<String> reportedRegionValues = new ArrayList<String>();

		for (ElementRegion region : regions) {
			if (duplicateCheckMap.containsKey(region.getValue())) {
				result.add(createMessage(resource, document,
						region.getValueRegion(), severity, message));

				if (!reportedRegionValues.contains(region.getValue())) {
					reportedRegionValues.add(region.getValue());
					result.add(createMessage(resource, document,
							duplicateCheckMap.get(region.getValue())
									.getValueRegion(), severity, message));
				}
			} else {
				duplicateCheckMap.put(region.getValue(), region);
			}
		}
	}

	protected ValidatorMessage createMessage(IResource resource,
			IDocument document, IRegion region, int severity, String text) {
		ValidatorMessage message = ValidatorMessage.create(text, resource);
		message.setType(getProblemMarkerId());
		message.setAttribute(IMarker.SEVERITY, severity);
		message.setAttribute(IMarker.CHAR_START, region.getOffset());
		message.setAttribute(IMarker.CHAR_END,
				region.getOffset() + region.getLength());
		// add line number
		try {
			int line = document.getLineOfOffset(region.getOffset());
			message.setAttribute(IMarker.LINE_NUMBER, line + 1);
		} catch (BadLocationException e) {
		}
		return message;
	}
}
