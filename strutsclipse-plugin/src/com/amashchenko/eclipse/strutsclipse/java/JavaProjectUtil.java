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

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;

public class JavaProjectUtil {
	private JavaProjectUtil() {
	}

	public static IJavaProject getCurrentJavaProject(IDocument document) {
		IJavaProject javaProject = null;
		try {
			// try file buffers
			ITextFileBuffer textFileBuffer = FileBuffers
					.getTextFileBufferManager().getTextFileBuffer(document);
			if (textFileBuffer != null) {
				IPath basePath = textFileBuffer.getLocation();
				if (basePath != null && !basePath.isEmpty()) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot()
							.getProject(basePath.segment(0));
					if (basePath.segmentCount() > 1 && project.isAccessible()
							&& project.hasNature(JavaCore.NATURE_ID)) {
						javaProject = JavaCore.create(project);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return javaProject;
	}
}
