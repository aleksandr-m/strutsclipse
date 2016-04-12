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
package com.amashchenko.eclipse.strutsclipse.xmlparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;

import com.amashchenko.eclipse.strutsclipse.StrutsXmlConstants;

public class StrutsXmlParser extends AbstractXmlParser {
	private static final String[] TAGS = { StrutsXmlConstants.BEAN_TAG,
			StrutsXmlConstants.CONSTANT_TAG, StrutsXmlConstants.PACKAGE_TAG,
			StrutsXmlConstants.INTERCEPTOR_REF, StrutsXmlConstants.ACTION_TAG,
			StrutsXmlConstants.RESULT_TAG, StrutsXmlConstants.PARAM_TAG,
			CLOSE_TAG_TOKEN };

	private static final String[] ATTRS = { StrutsXmlConstants.EXTENDS_ATTR,
			StrutsXmlConstants.NAMESPACE_ATTR, StrutsXmlConstants.NAME_ATTR,
			StrutsXmlConstants.TYPE_ATTR, StrutsXmlConstants.METHOD_ATTR,
			StrutsXmlConstants.CLASS_ATTR, StrutsXmlConstants.SCOPE_ATTR };

	public TagRegion getTagRegion(final IDocument document, final int offset) {
		return getTagRegion(document, offset, TAGS, ATTRS);
	}

	public TagRegion getParentTagRegion(final IDocument document,
			final int offset, final String parentTagName) {
		IDocumentPartitioner partitioner = null;
		try {
			TagRegion result = null;

			final String closeParentTag = "/" + parentTagName;

			partitioner = createTagPartitioner(document, new String[] {
					parentTagName, closeParentTag });

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			// get parent
			tagRegion = partitioner.getPartition(tagRegion.getOffset() - 1);

			if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())
					&& !closeParentTag.equals(tagRegion.getType())) {
				List<ElementRegion> attrRegions = parseTag(document, tagRegion,
						ATTRS);

				result = new TagRegion(tagRegion.getType(), null, null,
						attrRegions);
			}

			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	public Set<String> getActionNames(final IDocument document,
			final Set<String> packageNamespaces) {
		Map<String, TagGroup> actionRegions = getNamespacedActionTagRegions(document);

		Set<String> result = new HashSet<String>();
		if (actionRegions != null) {
			for (String namespace : packageNamespaces) {
				if (actionRegions.containsKey(namespace)) {
					for (TagRegion tr : actionRegions.get(namespace)
							.getTagRegions()) {
						result.add(tr.getAttrValue(
								StrutsXmlConstants.NAME_ATTR, ""));
					}
				}
			}
		}

		return result;
	}

	public IRegion getActionRegion(final IDocument document,
			final Set<String> packageNamespaces, final String actionName) {
		Map<String, TagGroup> actionRegions = getNamespacedActionTagRegions(document);

		if (actionRegions != null) {
			for (String namespace : packageNamespaces) {
				if (actionRegions.containsKey(namespace)) {
					for (TagRegion tr : actionRegions.get(namespace)
							.getTagRegions()) {
						if (tr.getAttrs() != null) {
							ElementRegion nameAttr = tr.getAttrs().get(
									StrutsXmlConstants.NAME_ATTR);
							if (nameAttr != null
									&& actionName.equals(nameAttr.getValue())) {
								return nameAttr.getValueRegion();
							}
						}
					}
				}
			}
		}

		return null;
	}

	public Set<String> getPackageNames(final IDocument document) {
		return getAttrsValues(document, StrutsXmlConstants.PACKAGE_TAG,
				StrutsXmlConstants.NAME_ATTR);
	}

	public Set<String> getPackageNamespaces(final IDocument document) {
		return getAttrsValues(document, StrutsXmlConstants.PACKAGE_TAG,
				StrutsXmlConstants.NAMESPACE_ATTR);
	}

	public List<ElementRegion> getPackageNameRegions(final IDocument document) {
		return findAllTagAttr(document, StrutsXmlConstants.PACKAGE_TAG,
				StrutsXmlConstants.NAME_ATTR);
	}

