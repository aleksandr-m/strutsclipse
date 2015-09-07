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

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Assert;
import org.junit.Test;

import com.amashchenko.eclipse.strutsclipse.StrutsXmlConstants;

public class StrutsXmlParserTest {
	private StrutsXmlParser strutsXmlParser = new StrutsXmlParser();

	@Test
	public void testGetTagRegionPackageTag() throws Exception {
		final String content = "<package name=\"somename\" extends=\"someextends,otherextends\"></package>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.EXTENDS_ATTR));
	}

	@Test
	public void testGetTagRegionActionTag() throws Exception {
		final String content = "<action name=\"someaction\" method=\"somemethod\" class=\"someclass\"></action>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(3, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.METHOD_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.CLASS_ATTR));
	}

	@Test
	public void testGetTagRegionSingleQuotes() throws Exception {
		final String content = "<action name='someaction' method='somemethod' class='someclass'></action>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(3, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.METHOD_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.CLASS_ATTR));
	}

	@Test
	public void testGetTagRegionSingleQuotesInside() throws Exception {
		final String content = "<action name=\"some'action\" method=\"some''method\" class=\"some'''class\"></action>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(3, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.METHOD_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.CLASS_ATTR));
	}

	@Test
	public void testGetTagRegionQuotesInside() throws Exception {
		final String content = "<action name='some\"action' method='some\"\"method' class='some\"\"\"class'></action>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(3, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.METHOD_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.CLASS_ATTR));
	}

	@Test
	public void testGetTagRegionLineBrakes() throws Exception {
		final String content = "<action name\n=\"someaction\" method=\n\"somemethod\" class=\"someclass\"></action>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(3, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.METHOD_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.CLASS_ATTR));
	}

	@Test
	public void testGetTagRegionUnknowAttributes() throws Exception {
		final String content = "<action unknown=\"someaction\" unknown=\"somemethod\" class=\"someclass\"></action>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(1, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.CLASS_ATTR));
	}

	@Test
	public void testGetTagRegionResultTag() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"></result>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetTagRegionResultTagBody() throws Exception {
		final String value = "somevalue";
		final String content = "<result name=\"somename\" type=\"sometype\">"
				+ value + "</result>";

