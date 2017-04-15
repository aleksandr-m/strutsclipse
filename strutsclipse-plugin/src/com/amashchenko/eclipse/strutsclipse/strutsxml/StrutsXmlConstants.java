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

public class StrutsXmlConstants {
	public static final String CONSTANT_TAG = "constant";
	public static final String BEAN_TAG = "bean";
	public static final String INCLUDE_TAG = "include";
	public static final String PACKAGE_TAG = "package";
	public static final String INTERCEPTOR_TAG = "interceptor";
	public static final String INTERCEPTOR_STACK_TAG = "interceptor-stack";
	public static final String INTERCEPTOR_REF_TAG = "interceptor-ref";
	public static final String DEFAULT_INTERCEPTOR_REF_TAG = "default-interceptor-ref";
	public static final String DEFAULT_ACTION_REF_TAG = "default-action-ref";
	public static final String ACTION_TAG = "action";
	public static final String RESULT_TAG = "result";
	public static final String RESULT_TYPE_TAG = "result-type";
	public static final String PARAM_TAG = "param";

	public static final String METHOD_ATTR = "method";
	public static final String CLASS_ATTR = "class";
	public static final String NAME_ATTR = "name";
	public static final String TYPE_ATTR = "type";
	public static final String VALUE_ATTR = "value";
	public static final String EXTENDS_ATTR = "extends";
	public static final String NAMESPACE_ATTR = "namespace";
	public static final String SCOPE_ATTR = "scope";
	public static final String FILE_ATTR = "file";

	public static final String DISPATCHER_RESULT = "dispatcher";
	public static final String REDIRECT_ACTION_RESULT = "redirectAction";
	public static final String TILES_RESULT = "tiles";
	public static final String FREEMARKER_RESULT = "freemarker";

	public static final String LOCATION_PARAM = "location";
	public static final String ACTION_NAME_PARAM = "actionName";

	public static final String STRUTS_DEFAULT_FILE_NAME = "struts-default.xml";
	public static final String STRUTS_PLUGIN_FILE_NAME = "struts-plugin.xml";
	public static final String STRUTS_FILE_NAME = "struts.xml";

	public static final String MULTI_VALUE_SEPARATOR = ",";

	public static final String CONSTANT_CUSTOM_RESOURCES = "struts.custom.i18n.resources";

