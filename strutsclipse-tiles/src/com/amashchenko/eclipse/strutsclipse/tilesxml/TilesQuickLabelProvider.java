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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TilesQuickLabelProvider extends ColumnLabelProvider {
	private static final String PLUGIN_ID = "com.amashchenko.eclipse.strutsclipse.tiles";

	private static final Image DEFINITION_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/d.gif").createImage();
	private static final Image PUT_ATTRIBUTE_TAG_IMG = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/p.gif").createImage();

	@Override
	public Image getImage(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;

			Image image = null;

			switch (node.getNodeName().toLowerCase()) {
			case TilesXmlConstants.DEFINITION_TAG:
				image = DEFINITION_TAG_IMG;
				break;
			case TilesXmlConstants.PUT_ATTRIBUTE_TAG:
				image = PUT_ATTRIBUTE_TAG_IMG;
				break;
			default:
				break;
			}

			return image;
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;

			NamedNodeMap attrs = node.getAttributes();

			StringBuilder text = new StringBuilder();

			if (attrs != null) {
				text.append("<" + node.getNodeName() + " ");
				// try to append name attribute
				appendValue(text,
						attrs.getNamedItem(TilesXmlConstants.NAME_ATTR));

				switch (node.getNodeName()) {
				case TilesXmlConstants.DEFINITION_TAG:
					text.delete(0, text.indexOf(" ") + 1);
					appendNameValue(text,
							attrs.getNamedItem(TilesXmlConstants.TEMPLATE_ATTR));
					appendNameValue(text,
							attrs.getNamedItem(TilesXmlConstants.EXTENDS_ATTR));
					break;
				case TilesXmlConstants.PUT_ATTRIBUTE_TAG:
					appendNameValue(text,
							attrs.getNamedItem(TilesXmlConstants.NAME_ATTR));
					appendNameValue(text,
							attrs.getNamedItem(TilesXmlConstants.VALUE_ATTR));
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