	public IRegion getPackageNameRegion(final IDocument document,
			final String nameValue) {
		ElementRegion attrRegion = findTagAttrByValue(document,
				StrutsXmlConstants.PACKAGE_TAG, StrutsXmlConstants.NAME_ATTR,
				nameValue);
		IRegion region = null;
		if (attrRegion != null) {
			region = attrRegion.getValueRegion();
		}
		return region;
	}

	public List<ElementRegion> getConstantNameRegions(final IDocument document) {
		return findAllTagAttr(document, StrutsXmlConstants.CONSTANT_TAG,
				StrutsXmlConstants.NAME_ATTR);
	}

	public Map<String, TagGroup> getNamespacedActionTagRegions(
			final IDocument document) {
		return getGroupedTagRegions(document, StrutsXmlConstants.PACKAGE_TAG,
				new String[] { StrutsXmlConstants.NAMESPACE_ATTR },
				new String[] { StrutsXmlConstants.ACTION_TAG }, new String[] {
						StrutsXmlConstants.NAME_ATTR,
						StrutsXmlConstants.METHOD_ATTR,
						StrutsXmlConstants.CLASS_ATTR },
				StrutsXmlConstants.NAMESPACE_ATTR);
	}

	/**
	 * Gets result tag with its param tags.
	 * 
	 * @param document
	 *            Document to search.
	 * @param offset
	 *            Document offset.
	 * @return {@link TagRegion} instance where {@link TagRegion#getName()}
	 *         holds result tag type attribute value and
	 *         {@link TagRegion#getAttrs()} hold param tags (attr.name is param
	 *         name value, attr.value is param body).
	 */
	public TagRegion getResultTagRegion(final IDocument document,
			final int offset) {
		IDocumentPartitioner partitioner = null;
		try {
			final String closeResultTag = "/" + StrutsXmlConstants.RESULT_TAG;

			partitioner = createTagPartitioner(document, new String[] {
					StrutsXmlConstants.RESULT_TAG, closeResultTag });

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			int tagOffset = tagRegion.getOffset();
			int length = tagRegion.getLength();

			List<ElementRegion> paramTags = findAllTagAttr(document,
					StrutsXmlConstants.PARAM_TAG, StrutsXmlConstants.NAME_ATTR,
					true, tagOffset, length);

			// get result type attribute value
			String resultTypeValue = null;
			tagRegion = partitioner.getPartition(tagOffset - 1);
			if (StrutsXmlConstants.RESULT_TAG.equals(tagRegion.getType())) {
				List<ElementRegion> attrRegions = parseTag(document, tagRegion,
						new String[] { StrutsXmlConstants.TYPE_ATTR });
				if (!attrRegions.isEmpty() && attrRegions.get(0) != null) {
					resultTypeValue = attrRegions.get(0).getValue();
				}
			}

			// re-map parameters because name is name attribute value and value
			// is param tag body
			List<ElementRegion> list = new ArrayList<ElementRegion>();
			String paramName = null;
			String paramValue = null;
			int paramValueOffset = 0;
			for (ElementRegion r : paramTags) {
				if (StrutsXmlConstants.NAME_ATTR.equals(r.getName())) {
					paramName = r.getValue();
				} else if (r.getName() == null) {
					paramValue = r.getValue();
					paramValueOffset = r.getValueRegion().getOffset();
				}

				if (paramName != null && paramValue != null) {
					list.add(new ElementRegion(paramName, paramValue,
							paramValueOffset));

					paramName = null;
					paramValue = null;
				}
			}

			return new TagRegion(resultTypeValue, null, null, list);
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	public Map<String, TagGroup> getPackageInterceptorsTagRegions(
			final IDocument document) {
		return getGroupedTagRegions(document, StrutsXmlConstants.PACKAGE_TAG,
				new String[] { StrutsXmlConstants.NAME_ATTR,
						StrutsXmlConstants.EXTENDS_ATTR }, new String[] {
						StrutsXmlConstants.INTERCEPTOR_STACK,
						// this is needed because `interceptor-stack` tag starts
						// exactly as `interceptor`
						StrutsXmlConstants.INTERCEPTOR + " " },
				new String[] { StrutsXmlConstants.NAME_ATTR },
				StrutsXmlConstants.NAME_ATTR);
	}
}
