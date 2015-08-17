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
package com.amashchenko.eclipse.strutsclipse;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;

import com.amashchenko.eclipse.strutsclipse.java.JavaProjectUtil;

public class StrutsXmlHyperlinkDetector extends AbstractHyperlinkDetector {
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		IHyperlink link = null;

		final TagRegion tagRegion = StrutsXmlParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentAttr() != null) {
			if (StrutsXmlConstants.ACTION_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				final AttrRegion classAttr = tagRegion.getAttrs().get(
						StrutsXmlConstants.CLASS_ATTR);
				if (StrutsXmlConstants.METHOD_ATTR.equalsIgnoreCase(tagRegion
						.getCurrentAttr().getName()) && classAttr != null) {
					try {
						IJavaProject javaProject = JavaProjectUtil
								.getCurrentJavaProject(document);
						if (javaProject != null && javaProject.exists()) {
							IType type = javaProject.findType(classAttr
									.getValue());
							if (type != null && type.exists()) {
								IMethod method = type.getMethod(tagRegion
										.getCurrentAttr().getValue(), null);
								if (method != null && method.exists()) {
									link = new JavaElementHyperlink(tagRegion
											.getCurrentAttr().getValueRegion(),
											method);
								} else {
									// try super classes
									IType[] superClasses = type
											.newSupertypeHierarchy(null)
											.getAllSuperclasses(type);
									for (IType superType : superClasses) {
										method = superType.getMethod(tagRegion
												.getCurrentAttr().getValue(),
												null);
										if (method != null && method.exists()) {
											link = new JavaElementHyperlink(
													tagRegion.getCurrentAttr()
															.getValueRegion(),
													method);
											break;
										}
									}
								}
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			}
		}

		IHyperlink[] links = null;
		if (link != null) {
			links = new IHyperlink[] { link };
		}

		return links;
	}

	private static class JavaElementHyperlink implements IHyperlink {
		private final IJavaElement fElement;
		private final IRegion fRegion;

		private JavaElementHyperlink(IRegion region, IJavaElement element) {
			fRegion = region;
			fElement = element;
		}

		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		@Override
		public String getHyperlinkText() {
			return null;
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
}
