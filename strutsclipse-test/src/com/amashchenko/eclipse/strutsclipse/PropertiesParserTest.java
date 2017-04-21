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

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.junit.Assert;
import org.junit.Test;

public class PropertiesParserTest {
	private PropertiesParser propertiesParser = new PropertiesParser();

	@Test
	public void testGetKeyRegion() throws Exception {
		final String key = "key_key.key";
		final String content = "\r\n" + key + " = value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNotNull(region);
		Assert.assertTrue(region.getLength() > 0);
	}

	@Test
	public void testGetKeyRegion2() throws Exception {
		final String key = "key_key.key";
		final String content = "\r\n some = value \r\n" + key
				+ "=value\n # comment\n value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNotNull(region);
		Assert.assertTrue(region.getLength() > 0);
	}

	@Test
	public void testGetKeyRegionColon() throws Exception {
		final String key = "key_key.key";
		final String content = "\r\n" + key + " : value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNotNull(region);
		Assert.assertTrue(region.getLength() > 0);
	}

	@Test
	public void testGetKeyRegionWithSpaces() throws Exception {
		final String key = "key\\ key.key";
		final String content = "\r\n" + key + " = value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, "key key.key");
		Assert.assertNotNull(region);
		Assert.assertTrue(region.getLength() > 0);
	}

	@Test
	public void testGetKeyRegionKeyOnFirstLine() throws Exception {
		final String key = "key_key.key";
		final String content = key + " = value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNotNull(region);
		Assert.assertTrue(region.getLength() > 0);
	}

	@Test
	public void testGetKeyRegionComment() throws Exception {
		final String key = "key_key.key";
		final String content = "#" + key + " = value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNull(region);
	}

	@Test
	public void testGetKeyRegionComment2() throws Exception {
		final String key = "key_key.key";
		final String content = "!" + key + " = value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNull(region);
	}

	@Test
	public void testGetKeyRegionJustKey() throws Exception {
		final String key = "key_key.key";
		final String content = key;
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, key);
		Assert.assertNull(region);
	}

	@Test
	public void testGetKeyRegionNull() throws Exception {
		final String content = "\r\n key = value";
		IDocument document = new Document(content);
		IRegion region = propertiesParser.getKeyRegion(document, null);
		Assert.assertNull(region);
	}
}
