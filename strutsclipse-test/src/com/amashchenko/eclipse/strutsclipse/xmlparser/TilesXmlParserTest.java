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

import java.util.Set;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.junit.Assert;
import org.junit.Test;

public class TilesXmlParserTest {
	private TilesXmlParser tilesXmlParser = new TilesXmlParser();

	@Test
	public void testGetDefinitionNames() throws Exception {
		final String name = "somename";
		final String content = "<definition name=\""
				+ name
				+ "\" extends=\"some\"><put-attribute name=\"title\" value=\"someval\"/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(name));
	}

	@Test
	public void testGetDefinitionNamesMultiple() throws Exception {
		final String name = "somename";
		final String name2 = "someothername";
		final String content = "<definition name=\""
				+ name
				+ "\" extends=\"some\"><put-attribute name=\"title\" value=\"someval\"/></definition>"
				+ "<definition name=\""
				+ name2
				+ "\" extends=\"some\"><put-attribute name=\"title2\" value=\"someval2\"/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(2, names.size());
		Assert.assertTrue(names.contains(name));
		Assert.assertTrue(names.contains(name2));
	}

	@Test
	public void testGetDefinitionNamesSingleQuotes() throws Exception {
		final String name = "somename";
		final String content = "<definition name='"
				+ name
				+ "' extends='some'><put-attribute name='title' value='someval'/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(name));
	}

	@Test
	public void testGetDefinitionNamesSingleQuotesInside() throws Exception {
		final String name = "some'''name";
		final String content = "<definition name=\""
				+ name
				+ "\" extends=\"some\"><put-attribute name=\"title\" value=\"someval\"/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(name));
	}

	@Test
	public void testGetDefinitionNamesQuotesInside() throws Exception {
		final String name = "some\"\"\"name";
		final String content = "<definition name='"
				+ name
				+ "' extends='some'><put-attribute name='title' value='someval'/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(name));
	}

	@Test
	public void testGetDefinitionNamesLineBrakes() throws Exception {
		final String name = "somename";
		final String content = "<definition name\n=\n\""
				+ name
				+ "\" extends=\"some\"><put-attribute name\n=\n\"title\" value=\"someval\"/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(name));
	}

	@Test
	public void testGetDefinitionNamesUnknownAttributes() throws Exception {
		final String name = "somename";
		final String content = "<definition unknown=\"some\" unknown=\"some\" name=\""
				+ name
				+ "\" extends=\"some\"><put-attribute name=\"title\" value=\"someval\"/></definition>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertFalse(names.isEmpty());

		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(name));
	}

	@Test
	public void testGetDefinitionNamesUnknownTag() throws Exception {
		final String content = "<unknown></unknown>";
		IDocument document = new Document(content);
		Set<String> names = tilesXmlParser.getDefinitionNames(document);

		Assert.assertNotNull(names);
		Assert.assertTrue(names.isEmpty());
	}

	// get definition region
	@Test
	public void testGetDefinitionRegion() throws Exception {
		final String name = "somename";
		final String content = "<definition name=\"" + name
				+ "\" extends=\"some\"><put-attribute name=\"" + name
				+ "\" value=\"someval\"/></definition>";
		IDocument document = new Document(content);
		IRegion region = tilesXmlParser.getDefinitionRegion(document, name);

		Assert.assertNotNull(region);

		Assert.assertEquals(content.indexOf(name), region.getOffset());
		Assert.assertEquals(name.length(), region.getLength());
	}
}
