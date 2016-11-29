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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

import com.amashchenko.eclipse.strutsclipse.strutsxml.StrutsXmlConstants;

public class ProjectUtil {
	private ProjectUtil() {
	}

	private static final List<String> JSP_HTML_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "jsp", "html", "htm" });
	private static final List<String> FREEMARKER_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "ftl" });
	private static final List<String> XML_FILE_EXTENSIONS = Arrays
			.asList(new String[] { "xml" });
	private static final String STRUTS_XML_CONTENT_TYPE_ID = "com.amashchenko.eclipse.strutsclipse.strutsxml";
	private static final String TILES_XML_CONTENT_TYPE_ID = "com.amashchenko.eclipse.strutsclipse.tilesxml";
	private static final String WEB_INF_CLASSES_FOLDER_PATH = "/WEB-INF/classes";

	public static IPath getCurrentDocumentPath(IDocument document) {
		IPath path = null;
		// try file buffers
		ITextFileBuffer textFileBuffer = FileBuffers.getTextFileBufferManager()
				.getTextFileBuffer(document);
		if (textFileBuffer != null) {
			path = textFileBuffer.getLocation();
		}
		return path;
	}

	public static IProject getCurrentProject(IDocument document) {
		IPath basePath = getCurrentDocumentPath(document);
		if (basePath != null && !basePath.isEmpty()) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(basePath.segment(0));
			if (basePath.segmentCount() > 1 && project.isAccessible()) {
				return project;
			}
		}
		return null;
	}

	public static IJavaProject getCurrentJavaProject(IDocument document) {
		IJavaProject javaProject = null;
		try {
			IProject project = getCurrentProject(document);
			if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
				javaProject = JavaCore.create(project);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return javaProject;
	}

	public static IType findClass(final IDocument document,
			final String className) {
		IType result = null;
		try {
			IJavaProject javaProject = getCurrentJavaProject(document);
			if (javaProject != null && javaProject.exists()) {
				IType type = javaProject.findType(className);
				if (type != null && type.exists()) {
					result = type;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static IMethod findClassParameterlessMethod(final IType clazz,
			final String methodName) {
		IMethod result = null;
		try {
			if (clazz != null) {
				IMethod method = clazz.getMethod(methodName, null);
				if (method != null && method.exists()) {
					result = method;
				} else {
					// try super classes
					IType[] superClasses = clazz.newSupertypeHierarchy(null)
							.getAllSuperclasses(clazz);
					for (IType superType : superClasses) {
						method = superType.getMethod(methodName, null);
						if (method != null && method.exists()) {
							result = method;
							break;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<JarEntryStorage> findJarEntryStrutsResources(
			final IDocument document) {
		List<JarEntryStorage> results = new ArrayList<JarEntryStorage>();
		try {
			IJavaProject javaProject = getCurrentJavaProject(document);

			if (javaProject != null && javaProject.exists()) {
				IPackageFragmentRoot[] roots = javaProject
						.getPackageFragmentRoots();
				for (IPackageFragmentRoot root : roots) {
					if (root.isArchive()) {
						Object[] nonJavaResources = root.getNonJavaResources();
						for (Object nonJavaRes : nonJavaResources) {
							if (nonJavaRes instanceof IJarEntryResource) {
								IJarEntryResource jarEntry = (IJarEntryResource) nonJavaRes;
								if (jarEntry.isFile()
										&& (StrutsXmlConstants.STRUTS_DEFAULT_FILE_NAME
												.equals(jarEntry.getName()) || StrutsXmlConstants.STRUTS_PLUGIN_FILE_NAME
												.equals(jarEntry.getName()))) {
									results.add(new JarEntryStorage(root
											.getPath(), jarEntry));
								}
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Searches project for files with given file extension and content type. If
	 * given content type isn't known by the platform search will check if
	 * resource name contains given file name.
	 * 
	 * @param currentDocument
	 *            Document in the project to search.
	 * @param folderName
	 *            Name of the folder to search in.
	 * @param fileExtensions
	 *            File extensions search criteria.
	 * @param contentTypeId
	 *            Content type identifier, if the platform doesn't know it then
	 *            <code>fileName</code> parameter will be used.
	 * @param fileName
	 *            File name search criteria.
	 * @param makeAbsolute
	 *            Whether to make resource path absolute.
	 * @return List of the resources meeting the search criteria, or empty list
	 *         if no resources are found.
	 */
	private static List<ResourceDocument> findResources(
			final IDocument currentDocument, final String folderName,
			final List<String> fileExtensions, final String contentTypeId,
			final String fileName, final boolean makeAbsolute) {
		final List<ResourceDocument> result = new ArrayList<ResourceDocument>();

		IContentTypeManager contentTypeManager = Platform
				.getContentTypeManager();
		final IContentType contentType = contentTypeManager
				.getContentType(contentTypeId);

		try {
			final IProject project = getCurrentProject(currentDocument);
			if (project != null && project.exists()) {
				IVirtualComponent rootComponent = ComponentCore
						.createComponent(project);

				if (rootComponent != null) {
					IVirtualFolder folder = rootComponent.getRootFolder();

					if (folderName != null) {
						folder = folder.getFolder(folderName);
					}

					if (folder != null && folder.exists()) {
						IResource[] resources = folder.getUnderlyingResources();
						if (resources != null) {
							final IDocumentProvider provider = new TextFileDocumentProvider();
							for (final IResource res : resources) {
								res.accept(new IResourceVisitor() {
									@Override
									public boolean visit(IResource resource)
											throws CoreException {
										if (resource.isAccessible()
												&& resource.getType() == IResource.FILE
												&& resource.getFileExtension() != null
												&& fileExtensions
														.contains(resource
																.getFileExtension()
																.toLowerCase(
																		Locale.ROOT))) {
											boolean addToList = false;
											if (contentTypeId == null) {
												addToList = true;
											} else if (contentType != null) {
												IFile file = project.getFile(resource
														.getProjectRelativePath());
												IContentDescription descrp = file
														.getContentDescription();
												addToList = descrp
														.getContentType()
														.isKindOf(contentType);
											} else if (fileName != null
													&& resource
															.getName()
															.toLowerCase(
																	Locale.ROOT)
															.contains(fileName)) {
												addToList = true;
											}

											if (addToList) {
												IPath path = resource
														.getProjectRelativePath()
														.makeRelativeTo(
																res.getProjectRelativePath());
												if (makeAbsolute) {
													path = path.makeAbsolute();
												}

												try {
													provider.connect(resource);
													IDocument document = provider
															.getDocument(resource);
													provider.disconnect(resource);

													result.add(new ResourceDocument(
															resource, document,
															path.toString()));
												} catch (CoreException e) {
													e.printStackTrace();
												}
											}
										}
										return true;
									}
								});
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static List<ResourceDocument> findTilesResources(
			final IDocument currentDocument) {
		return findResources(currentDocument, null, XML_FILE_EXTENSIONS,
				TILES_XML_CONTENT_TYPE_ID, StrutsXmlConstants.TILES_RESULT,
				false);
	}

	public static List<ResourceDocument> findStrutsResources(
			final IDocument currentDocument) {
		return findResources(currentDocument, WEB_INF_CLASSES_FOLDER_PATH,
				XML_FILE_EXTENSIONS, STRUTS_XML_CONTENT_TYPE_ID,
				StrutsXmlConstants.STRUTS_FILE_NAME, false);
	}

	public static Set<String> findJspHtmlFilesPaths(
			final IDocument currentDocument) {
		List<ResourceDocument> resources = findResources(currentDocument, null,
				JSP_HTML_FILE_EXTENSIONS, null, null, true);

		Set<String> paths = new HashSet<String>();
		if (resources != null) {
			for (ResourceDocument r : resources) {
				paths.add(r.getRelativePath());
			}
		}
		return paths;
	}

	public static Set<String> findFreeMarkerFilesPaths(
			final IDocument currentDocument) {
		List<ResourceDocument> resources = findResources(currentDocument, null,
				FREEMARKER_FILE_EXTENSIONS, null, null, true);

		Set<String> paths = new HashSet<String>();
		if (resources != null) {
			for (ResourceDocument r : resources) {
				paths.add(r.getRelativePath());
			}
		}
		return paths;
	}
}
