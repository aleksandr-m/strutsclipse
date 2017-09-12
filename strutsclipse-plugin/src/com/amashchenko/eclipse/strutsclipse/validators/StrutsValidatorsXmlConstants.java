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

public class StrutsValidatorsXmlConstants {
	public static final String FIELD_VALIDATOR_TAG = "field-validator";
	public static final String VALIDATOR_TAG = "validator";

	public static final String TYPE_ATTR = "type";

	public static final String[][] DEFAULT_VALIDATORS = {
			{ "conversion",
					"Field Validator that checks if a conversion error occurred for this field." },
			{ "date",
					"Field Validator that checks if the date supplied is within a specific range." },
			{
					"double",
					"Field Validator that checks if the double specified is within a certain range." },
			{
					"email",
					"EmailValidator checks that a given String field, if not empty, is a valid email address." },
			{
					"expression",
					"A Non-Field Level validator that validates based on regular expression supplied." },
			{ "fieldexpression", "Validates a field using an OGNL expression." },
			{
					"int",
					"Field Validator that checks if the integer specified is within a certain range." },
			{ "regex", "Validates a string field using a regular expression." },
			{ "required",
					"RequiredFieldValidator checks if the specified field is not null." },
			{
					"requiredstring",
					"RequiredStringValidator checks that a String field is non-null and has a length > 0." },
			{ "short",
					"Field Validator that checks if the short specified is within a certain range." },
			{ "stringlength",
					"StringLengthFieldValidator checks that a String field is of a certain length." },
			{ "url",
					"URLValidator checks that a given field is a String and a valid URL." },
			{
					"visitor",
					"The VisitorFieldValidator allows you to forward validation to object properties of your action using the object's own validation files." },
			{
					"conditionalvisitor",
					"The ConditionalVisitorFieldValidator will forward validation to the VisitorFieldValidator only if the expression will evaluate to true." } };
}
