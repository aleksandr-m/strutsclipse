package com.amashchenko.eclipse.strutsclipse;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Assert;
import org.junit.Test;

public class StrutsXmlParserTest {
	@Test
	public void testGetTagRegionPackageTag() throws Exception {
		final String content = "<package name=\"somename\" extends=\"someextends,otherextends\"></package>";
		IDocument document = new Document(content);
		TagRegion tagRegion = StrutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentAttr());
		Assert.assertNull(tagRegion.getCurrentAttrValuePrefix());

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
		TagRegion tagRegion = StrutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentAttr());
		Assert.assertNull(tagRegion.getCurrentAttrValuePrefix());

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
	public void testGetTagRegionResultTag() throws Exception {
		final String content = "<result name=\"somename\" type=\"sometype\"></result>";
		IDocument document = new Document(content);
		TagRegion tagRegion = StrutsXmlParser.getTagRegion(document, 2);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getCurrentAttr());
		Assert.assertNull(tagRegion.getCurrentAttrValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertEquals(2, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.NAME_ATTR));
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.TYPE_ATTR));
	}

	@Test
	public void testGetTagRegionUnknownTag() throws Exception {
		final String content = "<unknown></unknown>";
		IDocument document = new Document(content);
		TagRegion tagRegion = StrutsXmlParser.getTagRegion(document, 2);

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

		TagRegion tagRegion = StrutsXmlParser.getTagRegion(document,
				cursorOffset);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getCurrentAttr());
		Assert.assertNotNull(tagRegion.getCurrentAttr().getValueRegion());
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertEquals(StrutsXmlConstants.PACKAGE_TAG, tagRegion.getName());

		Assert.assertEquals(prefix, tagRegion.getCurrentAttrValuePrefix());

		// current attribute
		Assert.assertEquals(StrutsXmlConstants.EXTENDS_ATTR, tagRegion
				.getCurrentAttr().getName());
		Assert.assertEquals(attrValue, tagRegion.getCurrentAttr().getValue());
		Assert.assertEquals(valueOffset, tagRegion.getCurrentAttr()
				.getValueRegion().getOffset());
		Assert.assertEquals(attrValue.length(), tagRegion.getCurrentAttr()
				.getValueRegion().getLength());

		// attributes
		Assert.assertEquals(1, tagRegion.getAttrs().size());
		Assert.assertTrue(tagRegion.getAttrs().containsKey(
				StrutsXmlConstants.EXTENDS_ATTR));
	}
}
