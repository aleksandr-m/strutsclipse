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
package com.amashchenko.eclipse.strutsclipse.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class JavaClassCompletion {
	private static final String CLASS_NAME = "_xxx";
	private static final String CLASS_SOURCE_END = "\n" + "    }\n" + "}";
	private static final String CLASS_SOURCE_START = "public class "
			+ CLASS_NAME + " {\n" + "    public void main(String[] args) {\n"
			+ "        ";

	private JavaClassCompletion() {
	}

	public static List<ICompletionProposal> getSimpleJavaProposals(
			String prefix, IDocument document, IRegion region) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			String sourceStart = CLASS_SOURCE_START + prefix;
			String packageName = null;
			int dot = prefix.lastIndexOf('.');
			if (dot > -1) {
				packageName = prefix.substring(0, dot);
				sourceStart = "package " + packageName + ";\n" + sourceStart;
			}
			String source = sourceStart + CLASS_SOURCE_END;

			ICompilationUnit unit = createSourceCompilationUnit(prefix,
					document);
			if (unit != null) {
				setCompilationUnitContents(unit, source);

				SimpleJavaProposalCollector collector = new SimpleJavaProposalCollector(
						unit);
				unit.codeComplete(sourceStart.length(), collector);

				IJavaCompletionProposal[] props = collector
						.getJavaCompletionProposals();

				if (props != null) {
					for (IJavaCompletionProposal p : props) {
						if (p instanceof SimpleJavaCompletionProposal) {
							SimpleJavaCompletionProposal sjp = (SimpleJavaCompletionProposal) p;

							// ignore fake class proposals
							if (!sjp.getReplacementString()
									.endsWith(CLASS_NAME)) {
								proposals.add(new CompletionProposal(sjp
										.getReplacementString(), region
										.getOffset(), region.getLength(), sjp
										.getReplacementString().length(), sjp
										.getImage(), sjp.getDisplayString(),
										sjp.getContextInformation(), sjp
												.getAdditionalProposalInfo()));
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return proposals;
	}

	private static ICompilationUnit createSourceCompilationUnit(String prefix,
			IDocument document) throws JavaModelException {
		ICompilationUnit unit = null;

		IJavaProject javaProject = JavaProjectUtil
				.getCurrentJavaProject(document);
		if (javaProject != null) {
			IPackageFragment packageFragment = javaProject
					.getPackageFragments()[0];
			if (packageFragment != null) {
				unit = packageFragment.getCompilationUnit(CLASS_NAME + ".java")
						.getWorkingCopy(null);
			}
		}
		return unit;
	}

	private static void setCompilationUnitContents(ICompilationUnit cu,
			String source) {
		if (cu == null) {
			return;
		}

		synchronized (cu) {
			IBuffer buffer;
			try {
				buffer = cu.getBuffer();
			} catch (JavaModelException e) {
				buffer = null;
			}

			if (buffer != null) {
				buffer.setContents(source);
			}
		}
	}
}
