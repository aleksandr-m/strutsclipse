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
package com.amashchenko.eclipse.strutsclipse.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;

import com.amashchenko.eclipse.strutsclipse.ProjectUtil;

public class AnnotationParser {
	private static final String ACTIONS_ANNOTATION = "Actions";
	private static final String ACTIONS_ANNOTATION_FQN = "org.apache.struts2.convention.annotation.Actions";
	private static final String ACTION_ANNOTATION = "Action";
	private static final String ACTION_ANNOTATION_FQN = "org.apache.struts2.convention.annotation.Action";
	private static final String ANNOTATION_VALUE = "value";

	public Set<String> findAnnotationsActionNames(
			final IDocument currentDocument) {
		Set<String> names = new HashSet<String>();
		try {
			List<IAnnotation> annotations = parse(currentDocument);
			for (IAnnotation annotation : annotations) {
				String name = fetchAnnotationStringValue(annotation);
				if (name != null) {
					names.add(name);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return names;
	}

	public List<IJavaElement> findAnnotationsActionElements(
			final IDocument currentDocument, final String actionValue) {
		List<IJavaElement> elements = new ArrayList<IJavaElement>();
		try {
			List<IAnnotation> annotations = parse(currentDocument);
			for (IAnnotation annotation : annotations) {
				if (sameAnnotationStringValue(annotation, actionValue)) {
					elements.add(annotation);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return elements;
	}

	private List<IAnnotation> parse(final IDocument currentDocument)
			throws JavaModelException {
		List<IAnnotation> result = new ArrayList<IAnnotation>();

		IJavaProject javaProject = ProjectUtil
				.getCurrentJavaProject(currentDocument);
		if (javaProject != null && javaProject.exists()) {
			IPackageFragment[] fragments = javaProject.getPackageFragments();
			for (IPackageFragment fragment : fragments) {
				if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
					ICompilationUnit[] units = fragment.getCompilationUnits();
					for (ICompilationUnit unit : units) {
						IType[] types = unit.getTypes();
						for (IType type : types) {
							boolean actionsImportExist = false;
							boolean actionImportExist = false;
							String[][] resolvedType = type
									.resolveType(ACTIONS_ANNOTATION);
							if (resolvedType != null) {
								// if correct Actions annotation
								actionsImportExist = ACTIONS_ANNOTATION_FQN
										.equals(resolvedType[0][0] + "."
												+ resolvedType[0][1]);
							}
							resolvedType = type.resolveType(ACTION_ANNOTATION);
							if (resolvedType != null) {
								// if correct Action annotation
								actionImportExist = ACTION_ANNOTATION_FQN
										.equals(resolvedType[0][0] + "."
												+ resolvedType[0][1]);
							}

							// class annotation
							result.addAll(fetchActionAnnotationValue(type,
									actionsImportExist, actionImportExist));

							// methods annotation
							IMethod[] methods = type.getMethods();
							for (IMethod method : methods) {
								result.addAll(fetchActionAnnotationValue(
										method, actionsImportExist,
										actionImportExist));
							}
						}
					}
				}
			}
		}

		return result;
	}

	private List<IAnnotation> fetchActionAnnotationValue(
			IAnnotatable annotatable, boolean actionsImportExist,
			boolean actionImportExist) throws JavaModelException {
		List<IAnnotation> result = new ArrayList<IAnnotation>();

		IAnnotation[] annotations = annotatable.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (annotation.exists()) {
				boolean fetchValue = ACTIONS_ANNOTATION_FQN.equals(annotation
						.getElementName())
						|| (ACTIONS_ANNOTATION.equals(annotation
								.getElementName()) && actionsImportExist);

				if (fetchValue) {
					IMemberValuePair[] valuePairs = annotation
							.getMemberValuePairs();
					for (IMemberValuePair valuePair : valuePairs) {
						if (valuePair.getValueKind() == IMemberValuePair.K_ANNOTATION
								&& ANNOTATION_VALUE.equals(valuePair
										.getMemberName())) {
							// single value
							if (valuePair.getValue() instanceof IAnnotation) {
								result.add((IAnnotation) valuePair.getValue());
							} else {
								// array
								Object[] objs = (Object[]) valuePair.getValue();
								for (Object o : objs) {
									result.add((IAnnotation) o);
								}
							}
						}
					}
				} else {
					fetchValue = ACTION_ANNOTATION_FQN.equals(annotation
							.getElementName())
							|| (ACTION_ANNOTATION.equals(annotation
									.getElementName()) && actionImportExist);
					if (fetchValue) {
						result.add(annotation);
					}
				}
			}
		}

		return result;
	}

	private String fetchAnnotationStringValue(IAnnotation annotation)
			throws JavaModelException {
		if (annotation.exists()) {
			IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
			for (IMemberValuePair valuePair : valuePairs) {
				if (valuePair.getValueKind() == IMemberValuePair.K_STRING
						&& valuePair.getValue() instanceof String
						&& ANNOTATION_VALUE.equals(valuePair.getMemberName())) {
					return (String) valuePair.getValue();
				}
			}
		}
		return null;
	}

	private boolean sameAnnotationStringValue(IAnnotation annotation,
			String actionValue) throws JavaModelException {
		boolean res = false;
		if (annotation.exists()) {
			IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
			for (IMemberValuePair valuePair : valuePairs) {
				res = valuePair.getValueKind() == IMemberValuePair.K_STRING
						&& valuePair.getValue() instanceof String
						&& ANNOTATION_VALUE.equals(valuePair.getMemberName())
						&& actionValue.equals(valuePair.getValue());
				if (res) {
					break;
				}
			}
		}
		return res;
	}
}
