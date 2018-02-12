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
package com.amashchenko.eclipse.strutsclipse.java;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public class SimpleJavaProposalCollector extends CompletionProposalCollector {

	private static final String ACTION_METHOD_SIGNATURE = "()Ljava.lang.String;";
	private static final String OBJECT_SIGNATURE = "Ljava.lang.Object;";

	private final boolean collectMethods;

	public SimpleJavaProposalCollector(ICompilationUnit cu) {
		this(cu, false);
	}

	public SimpleJavaProposalCollector(ICompilationUnit cu,
			boolean collectMethods) {
		super(cu);
		this.collectMethods = collectMethods;
	}

	@Override
	protected IJavaCompletionProposal createJavaCompletionProposal(
			CompletionProposal proposal) {
		if (collectMethods) {
			if (CompletionProposal.METHOD_REF == proposal.getKind()
					&& Flags.isPublic(proposal.getFlags())) {
				char[] sig = proposal.getSignature();
				char[] declSig = proposal.getDeclarationSignature();
				// collect methods suitable for action methods ignoring Object
				// methods
				if (sig != null && declSig != null
						&& ACTION_METHOD_SIGNATURE.equals(String.valueOf(sig))
						&& !OBJECT_SIGNATURE.equals(String.valueOf(declSig))) {
					return new SimpleJavaCompletionProposal(proposal,
							getInvocationContext(), getImage(getLabelProvider()
									.createImageDescriptor(proposal)));
				}
			}
		} else {
			// collect packages and classes suitable for actions
			if ((CompletionProposal.PACKAGE_REF == proposal.getKind() || CompletionProposal.TYPE_REF == proposal
					.getKind())
					&& !Flags.isAbstract(proposal.getFlags())
					&& !Flags.isInterface(proposal.getFlags())
					&& !Flags.isEnum(proposal.getFlags())) {
				return new SimpleJavaCompletionProposal(proposal,
						getInvocationContext(), getImage(getLabelProvider()
								.createImageDescriptor(proposal)));
			}
		}
		return null;
	}
}
