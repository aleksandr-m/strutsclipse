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
package com.amashchenko.eclipse.strutsclipse.strutsxml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.amashchenko.eclipse.strutsclipse.AbstractStrutsHyperlinkDetector;
import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;
import com.amashchenko.eclipse.strutsclipse.ParseUtil;
import com.amashchenko.eclipse.strutsclipse.ProjectUtil;
import com.amashchenko.eclipse.strutsclipse.ResourceDocument;
import com.amashchenko.eclipse.strutsclipse.tilesxml.TilesXmlParser;
import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;
import com.amashchenko.eclipse.strutsclipse.xmlparser.PackageData;
import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsXmlHyperlinkDetector extends AbstractStrutsHyperlinkDetector
		implements StrutsXmlLocations {
	private static final String DEFAULT_VALIDATE_METHOD_LINK_TEXT = "ActionSupport#validate";

	private final StrutsXmlParser strutsXmlParser;
	private final TilesXmlParser tilesXmlParser;

	public StrutsXmlHyperlinkDetector() {
		strutsXmlParser = new StrutsXmlParser();
		tilesXmlParser = new TilesXmlParser();
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		List<IHyperlink> linksList = new ArrayList<IHyperlink>();

		final TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				region.getOffset());

		if (tagRegion != null && tagRegion.getCurrentElement() != null) {
			final IRegion elementRegion = tagRegion.getCurrentElement()
					.getValueRegion();
			final String elementValue = tagRegion.getCurrentElement()
					.getValue();

			final String key = tagRegion.getName()
					+ tagRegion.getCurrentElement().getName();

			switch (key) {
			case PACKAGE_EXTENDS:
				ElementRegion parsedValue = ParseUtil.parseElementValue(
						elementValue, tagRegion.getCurrentElementValuePrefix(),
						StrutsXmlConstants.MULTI_VALUE_SEPARATOR,
						elementRegion.getOffset());

				// find in current document
				IHyperlink localLink = createPackageExtendsLocalLink(document,
						parsedValue.getValue(), parsedValue.getValueRegion());

				if (localLink == null) {
					// find in other struts files
					linksList.addAll(createPackageExtendsResourcesLink(
							document, parsedValue.getValue(),
							parsedValue.getValueRegion()));
					if (linksList.isEmpty()) {
						// find in jars
						linksList.addAll(createPackageExtendsJarLinks(document,
								parsedValue.getValue(),
								parsedValue.getValueRegion()));
					}
				} else {
					linksList.add(localLink);
				}
				break;
			case INTERCEPTOR_REF_NAME:
			case DEFAULT_INTERCEPTOR_REF_NAME:
				linksList.addAll(createResultTypeOrInterceptorRefLinks(false,
						document, elementValue, elementRegion));
				break;
			case RESULT_TYPE:
				linksList.addAll(createResultTypeOrInterceptorRefLinks(true,
						document, elementValue, elementRegion));
				break;
			case DEFAULT_ACTION_REF_NAME:
				// same as for the result location, but with concrete namespace
				TagRegion packageTagRegion = strutsXmlParser
						.getParentTagRegion(document,
								elementRegion.getOffset(),
								StrutsXmlConstants.PACKAGE_TAG);
				if (packageTagRegion != null) {
					String namespace = packageTagRegion.getAttrValue(
							StrutsXmlConstants.NAMESPACE_ATTR, "");
					linksList.addAll(createResultLocationLinks(document,
							elementValue, elementRegion,
							StrutsXmlConstants.REDIRECT_ACTION_RESULT,
							namespace));
				}
				break;
			case ACTION_NAME:
				final String classAttrVal = tagRegion.getAttrValue(
						StrutsXmlConstants.CLASS_ATTR, null);
				final String methodAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.METHOD_ATTR, null);

				linksList.addAll(createValidationLinks(document, elementRegion,
						classAttrVal, elementValue, methodAttrValue));
				break;
			case ACTION_METHOD:
				final String classAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.CLASS_ATTR, null);
				if (classAttrValue != null) {
					linksList.add(createClassMethodLink(document, elementValue,
							elementRegion, classAttrValue));
				}
				break;
			case RESULT_BODY:
				linksList.addAll(createResultLocationLinks(document,
						elementValue, elementRegion, tagRegion.getAttrValue(
								StrutsXmlConstants.TYPE_ATTR, null), null));
				break;
			case PARAM_BODY:
				final String nameAttrValue = tagRegion.getAttrValue(
						StrutsXmlConstants.NAME_ATTR, null);
				if (nameAttrValue != null) {
					TagRegion resultTagRegion = strutsXmlParser
							.getResultTagRegion(document, region.getOffset());
					if (resultTagRegion != null) {
						// name is type value, here
						final String typeAttrValue = resultTagRegion.getName();
						boolean correctTypeAndName = (StrutsXmlConstants.LOCATION_PARAM
								.equals(nameAttrValue) && (typeAttrValue == null || (!StrutsXmlConstants.REDIRECT_ACTION_RESULT
								.equals(typeAttrValue) && !StrutsXmlConstants.CHAIN_RESULT
								.equals(typeAttrValue))))
								|| (typeAttrValue != null
										&& (StrutsXmlConstants.REDIRECT_ACTION_RESULT
												.equals(typeAttrValue) || StrutsXmlConstants.CHAIN_RESULT
												.equals(typeAttrValue)) && StrutsXmlConstants.ACTION_NAME_PARAM
											.equals(nameAttrValue));
						if (correctTypeAndName) {
							final String namespaceParamValue = resultTagRegion
									.getAttrValue(
											StrutsXmlConstants.NAMESPACE_ATTR,
											null);
							linksList.addAll(createResultLocationLinks(
									document, elementValue, elementRegion,
									typeAttrValue, namespaceParamValue));
						}
					}
				}
				break;
			}
		}

		return linksListToArray(linksList);
	}

	private List<IHyperlink> createResultLocationLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion, final String typeAttrValue,
			final String namespaceParamValue) {
		final List<IHyperlink> links = new ArrayList<IHyperlink>();

		// assume that default is dispatcher for now, TODO improve
		// that
		if (typeAttrValue == null
				|| StrutsXmlConstants.DISPATCHER_RESULT.equals(typeAttrValue)
				|| StrutsXmlConstants.FREEMARKER_RESULT.equals(typeAttrValue)) {
			IFile file = ProjectUtil.findFile(document, elementValue, false);
			if (file != null && file.exists()) {
				links.add(new FileHyperlink(elementRegion, file));
			}
		} else if (StrutsXmlConstants.REDIRECT_ACTION_RESULT
				.equals(typeAttrValue)
				|| StrutsXmlConstants.CHAIN_RESULT.equals(typeAttrValue)) {
			Set<String> namespaces = new HashSet<String>();

			// if there is a namespaceParamValue then used it, else get
			// namespace from parent package
			String namespace = namespaceParamValue;
			if (namespace == null) {
				TagRegion packageTagRegion = strutsXmlParser
						.getParentTagRegion(document,
								elementRegion.getOffset(),
								StrutsXmlConstants.PACKAGE_TAG);
				if (packageTagRegion != null) {
					namespace = packageTagRegion.getAttrValue(
							StrutsXmlConstants.NAMESPACE_ATTR, "");
				} else {
					namespace = "";
				}

				// if namespace came NOT from namespaceParamValue then add
				// special namespaces
				namespaces.add("");
				namespaces.add("/");
			}

			namespaces.add(namespace);

			IRegion region = strutsXmlParser.getActionRegion(document,
					namespaces, elementValue);
			if (region != null) {
				IFile file = ProjectUtil.getCurrentDocumentFile(document);
				if (file.exists()) {
					links.add(new FileHyperlink(elementRegion, file, region));
				}
			}
		} else if (StrutsXmlConstants.TILES_RESULT.equals(typeAttrValue)) {
			// find tiles resources
			List<ResourceDocument> resources = ProjectUtil
					.findTilesResources(document);
			for (ResourceDocument rd : resources) {
				IRegion region = tilesXmlParser.getDefinitionRegion(
						rd.getDocument(), elementValue);
				if (region != null) {
					if (rd.getResource().getType() == IResource.FILE
							&& rd.getResource().exists()) {
						links.add(new FileHyperlink(elementRegion, (IFile) rd
								.getResource(), region));
					}
				}
			}
		}
		return links;
	}

	private IHyperlink createClassMethodLink(final IDocument document,
			final String elementValue, final IRegion elementRegion,
			final String className) {
		IHyperlink link = null;

		IType clazz = ProjectUtil.findClass(document, className);
		if (clazz != null) {
			IMethod method = ProjectUtil.findClassParameterlessMethod(clazz,
					elementValue);
			if (method != null) {
				link = new JavaElementHyperlink(elementRegion, method);
			}
		}

		return link;
	}

	private IHyperlink createPackageExtendsLocalLink(final IDocument document,
			final String elementValue, final IRegion elementRegion) {
		IHyperlink link = null;

		IRegion packageNameRegion = strutsXmlParser.getPackageNameRegion(
				document, elementValue);
		if (packageNameRegion != null) {
			IFile file = ProjectUtil.getCurrentDocumentFile(document);
			if (file.exists()) {
				link = new FileHyperlink(elementRegion, file, packageNameRegion);
			}
		}

		return link;
	}

	private List<IHyperlink> createPackageExtendsResourcesLink(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		List<IHyperlink> links = new ArrayList<IHyperlink>();

		IPath currentPath = ProjectUtil.getCurrentDocumentPath(document);

		List<ResourceDocument> resources = ProjectUtil
				.findStrutsResources(document);
		for (ResourceDocument rd : resources) {
			if (!rd.getResource().getFullPath().equals(currentPath)) {
				IRegion nameRegion = strutsXmlParser.getPackageNameRegion(
						rd.getDocument(), elementValue);
				if (nameRegion != null) {
					if (rd.getResource().getType() == IResource.FILE
							&& rd.getResource().exists()) {
						links.add(new FileHyperlink(elementRegion, (IFile) rd
								.getResource(), nameRegion));
					}
				}
			}
		}

		return links;
	}

	private List<IHyperlink> createPackageExtendsJarLinks(
			final IDocument document, final String elementValue,
			final IRegion elementRegion) {
		List<IHyperlink> links = new ArrayList<IHyperlink>();

		List<JarEntryStorage> jarStorages = ProjectUtil
				.findJarEntryStrutsResources(document);
		for (JarEntryStorage jarStorage : jarStorages) {
			IRegion nameRegion = strutsXmlParser.getPackageNameRegion(
					jarStorage.toDocument(), elementValue);
			if (nameRegion != null) {
				links.add(new StorageHyperlink(elementRegion, jarStorage,
						nameRegion));
			}
		}

		return links;
	}

	private List<IHyperlink> createResultTypeOrInterceptorRefLinks(
			boolean fetchResultTypes, final IDocument document,
			final String elementValue, final IRegion elementRegion) {
		List<IHyperlink> links = new ArrayList<IHyperlink>();

		TagRegion parentPackage = strutsXmlParser.getParentTagRegion(document,
				elementRegion.getOffset(), StrutsXmlConstants.PACKAGE_TAG);
		if (parentPackage != null) {
			List<PackageData> packages = strutsXmlParser
					.getAllStrutsPackagesData(document, fetchResultTypes);

			List<PackageData> results = new ArrayList<PackageData>();

			collectPackageData(packages, parentPackage.getAttrValue(
					StrutsXmlConstants.NAME_ATTR, ""), elementValue,
					new ArrayList<PackageData>(), results);

			for (PackageData result : results) {
				if (result.getResourceDocument() == null) {
					links.add(new StorageHyperlink(elementRegion, result
							.getJarEntryStorage(), result.getRegion()
							.getValueRegion()));
				} else {
					IResource res = result.getResourceDocument().getResource();
					if (res.getType() == IResource.FILE && res.exists()) {
						links.add(new FileHyperlink(elementRegion, (IFile) res,
								result.getRegion().getValueRegion()));
					}
				}
			}
		}

		return links;
	}

	private void collectPackageData(List<PackageData> packages,
			String packageName, String tagName, List<PackageData> scaned,
			List<PackageData> results) {
		for (PackageData pd : packages) {
			if (packageName.equals(pd.getName()) && !scaned.contains(pd)) {
				if (pd.getTagRegions() != null) {
					for (TagRegion tr : pd.getTagRegions()) {
						if (tagName.equals(tr.getAttrValue(
								StrutsXmlConstants.NAME_ATTR, null))) {
							ElementRegion region = tr.getAttrs().get(
									StrutsXmlConstants.NAME_ATTR);
							pd.setRegion(region);
							results.add(pd);
						}
					}
				}

				scaned.add(pd);

				for (String ext : pd.getExtending()) {
					collectPackageData(packages, ext, tagName, scaned, results);
				}
			}
		}
	}

	private List<IHyperlink> createValidationLinks(final IDocument document,
			final IRegion elementRegion, final String classAttrValue,
			final String nameAttrValue, final String methodAttrValue) {
		List<IHyperlink> links = new ArrayList<IHyperlink>();

		if (classAttrValue != null) {
			final String actionClass = classAttrValue.replace('.', '/');

			String name = actionClass
					+ StrutsXmlConstants.VALIDATION_XML_FILE_SUFFIX;
			IFile xmlClassValidationFile = ProjectUtil.findFile(document, name,
					true);
			if (xmlClassValidationFile != null
					&& xmlClassValidationFile.exists()) {
				links.add(new FileHyperlink(elementRegion,
						xmlClassValidationFile));
			}

			IHyperlink validateMethodLink = createClassMethodLink(document,
					StrutsXmlConstants.VALIDATION_METHOD_NAME, elementRegion,
					classAttrValue);
			if (validateMethodLink != null
					&& !DEFAULT_VALIDATE_METHOD_LINK_TEXT
							.equals(validateMethodLink.getHyperlinkText())) {
				links.add(validateMethodLink);
			}

			if (nameAttrValue != null) {
				String name2 = actionClass + "-" + nameAttrValue
						+ StrutsXmlConstants.VALIDATION_XML_FILE_SUFFIX;
				IFile xmlValidationFile = ProjectUtil.findFile(document, name2,
						true);
				if (xmlValidationFile != null && xmlValidationFile.exists()) {
					links.add(new FileHyperlink(elementRegion,
							xmlValidationFile));
				}
			}

			if (methodAttrValue != null) {
				final String methodName = StrutsXmlConstants.VALIDATION_METHOD_NAME
						+ methodAttrValue.substring(0, 1).toUpperCase(
								Locale.ROOT) + methodAttrValue.substring(1);
				links.add(createClassMethodLink(document, methodName,
						elementRegion, classAttrValue));
			}
		}

		return links;
	}
}
