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
package com.amashchenko.eclipse.strutsclipse.taglib;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Assert;
import org.junit.Test;

import com.amashchenko.eclipse.strutsclipse.xmlparser.TagRegion;

public class StrutsTaglibParserTest {
	private StrutsTaglibParser strutsTaglibParser = new StrutsTaglibParser();

	@Test
	public void testGetGetTextRegionSingleQuotes() throws Exception {
		final String content = "<s:property value=\"getText('')\"/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("')"));

		Assert.assertNotNull(tagRegion);

		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertEquals("", tagRegion.getCurrentElement().getValue());

		Assert.assertEquals("", tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getCurrentElement().getValueRegion());
		Assert.assertEquals(28, tagRegion.getCurrentElement().getValueRegion()
				.getOffset());
		Assert.assertEquals(0, tagRegion.getCurrentElement().getValueRegion()
				.getLength());
	}

	@Test
	public void testGetGetTextRegionDoubleQuotes() throws Exception {
		final String content = "<s:property value='getText(\"\")'/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("\")"));

		Assert.assertNotNull(tagRegion);

		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertEquals("", tagRegion.getCurrentElement().getValue());

		Assert.assertEquals("", tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getCurrentElement().getValueRegion());
		Assert.assertEquals(28, tagRegion.getCurrentElement().getValueRegion()
				.getOffset());
		Assert.assertEquals(0, tagRegion.getCurrentElement().getValueRegion()
				.getLength());
	}

	@Test
	public void testGetGetTextRegionEscapedQuotes() throws Exception {
		final String content = "<s:property value=\"getText(\\\"\\\")\"/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("\")"));

		Assert.assertNotNull(tagRegion);

		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertEquals("", tagRegion.getCurrentElement().getValue());

		Assert.assertEquals("", tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getCurrentElement().getValueRegion());
		Assert.assertEquals(29, tagRegion.getCurrentElement().getValueRegion()
				.getOffset());
		Assert.assertEquals(0, tagRegion.getCurrentElement().getValueRegion()
				.getLength());
	}

	@Test
	public void testGetGetTextRegionText() throws Exception {
		final String text = "test";
		final String content = "<s:property value=\"getText('" + text
				+ "')\"/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("st')"));

		Assert.assertNotNull(tagRegion);

		Assert.assertNotNull(tagRegion.getCurrentElement());
		Assert.assertEquals(text, tagRegion.getCurrentElement().getValue());

		Assert.assertEquals("te", tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getCurrentElement().getValueRegion());
		Assert.assertEquals(28, tagRegion.getCurrentElement().getValueRegion()
				.getOffset());
		Assert.assertEquals(text.length(), tagRegion.getCurrentElement()
				.getValueRegion().getLength());
	}

	@Test
	public void testGetGetTextRegionOutOfRegion() throws Exception {
		final String content = "<s:property value=\"getText('')\"/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("getText"));

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetGetTextRegionOutOfRegion2() throws Exception {
		final String content = "<s:property value=\"getText('')\"/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("\"/"));

		Assert.assertNull(tagRegion);
	}

	@Test
	public void testGetGetTextRegion() throws Exception {
		final String content = "<s:property value=\"\"/>";
		IDocument document = new Document(content);
		TagRegion tagRegion = strutsTaglibParser.getGetTextRegion(document,
				content.indexOf("\"") + 1);

		Assert.assertNull(tagRegion);
	}
}
