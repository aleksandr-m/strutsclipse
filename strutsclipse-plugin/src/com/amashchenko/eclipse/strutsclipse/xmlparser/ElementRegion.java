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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class ElementRegion {
	private final String name;
	private final String value;
	private final IRegion valueRegion;

	public ElementRegion(String elementName, String elementValue, int valOffset) {
		this.name = elementName;
		this.value = elementValue;
		this.valueRegion = new Region(valOffset, elementValue == null ? 0
				: elementValue.length());
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public IRegion getValueRegion() {
		return valueRegion;
	}

	@Override
	public String toString() {
		return "ElementRegion [name=" + name + ", value=" + value
				+ ", valueRegion=" + valueRegion + "]";
	}
}
