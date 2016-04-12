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

import java.util.Set;

import org.eclipse.core.runtime.AssertionFailedException;
import org.junit.Assert;
import org.junit.Test;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public class ParseUtilTest {
	private static final String VALUE = "one  ,   two, three   ";

	@Test
	public void testParseFirstValue() throws Exception {
		final String prefix = "on";

		ElementRegion result = ParseUtil.parseElementValue(VALUE, prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals(prefix, result.getName());
		Assert.assertEquals("one", result.getValue());
		Assert.assertEquals(0, result.getValueRegion().getOffset());
		Assert.assertEquals(3, result.getValueRegion().getLength());
	}

	@Test
	public void testParseFirstValueWithSpaces() throws Exception {
		final String prefix = "on";

		ElementRegion result = ParseUtil.parseElementValue(
				"   one  ,   two, three   ", prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals(prefix, result.getName());
		Assert.assertEquals("one", result.getValue());
		Assert.assertEquals(3, result.getValueRegion().getOffset());
		Assert.assertEquals(3, result.getValueRegion().getLength());
	}

	@Test
	public void testParseMiddleValue() throws Exception {
		final String prefixStr = "tw";
		final String prefix = VALUE.substring(0, VALUE.indexOf(prefixStr)
				+ prefixStr.length());

		ElementRegion result = ParseUtil.parseElementValue(VALUE, prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals(prefixStr, result.getName());
		Assert.assertEquals("two", result.getValue());
		Assert.assertEquals(VALUE.indexOf(prefixStr), result.getValueRegion()
				.getOffset());
		Assert.assertEquals(3, result.getValueRegion().getLength());
	}

	@Test
	public void testParseLastValue() throws Exception {
		final String prefixStr = "thr";
		final String prefix = VALUE.substring(0, VALUE.indexOf(prefixStr)
				+ prefixStr.length());

		ElementRegion result = ParseUtil.parseElementValue(VALUE, prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals(prefixStr, result.getName());
		Assert.assertEquals("three", result.getValue());
		Assert.assertEquals(VALUE.indexOf(prefixStr), result.getValueRegion()
				.getOffset());
		Assert.assertEquals(5, result.getValueRegion().getLength());
	}

	@Test
	public void testParseLastValueWithSpaces() throws Exception {
		final String prefixStr = "three  ";
		final String prefix = VALUE.substring(0, VALUE.indexOf(prefixStr)
				+ prefixStr.length());

		ElementRegion result = ParseUtil.parseElementValue(VALUE, prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals("three", result.getName());
		Assert.assertEquals("three", result.getValue());
		Assert.assertEquals(VALUE.indexOf(prefixStr), result.getValueRegion()
				.getOffset());
		Assert.assertEquals(5, result.getValueRegion().getLength());
	}

	@Test
	public void testParseSingleValue() throws Exception {
		final String value = "single";
		final String prefixStr = "si";
		final String prefix = value.substring(0, value.indexOf(prefixStr)
				+ prefixStr.length());

		ElementRegion result = ParseUtil.parseElementValue(value, prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals(prefixStr, result.getName());
		Assert.assertEquals(value, result.getValue());
		Assert.assertEquals(0, result.getValueRegion().getOffset());
		Assert.assertEquals(value.length(), result.getValueRegion().getLength());
	}

	@Test
	public void testParseSingleValueWithSpaces() throws Exception {
		final String value = "   single   ";
		final String prefixStr = "   si";
		final String prefix = value.substring(0, value.indexOf(prefixStr)
				+ prefixStr.length());

		ElementRegion result = ParseUtil.parseElementValue(value, prefix,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR, 0);

		Assert.assertEquals(prefixStr, result.getName());
		Assert.assertEquals(value.trim(), result.getValue());
		Assert.assertEquals(3, result.getValueRegion().getOffset());
		Assert.assertEquals(value.trim().length(), result.getValueRegion()
				.getLength());
	}

	@Test
	public void testParseNullSeparator() throws Exception {
		final String prefix = "on";

		ElementRegion result = ParseUtil.parseElementValue(VALUE, prefix, null,
				0);

		Assert.assertEquals(prefix, result.getName());
		Assert.assertEquals(VALUE, result.getValue());
		Assert.assertEquals(0, result.getValueRegion().getOffset());
		Assert.assertEquals(VALUE.length(), result.getValueRegion().getLength());
	}

	@Test
	public void testParseEmptySeparator() throws Exception {
		final String prefix = "on";

		ElementRegion result = ParseUtil
				.parseElementValue(VALUE, prefix, "", 0);

		Assert.assertEquals(prefix, result.getName());
		Assert.assertEquals(VALUE, result.getValue());
		Assert.assertEquals(0, result.getValueRegion().getOffset());
		Assert.assertEquals(VALUE.length(), result.getValueRegion().getLength());
	}

	@Test
	public void testParseSingleValueNullSeparator() throws Exception {
		final String value = " single";
		final String prefixStr = " si";
		final String prefix = value.substring(0, value.indexOf(prefixStr)
				+ prefixStr.length());

		ElementRegion result = ParseUtil.parseElementValue(value, prefix, null,
				0);

		Assert.assertEquals(prefixStr, result.getName());
		Assert.assertEquals(value, result.getValue());
		Assert.assertEquals(0, result.getValueRegion().getOffset());
		Assert.assertEquals(value.length(), result.getValueRegion().getLength());
	}

	// delimitedStringToSet
	@Test
	public void testDelimitedStringToSet() throws Exception {
		final String f = "first";
		final String s = "second";
		final String t = "third";

		final String str = f + "  ,   " + s + "," + t;
		Set<String> set = ParseUtil.delimitedStringToSet(str,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR);

		Assert.assertNotNull(set);
		Assert.assertFalse(set.isEmpty());
		Assert.assertEquals(3, set.size());
		Assert.assertTrue(set.contains(f));
		Assert.assertTrue(set.contains(s));
		Assert.assertTrue(set.contains(t));
	}

	@Test
	public void testDelimitedStringToSetSingle() throws Exception {
		final String str = "single";
		Set<String> set = ParseUtil.delimitedStringToSet(str,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR);

		Assert.assertNotNull(set);
		Assert.assertFalse(set.isEmpty());
		Assert.assertEquals(1, set.size());
		Assert.assertTrue(set.contains(str));
	}

	@Test(expected = AssertionFailedException.class)
	public void testDelimitedStringToSetNoDelimiter() throws Exception {
		final String str = "single";
		ParseUtil.delimitedStringToSet(str, null);
	}

	@Test
	public void testDelimitedStringToSetNull() throws Exception {
		Set<String> set = ParseUtil.delimitedStringToSet(null,
				StrutsXmlConstants.MULTI_VALUE_SEPARATOR);

		Assert.assertNotNull(set);
		Assert.assertTrue(set.isEmpty());
	}
}
