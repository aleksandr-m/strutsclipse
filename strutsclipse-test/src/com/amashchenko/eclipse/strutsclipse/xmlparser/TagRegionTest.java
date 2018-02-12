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
package com.amashchenko.eclipse.strutsclipse.xmlparser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TagRegionTest {
	@Test
	public void testTagRegion() throws Exception {
		final String tagName = "tagname";
		final String attrName = "attrname";
		final String attrValue = "attrvalue";

		List<ElementRegion> allattrs = new ArrayList<ElementRegion>();
		allattrs.add(new ElementRegion(attrName, attrValue, 0));
		allattrs.add(null);

		TagRegion tagRegion = new TagRegion(tagName, null, null, allattrs);

		Assert.assertNotNull(tagRegion);
		Assert.assertEquals(tagName, tagRegion.getName());
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertFalse(tagRegion.getAttrs().isEmpty());

		Assert.assertNotNull(tagRegion.getAttrs().get(attrName));
		Assert.assertEquals(attrName, tagRegion.getAttrs().get(attrName)
				.getName());
		Assert.assertEquals(attrValue, tagRegion.getAttrs().get(attrName)
				.getValue());
	}

	@Test
	public void testTagRegionNullAttrs() throws Exception {
		final String tagName = "tagname";

		TagRegion tagRegion = new TagRegion(tagName, null, null, null);

		Assert.assertNotNull(tagRegion);
		Assert.assertEquals(tagName, tagRegion.getName());
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNull(tagRegion.getAttrs());
	}

	@Test
	public void testTagRegionEmptyAttrs() throws Exception {
		final String tagName = "tagname";

		TagRegion tagRegion = new TagRegion(tagName, null, null,
				new ArrayList<ElementRegion>());

		Assert.assertNotNull(tagRegion);
		Assert.assertEquals(tagName, tagRegion.getName());
		Assert.assertNull(tagRegion.getCurrentElement());
		Assert.assertNull(tagRegion.getCurrentElementValuePrefix());

		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertTrue(tagRegion.getAttrs().isEmpty());
	}

	@Test
	public void testGetAttrValue() throws Exception {
		final String attrName = "attrname";
		final String attrValue = "attrvalue";
		final String defaultValue = "default";

		List<ElementRegion> allattrs = new ArrayList<ElementRegion>();
		allattrs.add(new ElementRegion(attrName, attrValue, 0));

		TagRegion tagRegion = new TagRegion(null, null, null, allattrs);

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getAttrs());
		Assert.assertFalse(tagRegion.getAttrs().isEmpty());
		Assert.assertNotNull(tagRegion.getAttrs().get(attrName));
		Assert.assertEquals(attrName, tagRegion.getAttrs().get(attrName)
				.getName());
		Assert.assertEquals(attrValue, tagRegion.getAttrs().get(attrName)
				.getValue());

		Assert.assertEquals(attrValue, tagRegion.getAttrValue(attrName, null));
		Assert.assertNull(tagRegion.getAttrValue("notexistingname", null));
		Assert.assertEquals(defaultValue,
				tagRegion.getAttrValue("notexistingname", defaultValue));
	}

	@Test
	public void testGetNullAttrValue() throws Exception {
		final String defaultValue = "default";

		TagRegion tagRegion = new TagRegion(null, null, null, null);

		Assert.assertNotNull(tagRegion);
		Assert.assertNull(tagRegion.getAttrs());

		Assert.assertNull(tagRegion.getAttrValue("notexistingname", null));
		Assert.assertEquals(defaultValue,
				tagRegion.getAttrValue("notexistingname", defaultValue));
	}

	@Test
	public void testGetEmptyAttrValue() throws Exception {
		final String defaultValue = "default";

		TagRegion tagRegion = new TagRegion(null, null, null,
				new ArrayList<ElementRegion>());

		Assert.assertNotNull(tagRegion);
		Assert.assertNotNull(tagRegion.getAttrs());

		Assert.assertNull(tagRegion.getAttrValue("notexistingname", null));
		Assert.assertEquals(defaultValue,
				tagRegion.getAttrValue("notexistingname", defaultValue));
	}
}
