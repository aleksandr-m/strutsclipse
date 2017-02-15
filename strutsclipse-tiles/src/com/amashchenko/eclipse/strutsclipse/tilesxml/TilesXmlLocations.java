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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

public interface TilesXmlLocations {
	String DEFINITION_TEMPLATE = TilesXmlConstants.DEFINITION_TAG
			+ TilesXmlConstants.TEMPLATE_ATTR;
	String DEFINITION_EXTENDS = TilesXmlConstants.DEFINITION_TAG
			+ TilesXmlConstants.EXTENDS_ATTR;
	String PUT_ATTRIBUTE_NAME = TilesXmlConstants.PUT_ATTRIBUTE_TAG
			+ TilesXmlConstants.NAME_ATTR;
	String PUT_ATTRIBUTE_VALUE = TilesXmlConstants.PUT_ATTRIBUTE_TAG
			+ TilesXmlConstants.VALUE_ATTR;
}
