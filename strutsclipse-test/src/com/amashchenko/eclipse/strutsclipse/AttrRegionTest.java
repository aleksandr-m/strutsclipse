package com.amashchenko.eclipse.strutsclipse;

import org.junit.Assert;
import org.junit.Test;

public class AttrRegionTest {
	@Test
	public void testNotNulls() throws Exception {
		final String attrName = "name";
		final String attrValue = "somename";
		final int valOffset = 5;

		AttrRegion attrRegion = new AttrRegion(attrName, attrValue, valOffset);

		Assert.assertNotNull(attrRegion);
		Assert.assertNotNull(attrRegion.getValueRegion());
		Assert.assertEquals(attrName, attrRegion.getName());
		Assert.assertEquals(attrValue, attrRegion.getValue());
		Assert.assertEquals(valOffset, attrRegion.getValueRegion().getOffset());
		Assert.assertEquals(attrValue.length(), attrRegion.getValueRegion()
				.getLength());
	}

	@Test
	public void testNulls() throws Exception {
		final int valOffset = 5;

		AttrRegion attrRegion = new AttrRegion(null, null, valOffset);

		Assert.assertNotNull(attrRegion);
		Assert.assertNotNull(attrRegion.getValueRegion());
		Assert.assertNull(attrRegion.getName());
		Assert.assertNull(attrRegion.getValue());
		Assert.assertEquals(valOffset, attrRegion.getValueRegion().getOffset());
		Assert.assertEquals(0, attrRegion.getValueRegion().getLength());
	}
}
