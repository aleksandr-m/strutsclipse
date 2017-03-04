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

	private static final List<String> JSP_HTML_FILE_EXTENSIONS = Arrays.asList(
			"jsp", "html", "htm");
	private static final List<String> FREEMARKER_FILE_EXTENSIONS = Arrays
			.asList("ftl");
	private static final List<String> XML_FILE_EXTENSIONS = Arrays
			.asList("xml");
	private static final String PROPERTIES_FILE_EXTENSION = "properties";
	private static final List<String> PROPERTIES_FILE_EXTENSIONS = Arrays
			.asList(PROPERTIES_FILE_EXTENSION);
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

	/**
	 * Searches project for files with given file extension and content type. If
	 * given content type isn't known by the platform, search will check if
	 * resource name contains given file name.
	 * 
	 * @param currentDocument
	 *            Document in the project to search.
	 * @param folderName
	 *            Name of the folder to search in.
	 * @param fileExtensions
	 *            File extensions search criteria.
	 * @param resourcePredicate
	 *            Determines whether to include resource into result list or
	 *            not.
	 * @param retrieveDocument
	 *            Whether to get document from the resource.
	 * @return List of the resources meeting the search criteria, or empty list
	 *         if no resources are found.
	 */
	private static List<ResourceDocument> findResources(
			final IDocument currentDocument, final String folderName,
			final List<String> fileExtensions,
			final ResourcePredicate resourcePredicate,
			final boolean retrieveDocument) {
		final List<ResourceDocument> result = new ArrayList<ResourceDocument>();

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
											boolean addToList = resourcePredicate == null ? true
													: resourcePredicate.test(
															project, resource);

											if (addToList) {
												IPath path = resource
														.getProjectRelativePath()
														.makeRelativeTo(
																res.getProjectRelativePath());

												if (retrieveDocument) {
													try {
														provider.connect(resource);
														IDocument document = provider
																.getDocument(resource);
														provider.disconnect(resource);

														result.add(new ResourceDocument(
																resource,
																document,
																path.toString()));
													} catch (CoreException e) {
														e.printStackTrace();
													}
												} else {
													path = path.makeAbsolute();
													result.add(new ResourceDocument(
															resource, null,
															path.toString()));
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
				new ContentTypeFileNamePredicate(
						StrutsXmlConstants.TILES_RESULT,
						TILES_XML_CONTENT_TYPE_ID), true);
	}

	public static List<ResourceDocument> findStrutsResources(
			final IDocument currentDocument) {
		return findResources(currentDocument, WEB_INF_CLASSES_FOLDER_PATH,
				XML_FILE_EXTENSIONS, new ContentTypeFileNamePredicate(
						StrutsXmlConstants.STRUTS_FILE_NAME,
						STRUTS_XML_CONTENT_TYPE_ID), true);
	}

	public static List<ResourceDocument> findPropertiesResources(
			final IDocument currentDocument, final Set<String> bundleNames) {
		return findResources(currentDocument, WEB_INF_CLASSES_FOLDER_PATH,
				PROPERTIES_FILE_EXTENSIONS, new ResourceBundlesPredicate(
						bundleNames), true);
	}

	public static Set<String> findJspHtmlFilesPaths(
			final IDocument currentDocument) {
		List<ResourceDocument> resources = findResources(currentDocument, null,
				JSP_HTML_FILE_EXTENSIONS, null, false);

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
				FREEMARKER_FILE_EXTENSIONS, null, false);

		Set<String> paths = new HashSet<String>();
		if (resources != null) {
			for (ResourceDocument r : resources) {
				paths.add(r.getRelativePath());
			}
		}
		return paths;
	}

	private static List<JarEntryStorage> findJarEntries(
			final IDocument document, JarEntryPredicate jarEntryPredicate) {
		List<JarEntryStorage> results = new ArrayList<JarEntryStorage>();
		try {
			IJavaProject javaProject = getCurrentJavaProject(document);

			if (javaProject != null && javaProject.exists()) {
				IPackageFragmentRoot[] roots = javaProject
						.getPackageFragmentRoots();
				for (IPackageFragmentRoot root : roots) {
					if (root.isArchive()) {
						// from root
						collectNonJavaResources(root.getNonJavaResources(),
								root.getPath(), results, jarEntryPredicate);
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return results;
	}

	private static void collectNonJavaResources(Object[] nonJavaResources,
			IPath rootPath, List<JarEntryStorage> list,
			JarEntryPredicate jarEntryPredicate) {
		for (Object nonJavaRes : nonJavaResources) {
			if (nonJavaRes instanceof IJarEntryResource) {
				IJarEntryResource jarEntry = (IJarEntryResource) nonJavaRes;

				boolean addToList = jarEntryPredicate == null ? true
						: jarEntryPredicate.test(jarEntry);

				if (addToList) {
					list.add(new JarEntryStorage(rootPath.append(jarEntry
							.getFullPath()), jarEntry));
				}
			}
		}
	}

	public static List<JarEntryStorage> findJarEntryStrutsResources(
			final IDocument document) {
		return findJarEntries(document, new StrutsResourceJarPredicate());
	}

	public static List<JarEntryStorage> findJarEntryPropertyResources(
			final IDocument document, final Set<String> bundleNames) {
		return findJarEntries(document, new ResourceBundlesJarPredicate(
				bundleNames));
	}

	private interface ResourcePredicate {
		boolean test(IProject project, IResource resource) throws CoreException;
	}

	private static class ContentTypeFileNamePredicate implements
			ResourcePredicate {
		private final String fileName;
		private final IContentType contentType;

		private ContentTypeFileNamePredicate(String fileName,
				String contentTypeId) {
			this.fileName = fileName;
			IContentTypeManager contentTypeManager = Platform
					.getContentTypeManager();
			this.contentType = contentTypeManager.getContentType(contentTypeId);
		}

		// If the platform doesn't know content type identifier then
		// <code>fileName</code> will be used.
		@Override
		public boolean test(IProject project, IResource resource)
				throws CoreException {
			boolean check = false;
			if (contentType != null) {
				IFile file = project.getFile(resource.getProjectRelativePath());
				IContentDescription descrp = file.getContentDescription();
				check = descrp.getContentType().isKindOf(contentType);
			} else if (fileName != null
					&& resource.getName().toLowerCase(Locale.ROOT)
							.contains(fileName)) {
				check = true;
			}
			return check;
		}
	}

	private static class ResourceBundlesPredicate implements ResourcePredicate {
		private final Set<String> bundleNames;

		private ResourceBundlesPredicate(Set<String> bundleNames) {
			this.bundleNames = bundleNames;
		}

		private boolean compare(Set<String> names, String name) {
			if (names != null && name != null) {
				for (String n : names) {
					if (name.startsWith(n + "_")
							|| name.equals(n + "." + PROPERTIES_FILE_EXTENSION)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean test(IProject project, IResource resource)
				throws CoreException {
			return resource.getFileExtension()
					.equals(PROPERTIES_FILE_EXTENSION)
					&& compare(bundleNames, resource.getName());
		}
	}

	private interface JarEntryPredicate {
		boolean test(IJarEntryResource jarEntryResource);
	}

	private static class StrutsResourceJarPredicate implements
			JarEntryPredicate {
		@Override
		public boolean test(IJarEntryResource jarEntryResource) {
			return jarEntryResource.isFile()
					&& (StrutsXmlConstants.STRUTS_DEFAULT_FILE_NAME
							.equals(jarEntryResource.getName()) || StrutsXmlConstants.STRUTS_PLUGIN_FILE_NAME
							.equals(jarEntryResource.getName()));
		}
	}

	private static class ResourceBundlesJarPredicate implements
			JarEntryPredicate {
		private final Set<String> bundleNames;

		private ResourceBundlesJarPredicate(Set<String> bundleNames) {
			this.bundleNames = bundleNames;
		}

		private boolean compare(Set<String> names, String name) {
			if (names != null && name != null) {
				for (String n : names) {
					if (name.startsWith(n + "_")
							|| name.equals(n + "." + PROPERTIES_FILE_EXTENSION)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean test(IJarEntryResource jarEntryResource) {
			return jarEntryResource.isFile()
					&& jarEntryResource.getName() != null
					&& jarEntryResource.getName().endsWith(
							PROPERTIES_FILE_EXTENSION)
					&& compare(bundleNames, jarEntryResource.getName());
		}
	}
}
