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
package com.amashchenko.eclipse.strutsclipse.strutsxml;

public interface StrutsXmlLocations {
	String CONSTANT_NAME = StrutsXmlConstants.CONSTANT_TAG
			+ StrutsXmlConstants.NAME_ATTR;
	String BEAN_CLASS = StrutsXmlConstants.BEAN_TAG
			+ StrutsXmlConstants.CLASS_ATTR;
	String BEAN_SCOPE = StrutsXmlConstants.BEAN_TAG
			+ StrutsXmlConstants.SCOPE_ATTR;
	String INCLUDE_FILE = StrutsXmlConstants.INCLUDE_TAG
			+ StrutsXmlConstants.FILE_ATTR;
	String PACKAGE_EXTENDS = StrutsXmlConstants.PACKAGE_TAG
			+ StrutsXmlConstants.EXTENDS_ATTR;
	String INTERCEPTOR_REF_NAME = StrutsXmlConstants.INTERCEPTOR_REF_TAG
			+ StrutsXmlConstants.NAME_ATTR;
	String DEFAULT_INTERCEPTOR_REF_NAME = StrutsXmlConstants.DEFAULT_INTERCEPTOR_REF_TAG
			+ StrutsXmlConstants.NAME_ATTR;
	String DEFAULT_ACTION_REF_NAME = StrutsXmlConstants.DEFAULT_ACTION_REF_TAG
			+ StrutsXmlConstants.NAME_ATTR;
	String ACTION_NAME = StrutsXmlConstants.ACTION_TAG
			+ StrutsXmlConstants.NAME_ATTR;
	String ACTION_CLASS = StrutsXmlConstants.ACTION_TAG
			+ StrutsXmlConstants.CLASS_ATTR;
	String ACTION_METHOD = StrutsXmlConstants.ACTION_TAG
			+ StrutsXmlConstants.METHOD_ATTR;
	String RESULT_NAME = StrutsXmlConstants.RESULT_TAG
			+ StrutsXmlConstants.NAME_ATTR;
	String RESULT_TYPE = StrutsXmlConstants.RESULT_TAG
			+ StrutsXmlConstants.TYPE_ATTR;
	String RESULT_BODY = StrutsXmlConstants.RESULT_TAG + "null";
	String PARAM_BODY = StrutsXmlConstants.PARAM_TAG + "null";
}
