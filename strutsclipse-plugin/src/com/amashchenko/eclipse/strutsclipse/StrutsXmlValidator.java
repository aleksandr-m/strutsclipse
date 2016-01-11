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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.validation.AbstractValidator;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;
import org.eclipse.wst.validation.ValidatorMessage;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.StrutsXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsXmlValidator extends AbstractValidator {
	private static final String PROBLEM_MARKER_ID = "com.amashchenko.eclipse.strutsclipse.strutsxmlproblemmarker";

	private static final String DUP_ACTION_MESSAGE_TEXT = "Duplicate action name.";
	private static final String DUP_PACKAGE_MESSAGE_TEXT = "Duplicate package name.";
	private static final String NO_METHOD_MESSAGE_TEXT = "No such method.";

	private final StrutsXmlParser strutsXmlParser;

	public StrutsXmlValidator() {
		strutsXmlParser = new StrutsXmlParser();
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
			// validate packages
			List<ElementRegion> packageNameRegions = strutsXmlParser
					.getPackageNameRegions(document);

			Map<String, ElementRegion> dupPackNameCheckMap = new HashMap<String, ElementRegion>();
			List<String> reportedPackages = new ArrayList<String>();

			for (ElementRegion pregion : packageNameRegions) {
				if (dupPackNameCheckMap.containsKey(pregion.getValue())) {
					result.add(createMessage(resource, document,
							pregion.getValueRegion(), IMarker.SEVERITY_WARNING,
							DUP_PACKAGE_MESSAGE_TEXT));

					if (!reportedPackages.contains(pregion.getValue())) {
						reportedPackages.add(pregion.getValue());
						result.add(createMessage(resource, document,
								dupPackNameCheckMap.get(pregion.getValue())
										.getValueRegion(),
								IMarker.SEVERITY_WARNING,
								DUP_PACKAGE_MESSAGE_TEXT));
					}
				} else {
					dupPackNameCheckMap.put(pregion.getValue(), pregion);
				}
			}

			// validate actions
			Map<String, List<TagRegion>> actionRegions = strutsXmlParser
					.getNamespacedActionTagRegions(document);

			Map<String, ElementRegion> dupCheckMap = new HashMap<String, ElementRegion>();
			List<String> reportedActions = new ArrayList<String>();

			for (Entry<String, List<TagRegion>> entr : actionRegions.entrySet()) {
				for (TagRegion tagRegion : entr.getValue()) {
					if (tagRegion.getAttrs() != null) {
						// check duplicate action names
						ElementRegion nameAttr = tagRegion.getAttrs().get(
								StrutsXmlConstants.NAME_ATTR);
						if (nameAttr != null) {
							String actionName = entr.getKey()
									+ nameAttr.getValue();
							if (dupCheckMap.containsKey(actionName)) {
								result.add(createMessage(resource, document,
										nameAttr.getValueRegion(),
										IMarker.SEVERITY_WARNING,
										DUP_ACTION_MESSAGE_TEXT));

								if (!reportedActions.contains(actionName)) {
									reportedActions.add(actionName);
									result.add(createMessage(resource,
											document,
											dupCheckMap.get(actionName)
													.getValueRegion(),
											IMarker.SEVERITY_WARNING,
											DUP_ACTION_MESSAGE_TEXT));
								}
							} else {
								dupCheckMap.put(actionName, nameAttr);
							}
						}

						// check method existence
						ElementRegion methodAttr = tagRegion.getAttrs().get(
								StrutsXmlConstants.METHOD_ATTR);
						String classAttrVal = tagRegion.getAttrValue(
								StrutsXmlConstants.CLASS_ATTR, null);
						if (classAttrVal != null && methodAttr != null) {
							String methodAttrValue = methodAttr.getValue();

							// skip wildcard methods
							if (methodAttrValue.indexOf('{') == -1) {
								IType clazz = ProjectUtil.findClass(document,
										classAttrVal);
								// skip methods with unknown class
								if (clazz != null) {
									IMethod method = ProjectUtil
											.findClassParameterlessMethod(
													clazz, methodAttrValue);
									if (method == null) {
										result.add(createMessage(resource,
												document,
												methodAttr.getValueRegion(),
												IMarker.SEVERITY_ERROR,
												NO_METHOD_MESSAGE_TEXT));
									}
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	private ValidatorMessage createMessage(IResource resource,
			IDocument document, IRegion region, int severity, String text) {
		ValidatorMessage message = ValidatorMessage.create(text, resource);
		message.setType(PROBLEM_MARKER_ID);
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
