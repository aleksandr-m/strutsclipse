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
package com.amashchenko.eclipse.strutsclipse.xmlparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;

import com.amashchenko.eclipse.strutsclipse.StrutsXmlConstants;

public class StrutsXmlParser extends AbstractXmlParser {
	private static final String[] TAGS = { StrutsXmlConstants.CONSTANT_TAG,
			StrutsXmlConstants.PACKAGE_TAG, StrutsXmlConstants.ACTION_TAG,
			StrutsXmlConstants.RESULT_TAG, StrutsXmlConstants.PARAM_TAG,
			CLOSE_TAG_TOKEN };

	private static final String[] ATTRS = { StrutsXmlConstants.EXTENDS_ATTR,
			StrutsXmlConstants.NAMESPACE_ATTR, StrutsXmlConstants.NAME_ATTR,
			StrutsXmlConstants.TYPE_ATTR, StrutsXmlConstants.METHOD_ATTR,
			StrutsXmlConstants.CLASS_ATTR };

	public TagRegion getTagRegion(final IDocument document, final int offset) {
		IDocumentPartitioner partitioner = null;
		try {
			TagRegion result = null;

			// create tag partitioner
			partitioner = createTagPartitioner(document, TAGS);

			ITypedRegion tagRegion = partitioner.getPartition(offset);

			ElementRegion currentElement = null;
			String elementValuePrefix = null;

			// check if offset is between start and end tags
			int bodyOffset = -1;
			int bodyLength = -1;
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())) {
				ITypedRegion nextRegion = partitioner.getPartition(tagRegion
						.getOffset() + tagRegion.getLength());
				if (CLOSE_TAG_TOKEN.equals(nextRegion.getType())) {
					bodyOffset = tagRegion.getOffset();
					bodyLength = tagRegion.getLength();
				}
			} else if (CLOSE_TAG_TOKEN.equals(tagRegion.getType())
					&& tagRegion.getOffset() == offset) {
				ITypedRegion prevRegion = partitioner.getPartition(tagRegion
						.getOffset() - 1);
				if (IDocument.DEFAULT_CONTENT_TYPE.equals(prevRegion.getType())) {
					bodyOffset = prevRegion.getOffset();
					bodyLength = prevRegion.getLength();
				} else {
					bodyOffset = tagRegion.getOffset();
				}
			}
			if (bodyOffset != -1) {
				if (bodyLength == -1) {
					currentElement = new ElementRegion(null, "",
							tagRegion.getOffset());
					elementValuePrefix = "";
				} else {
					try {
						currentElement = new ElementRegion(null, document.get(
								bodyOffset, bodyLength), bodyOffset);
						elementValuePrefix = document.get(bodyOffset, offset
								- bodyOffset);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}

				// get start tag of current tag body
				tagRegion = partitioner.getPartition(bodyOffset - 1);
			}

			if (!IDocument.DEFAULT_CONTENT_TYPE.equals(tagRegion.getType())
					&& !CLOSE_TAG_TOKEN.equals(tagRegion.getType())) {
				List<ElementRegion> attrRegions = parseTag(document, tagRegion,
						ATTRS);

				if (attrRegions != null) {
					for (ElementRegion r : attrRegions) {
						try {
							final int valDocOffset = r.getValueRegion()
									.getOffset();

							// if not in tag body and current attribute
							if (currentElement == null
									&& valDocOffset - 1 <= offset
									&& valDocOffset
											+ r.getValueRegion().getLength()
											+ 1 > offset) {
								currentElement = r;

								// attribute value to invocation offset
								elementValuePrefix = document.get(valDocOffset,
										offset - valDocOffset);
							}
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}

				result = new TagRegion(tagRegion.getType(), currentElement,
						elementValuePrefix, attrRegions);
			}

			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
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
			final String packageNamespace) {
		IDocumentPartitioner partitioner = null;
		try {
			Set<String> result = new HashSet<String>();

			List<String> namespaces = new ArrayList<String>();
			namespaces.add(packageNamespace);

			// handle special namespaces
			if (!"".equals(packageNamespace)) {
				namespaces.add("");
			} else if (!"/".equals(packageNamespace)) {
				namespaces.add("/");
			}

			List<IRegion> packBodyRegions = findTagsBodyRegionByAttrValues(
					document, StrutsXmlConstants.PACKAGE_TAG,
					StrutsXmlConstants.NAMESPACE_ATTR, namespaces, true);

			if (packBodyRegions != null) {
				List<ElementRegion> actionRegions = new ArrayList<ElementRegion>();
				for (IRegion r : packBodyRegions) {
					actionRegions.addAll(findAllTagAttr(document,
							StrutsXmlConstants.ACTION_TAG,
							StrutsXmlConstants.NAME_ATTR, r.getOffset(),
							r.getLength()));
				}

				if (actionRegions != null) {
					for (ElementRegion r : actionRegions) {
						result.add(r.getValue());
					}
				}
			}
			return result;
		} finally {
			if (partitioner != null) {
				partitioner.disconnect();
			}
		}
	}

	public IRegion getActionRegion(final IDocument document,
			final String namespace, final String actionName) {
		List<String> namespaces = new ArrayList<String>();
		namespaces.add(namespace);

		// handle special namespaces
		if (!"".equals(namespace)) {
			namespaces.add("");
		} else if (!"/".equals(namespace)) {
			namespaces.add("/");
		}

		List<IRegion> packBodyRegions = findTagsBodyRegionByAttrValues(
				document, StrutsXmlConstants.PACKAGE_TAG,
				StrutsXmlConstants.NAMESPACE_ATTR, namespaces, true);

		IRegion region = null;
		if (packBodyRegions != null) {
			for (IRegion r : packBodyRegions) {
				ElementRegion attrRegion = findTagAttrByValue(document,
						StrutsXmlConstants.ACTION_TAG,
						StrutsXmlConstants.NAME_ATTR, actionName,
						r.getOffset(), r.getLength());
				if (attrRegion != null) {
					region = attrRegion.getValueRegion();
					break;
				}
			}
		}
		return region;
	}

	public Set<String> getPackageNames(final IDocument document) {
		List<ElementRegion> attrRegions = findAllTagAttr(document,
				StrutsXmlConstants.PACKAGE_TAG, StrutsXmlConstants.NAME_ATTR);
		Set<String> result = new HashSet<String>();
		if (attrRegions != null) {
			for (ElementRegion r : attrRegions) {
				result.add(r.getValue());
			}
		}
		return result;
	}
}