	public static final String[][] DEFAULT_METHODS = { { "execute", null },
			{ "input", null } };
	public static final String[][] DEFAULT_RESULT_NAMES = {
			{ "success", null }, { "input", null }, { "error", null },
			{ "login", null } };
	public static final String[][] DEFAULT_BEAN_SCOPES = {
			{ "default",
					"One instance per injection. Removed since 2.5 use 'prototype' instead." },
			{ "prototype", "One instance per injection. Since 2.5" },
			{ "singleton",
					"One instance per container. This is the default scope." },
			{ "request", "One instance per request." },
			{ "session", "One instance per session." },
			{
					"thread",
					"One instance per thread. Note: if a thread local object strongly references its Container, neither the Container nor the object will be eligible for garbage collection, i.e. memory leak." } };
	public static final String[][] DEFAULT_RESULT_TYPES = {
			{
					DISPATCHER_RESULT,
					"Includes or forwards to a view (usually a jsp). Behind the scenes Struts will use a RequestDispatcher, where the target servlet/JSP receives the same request/response objects as the original servlet/JSP." },
			{
					REDIRECT_ACTION_RESULT,
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

	public static final String[][] DEFAULT_CONSTANTS = {
			{
					"struts.devMode",
					"When set to true, Struts will act much more friendly for developers. This includes: - struts.i18n.reload = true, - struts.configuration.xml.reload = true, - raising various debug or ignorable problems to errors." },
			{ "struts.i18n.encoding",
					"The encoding to use for localization messages." },
			{ "struts.locale", "The default locale for the Struts application." },
			{ CONSTANT_CUSTOM_RESOURCES,
					"Location of additional localization properties files to load." },
			{
					"struts.action.excludePattern",
					"Comma separated list of patterns (java.util.regex.Pattern) to be excluded from Struts2-processing." },
			{ "struts.mapper.action.prefix.enabled",
					"Set support for action: prefix." },
			{ "struts.mapper.action.prefix.crossNamespaces",
					"Set access to actions in other namespace than current with action: prefix." },
			{
					"struts.mapper.alwaysSelectFullNamespace",
					"Whether to always select the namespace to be everything before the last slash or not." },
			{
					"struts.enable.SlashesInActionNames",
					"Set this to true if you wish to allow slashes in your action names.  If false, Actions names cannot have slashes, and will be accessible via any directory prefix.  This is the traditional behavior expected of WebWork applications. Setting to true is useful when you want to use wildcards and store values in the URL, to be extracted by wildcard patterns, such as <action name=\"*/*\" method=\"{2}\" class=\"actions.{1}\"> to match \"/foo/edit\" or \"/foo/save\"." },
			{ "struts.patternMatcher",
					"The com.opensymphony.xwork2.util.PatternMatcher implementation class." },
			{ "struts.multipart.maxSize",
					"The maximize size of a multipart request (file upload)." },
			{ "struts.multipart.saveDir",
					"The directory to use for storing uploaded files." },
			{
					"struts.multipart.parser",
					"The org.apache.struts2.dispatcher.multipart.MultiPartRequest parser implementation for a multipart request (file upload)." },
			{ "struts.ui.theme", "The default UI template theme." },
			{ "struts.objectFactory",
					"If specified, the default object factory can be overridden." },
			{
					"struts.objectFactory.spring.autoWire",
					"Specifies the autowiring logic when using the SpringObjectFactory. Valid values are: name, type, auto, and constructor (name is the default)." },
			{
					"struts.objectFactory.spring.autoWire.alwaysRespect",
					"Whether the autowire strategy chosen by struts.objectFactory.spring.autoWire is always respected. Defaults to false, which is the legacy behavior that tries to determine the best strategy for the situation." },
			{
					"struts.i18n.reload",
					"When set to true, resource bundles will be reloaded on _every_ request. This is good during development, but should never be used in production." },
			{
					"struts.action.extension",
					"Used by the DefaultActionMapper You may provide a comma separated list, e.g. struts.action.extension=action,jnlp,do The blank extension allows you to match directory listings as well as pure action names without interfering with static resources, which can be specified as an empty string prior to a comma e.g. struts.action.extension=, or struts.action.extension=x,y,z,," },
			{
					"struts.ui.templateDir",
					"The directory containing UI templates.  All templates must reside in this directory." },
			{ "struts.configuration.xml.reload",
					"This will cause the configuration to reload struts.xml when it is changed." },
			{ "struts.serve.static",
					"Whether the Struts filter should serve static content or not." },
			{ "struts.custom.properties",
					"Location of additional configuration properties files to load." },
			{ "struts.unknownHandlerManager",
					"The com.opensymphony.xwork2.UnknownHandlerManager implementation class." },
			{ "struts.override.excludedPatterns",
					"Constant is used to override framework's default excluded patterns." },
			{ "struts.override.acceptedPatterns",
					"Constant is used to override framework's default accepted patterns." },
			{ "struts.additional.excludedPatterns",
					"Additional excluded patterns." },
			{ "struts.additional.acceptedPatterns",
					"Additional accepted patterns." },
			{ "struts.excludedPatterns.checker",
					"Dedicated services to check if passed string is excluded." },
			{ "struts.acceptedPatterns.checker",
					"Dedicated services to check if passed string is accepted." },
			{
					"struts.excludedClasses",
					"Comma delimited set of excluded classes which cannot be accessed via expressions." },
			{
					"struts.excludedPackageNamePatterns",
					"Comma delimited set of excluded package names which cannot be accessed via expressions. This must be valid regex, each '.' in package name must be escaped! It's more flexible but slower than simple string comparison struts.excludedPackageNames." },
			{
					"struts.excludedPackageNames",
					"Comma delimited set of excluded package names which cannot be accessed via expressions. This is simpler version of the struts.excludedPackageNamePatterns used with string comparison." },
			{
					"struts.enable.DynamicMethodInvocation",
					"Set this to false if you wish to disable implicit dynamic method invocation via the URL request. This includes URLs like foo!bar.action, as well as params like method:bar (but not action:foo). An alternative to implicit dynamic method invocation is to use wildcard mappings, such as <action name=\"*/*\" method=\"{2}\" class=\"actions.{1}\">." } };
}
