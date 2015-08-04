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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import com.amashchenko.eclipse.strutsclipse.java.JavaClassCompletion;

public class StrutsXmlCompletionProposalComputer implements
		ICompletionProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		final TagRegion tagRegion = StrutsXmlParser.getTagRegion(
				context.getDocument(), context.getInvocationOffset());

		String[][] proposals = null;
		IRegion proposalRegion = null;
		String attrValuePrefix = null;
		String attrValue = null;
		String multiValueSeparator = null;

		if (tagRegion != null && tagRegion.getCurrentAttr() != null) {
			final String attrName = tagRegion.getCurrentAttr().getName();
			proposalRegion = tagRegion.getCurrentAttr().getValueRegion();
			attrValuePrefix = tagRegion.getCurrentAttrValuePrefix();
			attrValue = tagRegion.getCurrentAttr().getValue();

			if (StrutsXmlConstants.PACKAGE_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (StrutsXmlConstants.EXTENDS_ATTR.equalsIgnoreCase(attrName)) {
					proposals = StrutsXmlConstants.DEFAULT_PACKAGE_NAMES;
					// extends attribute can have multiple values separated by ,
					multiValueSeparator = ",";
				}
			} else if (StrutsXmlConstants.ACTION_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (StrutsXmlConstants.NAME_ATTR.equalsIgnoreCase(attrName)
						|| StrutsXmlConstants.METHOD_ATTR
								.equalsIgnoreCase(attrName)) {
					final AttrRegion classAttr = tagRegion.getAttrs().get(
							StrutsXmlConstants.CLASS_ATTR);

					if (classAttr == null) {
						proposals = StrutsXmlConstants.DEFAULT_METHODS;
					} else {
						List<String> actionMethods = findClassActionMethods(classAttr
								.getValue());
						proposals = new String[actionMethods.size()][2];
						for (int i = 0; i < actionMethods.size(); i++) {
							proposals[i][0] = actionMethods.get(i);
							proposals[i][1] = null;
						}
					}
				} else if (StrutsXmlConstants.CLASS_ATTR
						.equalsIgnoreCase(attrName)) {
					// return proposals
					return JavaClassCompletion.getSimpleJavaProposals(
							attrValuePrefix, context.getDocument(),
							proposalRegion);
				}
			} else if (StrutsXmlConstants.RESULT_TAG.equalsIgnoreCase(tagRegion
					.getName())) {
				if (StrutsXmlConstants.NAME_ATTR.equalsIgnoreCase(attrName)) {
					proposals = StrutsXmlConstants.DEFAULT_RESULT_NAMES;
				} else if (StrutsXmlConstants.TYPE_ATTR
						.equalsIgnoreCase(attrName)) {
					proposals = StrutsXmlConstants.DEFAULT_RESULT_TYPES;
				}
			}
		}

		return createAttrCompletionProposals(proposals, attrValuePrefix,
				proposalRegion, multiValueSeparator, attrValue);
	}

	private List<ICompletionProposal> createAttrCompletionProposals(
			String[][] proposalsData, String prefix, IRegion region,
			String valueSeparator, String attrvalue) {
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		if (proposalsData != null && region != null) {
			int replacementOffset = region.getOffset();
			int replacementLength = region.getLength();

			boolean multivalue = valueSeparator != null
					&& attrvalue.contains(valueSeparator);

			List<String> excludes = new ArrayList<String>();

			if (multivalue) {
				int startSeprIndx = prefix.lastIndexOf(valueSeparator) + 1;

				// spaces between valueSeparator and current value prefix
				// (one,_t|wo -> 1; one,_|two -> 1; one,__t|wo -> 2)
				int spacesCount = 0;

				String currentValue = "";

				// first value in attrvalue
				if (startSeprIndx <= 0) {
					currentValue = attrvalue.substring(0,
							attrvalue.indexOf(valueSeparator));
				} else {
					prefix = prefix.substring(startSeprIndx);
					spacesCount = prefix.length();
					prefix = prefix.trim();
					spacesCount = spacesCount - prefix.length();

					int endSeprIndx = attrvalue.indexOf(valueSeparator,
							startSeprIndx);
					if (endSeprIndx <= 0) {
						// last value in attrvalue
						currentValue = attrvalue.substring(startSeprIndx);
					} else {
						// somewhere in the middle of attrvalue
						currentValue = attrvalue.substring(startSeprIndx,
								endSeprIndx);
					}
				}

				currentValue = currentValue.trim();

				if (spacesCount < 0) {
					spacesCount = 0;
				}

				replacementOffset = replacementOffset + startSeprIndx
						+ spacesCount;
				replacementLength = currentValue.length();

				// exclude already defined values except current value
				String[] valArr = attrvalue.split(valueSeparator);
				for (String val : valArr) {
					if (!currentValue.equalsIgnoreCase(val.trim())) {
						excludes.add(val.trim());
					}
				}
			}

			for (String[] proposal : proposalsData) {
				if (proposal[0].toLowerCase().startsWith(prefix.toLowerCase())
						&& !excludes.contains(proposal[0])) {
					list.add(new CompletionProposal(proposal[0],
							replacementOffset, replacementLength, proposal[0]
									.length(), null, null, null, proposal[1]));
				}
			}
		}
		return list;
	}

	private List<String> findClassActionMethods(final String fullClassName) {
		final List<String> methods = new ArrayList<String>();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			try {
				if (project.isAccessible()
						&& project.isNatureEnabled(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);

					IType itype = javaProject.findType(fullClassName);

					if (itype != null) {
						// support older eclipses
						ASTParser parser = ASTParser.newParser(AST.JLS3);
						parser.setSource(itype.getCompilationUnit());
						parser.setKind(ASTParser.K_COMPILATION_UNIT);

						final CompilationUnit cu = (CompilationUnit) parser
								.createAST(null);

						cu.accept(new ASTVisitor() {
							public boolean visit(MethodDeclaration md) {
								if (Modifier.isPublic(md.getModifiers())
										&& md.parameters().isEmpty()) {
									Type returnType = md.getReturnType2();
									if (returnType != null
											&& returnType.isSimpleType()
											&& "string"
													.equalsIgnoreCase(returnType
															.toString())) {
										methods.add(md.getName()
												.getFullyQualifiedName());
									}
								}

								return false;
							}
						});
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return methods;
	}

	@Override
	public List<IContextInformation> computeContextInformation(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionStarted() {
	}

	@Override
	public void sessionEnded() {
	}
}
