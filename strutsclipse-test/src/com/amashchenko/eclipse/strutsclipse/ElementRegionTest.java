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
package com.amashchenko.eclipse.strutsclipse;

import org.junit.Assert;
import org.junit.Test;

public class ElementRegionTest {
	@Test
	public void testNotNulls() throws Exception {
		final String attrName = "name";
		final String attrValue = "somename";
		final int valOffset = 5;

		ElementRegion elementRegion = new ElementRegion(attrName, attrValue,
				valOffset);

		Assert.assertNotNull(elementRegion);
		Assert.assertNotNull(elementRegion.getValueRegion());
		Assert.assertEquals(attrName, elementRegion.getName());
		Assert.assertEquals(attrValue, elementRegion.getValue());
		Assert.assertEquals(valOffset, elementRegion.getValueRegion()
				.getOffset());
		Assert.assertEquals(attrValue.length(), elementRegion.getValueRegion()
				.getLength());
	}

	@Test
	public void testNulls() throws Exception {
		final int valOffset = 5;

		ElementRegion elementRegion = new ElementRegion(null, null, valOffset);

		Assert.assertNotNull(elementRegion);
		Assert.assertNotNull(elementRegion.getValueRegion());
		Assert.assertNull(elementRegion.getName());
		Assert.assertNull(elementRegion.getValue());
		Assert.assertEquals(valOffset, elementRegion.getValueRegion()
				.getOffset());
		Assert.assertEquals(0, elementRegion.getValueRegion().getLength());
	}
}
