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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagRegion {
	private final String name;
	private final ElementRegion currentElement;
	private final String currentElementValuePrefix;
	private final Map<String, ElementRegion> attrs;

	public TagRegion(String tagName, ElementRegion currelement,
			String currelementvalprefix, List<ElementRegion> attrslist) {
		this.name = tagName;
		this.currentElement = currelement;
		this.currentElementValuePrefix = currelementvalprefix;

		this.attrs = createMapAttrs(attrslist);
	}

	private Map<String, ElementRegion> createMapAttrs(
			List<ElementRegion> attrslist) {
		Map<String, ElementRegion> map = null;
		if (attrslist != null) {
			map = new HashMap<String, ElementRegion>();
			for (ElementRegion r : attrslist) {
				if (r != null) {
					map.put(r.getName(), r);
				}
			}
		}
		return map;
	}

	public String getName() {
		return name;
	}

	public ElementRegion getCurrentElement() {
		return currentElement;
	}

	public String getCurrentElementValuePrefix() {
		return currentElementValuePrefix;
	}

	public Map<String, ElementRegion> getAttrs() {
		return attrs;
	}

	/**
	 * Gets safely attribute value.
	 * 
	 * @param attrName
	 *            Name of the attribute to get value from.
	 * @param defaultValue
	 *            Default value to return in case of any null.
	 * @return Attribute value or defaultValue. The defaultValue will be
	 *         returned if attributes are null or attribute with such name
	 *         doesn't exist or it is null.
	 */
	public String getAttrValue(String attrName, String defaultValue) {
		String value = defaultValue;
		if (attrs != null) {
			ElementRegion r = attrs.get(attrName);
			if (r != null) {
				value = r.getValue();
			}
		}
		return value;
	}
}