		final int valueOffset = content.indexOf(value);

		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				valueOffset);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertNotNull(tagRegion.getCurrentElementValuePrefix());
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertEquals(StrutsXmlConstants.RESULT_TAG, tagRegion.getName());
		Assert.assertEquals("", tagRegion.getCurrentElementValuePrefix());

		// current element
		Assert.assertNull(tagRegion.getCurrentElement().getName());
		Assert.assertEquals(value, tagRegion.getCurrentElement().getValue());
		Assert.assertEquals(valueOffset, tagRegion.getCurrentElement()
				.getValueRegion().getOffset());
		Assert.assertEquals(value.length(), tagRegion.getCurrentElement()
				.getValueRegion().getLength());

		// attributes
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetTagRegionResultTagBody2() throws Exception {
		final String value = "somevalue";
		final String content = "<result name=\"somename\" type=\"sometype\">"
				+ value + "</result>";

		final int valueOffset = content.indexOf(value);
		final int cursorOffset = content.indexOf(value) + value.length();

		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				cursorOffset);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertNotNull(tagRegion.getCurrentElementValuePrefix());
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertEquals(StrutsXmlConstants.RESULT_TAG, tagRegion.getName());
		Assert.assertEquals(value, tagRegion.getCurrentElementValuePrefix());

		// current element
		Assert.assertNull(tagRegion.getCurrentElement().getName());
		Assert.assertEquals(value, tagRegion.getCurrentElement().getValue());
		Assert.assertEquals(valueOffset, tagRegion.getCurrentElement()
				.getValueRegion().getOffset());
		Assert.assertEquals(value.length(), tagRegion.getCurrentElement()
				.getValueRegion().getLength());

		// attributes
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetTagRegionResultTagBodyNoValue() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"></result>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				content.indexOf("</"));

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertNotNull(tagRegion.getCurrentElementValuePrefix());
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertEquals(StrutsXmlConstants.RESULT_TAG, tagRegion.getName());
		Assert.assertEquals("", tagRegion.getCurrentElementValuePrefix());

		// current element
		Assert.assertNull(tagRegion.getCurrentElement().getName());
		Assert.assertEquals("", tagRegion.getCurrentElement().getValue());
		Assert.assertEquals(0, tagRegion.getCurrentElement().getValueRegion()
				.getLength());

		// attributes
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetTagRegionResultTagCloseTag() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"></result>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				content.indexOf("</") + 4);

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetTagRegionUnknownTag() throws Exception {
		final String content = "<unknown></unknown>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getTagRegion(document, 2);

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetTagRegionInAttr() throws Exception {
		final String attrValue = "tiles-default, struts-default, json-default";
		final String content = "<package extends=\"" + attrValue
				+ "\"></package>";
		IDocument document = new Document(content);

		final int cursorOffset = content.indexOf("ts-default, json-default");
		final int valueOffset = content.indexOf("\"") + 1;
		final String prefix = content.substring(valueOffset, cursorOffset);

		TagRegion tagRegion = strutsXmlParser.getTagRegion(document,
				cursorOffset);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertNotNull(tagRegion.getCurrentElement().getValueRegion());
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertEquals(StrutsXmlConstants.PACKAGE_TAG, tagRegion.getName());

		Assert.assertEquals(prefix, tagRegion.getCurrentElementValuePrefix());

		// current attribute
		Assert.assertEquals(StrutsXmlConstants.EXTENDS_ATTR, tagRegion
				.getCurrentElement().getName());
		Assert.assertEquals(attrValue, tagRegion.getCurrentElement().getValue());
		Assert.assertEquals(valueOffset, tagRegion.getCurrentElement()
				.getValueRegion().getOffset());
		Assert.assertEquals(attrValue.length(), tagRegion.getCurrentElement()
				.getValueRegion().getLength());

		// attributes
		Assert.assertEquals(1, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.EXTENDS_ATTR));
	}

	// get parent tag region
	@Test
	public void testGetParentTagRegion() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"><param name=\"some\">paramvalue</param></result>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("paramvalue"), StrutsXmlConstants.RESULT_TAG);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertEquals(StrutsXmlConstants.RESULT_TAG, tagRegion.getName());

		// attributes
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetParentTagRegionNoValue() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"><param name=\"some\"></param></result>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("</param"), StrutsXmlConstants.RESULT_TAG);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertEquals(StrutsXmlConstants.RESULT_TAG, tagRegion.getName());

		// attributes
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetParentTagRegionUnknown() throws Exception {
		final String content = "<unknown name=\"somename\" type=\"sometype\"><param name=\"some\">paramvalue</param></unknown>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("paramvalue"), StrutsXmlConstants.RESULT_TAG);

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetParentTagRegionOtherParent() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"></result><unknown><param name=\"some\">paramvalue</param></unknown>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("paramvalue"), StrutsXmlConstants.RESULT_TAG);

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetParentTagRegionOtherParentNoValue() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"></result><unknown><param name=\"some\"></param></unknown>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("</param"), StrutsXmlConstants.RESULT_TAG);

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetParentTagRegionDeepNested() throws Exception {
		final String content = "<package extends=\"some\"><action name=\"somename\"><result><param name=\"some\">paramvalue</param></result></action></package>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("paramvalue"), StrutsXmlConstants.PACKAGE_TAG);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertEquals(StrutsXmlConstants.PACKAGE_TAG, tagRegion.getName());

		// attributes
		Assert.assertEquals(1, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.EXTENDS_ATTR));
	}

	@Test
	public void testGetParentTagRegionDeepNestedNoValue() throws Exception {
		final String content = "<package extends=\"some\"><action name=\"somename\"><result><param name=\"some\"></param></result></action></package>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsXmlParser.getParentTagRegion(document,
				content.indexOf("</param"), StrutsXmlConstants.PACKAGE_TAG);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertEquals(StrutsXmlConstants.PACKAGE_TAG, tagRegion.getName());

		// attributes
		Assert.assertEquals(1, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.EXTENDS_ATTR));
	}
}
