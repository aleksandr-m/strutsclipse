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
package com.amashchenko.eclipse.strutsclipse.taglib;

public interface StrutsTaglibLocations {
	String URL_ACTION = StrutsTaglibConstants.URL_TAG
			+ StrutsTaglibConstants.ACTION_ATTR;
	String FORM_ACTION = StrutsTaglibConstants.FORM_TAG
			+ StrutsTaglibConstants.ACTION_ATTR;
	String LINK_ACTION = StrutsTaglibConstants.LINK_TAG
			+ StrutsTaglibConstants.ACTION_ATTR;

	String URL_NAMESPACE = StrutsTaglibConstants.URL_TAG
			+ StrutsTaglibConstants.NAMESPACE_ATTR;
	String FORM_NAMESPACE = StrutsTaglibConstants.FORM_TAG
			+ StrutsTaglibConstants.NAMESPACE_ATTR;
	String LINK_NAMESPACE = StrutsTaglibConstants.LINK_TAG
			+ StrutsTaglibConstants.NAMESPACE_ATTR;

	String ACTION_NAME = StrutsTaglibConstants.ACTION_TAG
			+ StrutsTaglibConstants.NAME_ATTR;
	String ACTION_NAMESPACE = StrutsTaglibConstants.ACTION_TAG
			+ StrutsTaglibConstants.NAMESPACE_ATTR;

	String SUBMIT_ACTION = StrutsTaglibConstants.SUBMIT_TAG
			+ StrutsTaglibConstants.ACTION_ATTR;
}
