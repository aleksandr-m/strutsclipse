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
package com.amashchenko.eclipse.strutsclipse.taglib;

public class StrutsTaglibConstants {
	public static final String TAGLIB_PREFIX = "s:";

	public static final String URL_TAG = "s:url";
	public static final String FORM_TAG = "s:form";
	public static final String LINK_TAG = "s:a";
	public static final String ACTION_TAG = "s:action";
	public static final String SUBMIT_TAG = "s:submit";
	public static final String INCLUDE_TAG = "s:include";
	public static final String TEXT_TAG = "s:text";

	public static final String ACTION_ATTR = "action";
	public static final String NAMESPACE_ATTR = "namespace";
	public static final String NAME_ATTR = "name";
	public static final String VALUE_ATTR = "value";
	public static final String THEME_ATTR = "theme";

	public static final String[][] DEFAULT_THEMES = {
			{ "xhtml", "The default theme." },
			{
					"simple",
					"The simple theme renders 'bare bones' HTML elements. For example, the textfield tag renders the HTML <input/> tag without a label, validation, error reporting, or any other formatting or functionality." },
			{ "css_xhtml",
					"The xhtml theme re-implemented using strictly CSS for layout." } };
}
