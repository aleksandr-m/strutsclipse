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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class QuickLabelProvider extends ColumnLabelProvider {

	private static final String PLUGIN_ID = "com.amashchenko.eclipse.strutsclipse.plugin";

	private static final Image CONSTANT_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/c.gif").createImage();
	private static final Image BEAN_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/b.gif").createImage();
	private static final Image PACKAGE_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/p.gif").createImage();
	private static final Image ACTION_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/a.gif").createImage();
	private static final Image RESULT_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/r.gif").createImage();
	private static final Image PARAM_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/pr.gif").createImage();

	@Override
	public Image getImage(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;

			Image image = null;

			switch (node.getNodeName()) {
			case StrutsXmlConstants.CONSTANT_TAG:
				image = CONSTANT_TAG_IMG;
				break;
			case StrutsXmlConstants.BEAN_TAG:
				image = BEAN_TAG_IMG;
				break;
			case StrutsXmlConstants.PACKAGE_TAG:
				image = PACKAGE_TAG_IMG;
				break;
			case StrutsXmlConstants.ACTION_TAG:
				image = ACTION_TAG_IMG;
				break;
			case StrutsXmlConstants.RESULT_TAG:
				image = RESULT_TAG_IMG;
				break;
			case StrutsXmlConstants.PARAM_TAG:
				image = PARAM_TAG_IMG;
				break;
			default:
				break;
			}

			return image;
		}
		return super.getImage(element);
	}

	public String getText(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;

			NamedNodeMap attrs = node.getAttributes();

			StringBuilder text = new StringBuilder();

			if (attrs != null) {
				text.append("<" + node.getNodeName() + " ");
				// try to append name attribute
				appendValue(text,
						attrs.getNamedItem(StrutsXmlConstants.NAME_ATTR));

				switch (node.getNodeName()) {
				case StrutsXmlConstants.CONSTANT_TAG:
					text.append('=');
					appendValue(text,
							attrs.getNamedItem(StrutsXmlConstants.VALUE_ATTR));
					break;
				case StrutsXmlConstants.PACKAGE_TAG:
					appendNameValue(
							text,
							attrs.getNamedItem(StrutsXmlConstants.NAMESPACE_ATTR));
					appendNameValue(text,
							attrs.getNamedItem(StrutsXmlConstants.EXTENDS_ATTR));
					break;
				case StrutsXmlConstants.INTERCEPTOR_TAG:
					appendNameValue(text,
							attrs.getNamedItem(StrutsXmlConstants.CLASS_ATTR));
					break;
				case StrutsXmlConstants.INTERCEPTOR_REF_TAG:
				case StrutsXmlConstants.INTERCEPTOR_STACK_TAG:
					break;
				case StrutsXmlConstants.ACTION_TAG:
					text.delete(0, text.indexOf(" ") + 1);
					appendNameValue(text,
							attrs.getNamedItem(StrutsXmlConstants.METHOD_ATTR));
					appendNameValue(text,
							attrs.getNamedItem(StrutsXmlConstants.CLASS_ATTR));
					break;
				case StrutsXmlConstants.RESULT_TAG:
					appendNameValue(text,
							attrs.getNamedItem(StrutsXmlConstants.TYPE_ATTR));
					text.append("-> " + node.getTextContent());
					break;
				case StrutsXmlConstants.PARAM_TAG:
					text.append("=" + node.getTextContent());
					break;
				default:
					// not one of the types above -> remove name attribute
					text = new StringBuilder();
					break;
				}
			}

			if (text.length() == 0) {
				text.append("<" + node.getNodeName() + ">");
			} else if (text.charAt(0) == '<') {
				text.append('>');
			}

			return text.toString();
		}
		return super.getText(element);
	}

	private void appendNameValue(StringBuilder text, Node node) {
		if (node != null) {
			if (text.length() > 0) {
				text.append(' ');
			}
			text.append(node.getNodeName() + "=" + node.getNodeValue() + " ");
		}
	}

	private void appendValue(StringBuilder text, Node node) {
		if (node != null) {
			text.append(node.getNodeValue());
		}
	}
}
