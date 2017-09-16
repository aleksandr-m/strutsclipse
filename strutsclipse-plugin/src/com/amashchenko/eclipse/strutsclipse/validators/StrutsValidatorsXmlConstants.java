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
package com.amashchenko.eclipse.strutsclipse.validators;

import java.util.HashMap;
import java.util.Map;

public class StrutsValidatorsXmlConstants {
	public static final String FIELD_VALIDATOR_TAG = "field-validator";
	public static final String VALIDATOR_TAG = "validator";

	public static final String TYPE_ATTR = "type";
	public static final String NAME_ATTR = "name";

	public static final String VALIDATORS_FILE_NAME = "validators.xml";

	public static final String DEFAULT_VALIDATOR_PATH = "com.opensymphony.xwork2.validator.validators";
	public static final String DEFAULT_VALIDATOR_FILE_NAME = "default.xml";

	public static final Map<String, String> DEFAULT_VALIDATORS = new HashMap<String, String>();
	static {
		DEFAULT_VALIDATORS
				.put("conversion",
						"Field Validator that checks if a conversion error occurred for this field.");
		DEFAULT_VALIDATORS
				.put("date",
						"Validator that checks if the date supplied is within a specific range.");
		DEFAULT_VALIDATORS
				.put("double",
						"Field Validator that checks if the double specified is within a certain range.");
		DEFAULT_VALIDATORS
				.put("email",
						"EmailValidator checks that a given String field, if not empty, is a valid email address.");
		DEFAULT_VALIDATORS
				.put("expression",
						"A Non-Field Level validator that validates based on regular expression supplied.");
		DEFAULT_VALIDATORS.put("fieldexpression",
				"Validates a field using an OGNL expression.");
		DEFAULT_VALIDATORS
				.put("int",
						"Field Validator that checks if the integer specified is within a certain range.");
		DEFAULT_VALIDATORS.put("regex",
				"Validates a string field using a regular expression.");
		DEFAULT_VALIDATORS
				.put("required",
						"RequiredFieldValidator checks if the specified field is not null.");
		DEFAULT_VALIDATORS
				.put("requiredstring",
						"RequiredStringValidator checks that a String field is non-null and has a length > 0.");
		DEFAULT_VALIDATORS
				.put("short",
						"Field Validator that checks if the short specified is within a certain range.");
		DEFAULT_VALIDATORS
				.put("stringlength",
						"StringLengthFieldValidator checks that a String field is of a certain length.");
		DEFAULT_VALIDATORS
				.put("url",
						"URLValidator checks that a given field is a String and a valid URL.");
		DEFAULT_VALIDATORS
				.put("visitor",
						"The VisitorFieldValidator allows you to forward validation to object properties of your action using the object's own validation files.");
		DEFAULT_VALIDATORS
				.put("conditionalvisitor",
						"The ConditionalVisitorFieldValidator will forward validation to the VisitorFieldValidator only if the expression will evaluate to true.");
	}
}
