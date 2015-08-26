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

import java.util.Map;

public class TagRegion {
	private final String name;
	private final ElementRegion currentElement;
	private final String currentElementValuePrefix;
	private final Map<String, ElementRegion> attrs;

	public TagRegion(String tagName, ElementRegion currelement,
			String currelementvalprefix, Map<String, ElementRegion> allattrs) {
		this.name = tagName;
		this.currentElement = currelement;
		this.currentElementValuePrefix = currelementvalprefix;
		this.attrs = allattrs;
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
}
