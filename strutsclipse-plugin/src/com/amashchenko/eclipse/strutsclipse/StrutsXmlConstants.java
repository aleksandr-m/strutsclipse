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

public class StrutsXmlConstants {
	public static final String CONSTANT_TAG = "constant";
	public static final String BEAN_TAG = "bean";
	public static final String PACKAGE_TAG = "package";
	public static final String ACTION_TAG = "action";
	public static final String RESULT_TAG = "result";
	public static final String PARAM_TAG = "param";

	public static final String METHOD_ATTR = "method";
	public static final String CLASS_ATTR = "class";
	public static final String NAME_ATTR = "name";
	public static final String TYPE_ATTR = "type";
	public static final String VALUE_ATTR = "value";
	public static final String EXTENDS_ATTR = "extends";
	public static final String NAMESPACE_ATTR = "namespace";

	public static final String DISPATCHER_RESULT = "dispatcher";
	public static final String TILES_RESULT = "tiles";
	public static final String FREEMARKER_RESULT = "freemarker";

	public static final String[][] DEFAULT_PACKAGE_NAMES = {
			{ "struts-default", null }, { "json-default", null },
			{ "tiles-default", null }, { "rest-default", null },
			{ "convention-default", null }, { "jasperreports-default", null },
			{ "struts-portlet-default", null },
			{ "struts-portlet-tiles-default", null }, { "osgi-default", null },
			{ "oval-default", null } };

	public static final String[][] DEFAULT_METHODS = { { "execute", null },
			{ "input", null } };
	public static final String[][] DEFAULT_RESULT_NAMES = {
			{ "success", null }, { "input", null }, { "error", null },
			{ "login", null } };
	public static final String[][] DEFAULT_RESULT_TYPES = {
			{
					DISPATCHER_RESULT,
					"Includes or forwards to a view (usually a jsp). Behind the scenes Struts will use a RequestDispatcher, where the target servlet/JSP receives the same request/response objects as the original servlet/JSP." },
			{
					"redirectAction",
					"This result uses the ActionMapper provided by the ActionMapperFactory to redirect the browser to a URL that invokes the specified action and (optional) namespace. This is better than the ServletRedirectResult because it does not require you to encode the URL patterns processed by the ActionMapper in to your struts.xml configuration files. This means you can change your URL patterns at any point and your application will still work. It is strongly recommended that if you are redirecting to another action, you use this result rather than the standard redirect result." },
			{
					TILES_RESULT,
					"The tiles result allows actions to return Tiles pages. To use this result type you need to add tiles plugin." },
			{
					"stream",
					"A result type for sending raw data (via an InputStream) directly to the HttpServletResponse. Very useful for allowing users to download content." },
			{
					"json",
					"The JSON plugin provides a 'json' result type that serializes actions into JSON. To use this result type you need to add json plugin." },
			{ FREEMARKER_RESULT,
					"Renders a view using the Freemarker template engine." },
			{
					"httpheader",
					"A result type for setting HTTP headers and status by optionally evaluating against the ValueStack. This result can also be used to send and error to the client. All the parameters can be evaluated against the ValueStack. " },
			{
					"plainText",
					"A result that send the content out as plain text. Useful typically when needed to display the raw content of a JSP or Html file for example." },
			{
					"redirect",
					"Calls the HttpServletResponse#sendRedirect(String) method to the location specified." },
			{ "xslt",
					"XSLTResult uses XSLT to transform an action object to XML." },
			{
					"chain",
					"This result invokes an entire other action, complete with it's own interceptor stack and result. Don't Try This at Home! As a rule, Action Chaining is not recommended. First explore other options, such as the Redirect After Post technique." },
			{
					"postback",
					"A result that renders the current request parameters as a form which immediately submits a postback to the specified destination." } };

}
