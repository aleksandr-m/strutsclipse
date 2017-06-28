# Changelog

## 1.4.0

* Added content assist for `getText` in JSP
* Added hyperlinks for `getText` in JSP
* Added content assist for `theme` attribute in JSP
* Added ability to jump to the action's validation XML files or `validate` methods in actions classes
* Improved path matching in completion proposals

## 1.3.0

* Added content assist for JSP `text` tag `name` attribute
* Added hyperlinks for JSP `text` tag `name` attribute
* Added hyperlinks for `interceptor-ref` tag `name` attribute
* Added hyperlinks for `default-interceptor-ref` tag `name` attribute
* Added hyperlinks for `result` tag `type` attribute
* Added icon for taglib proposals
* Various improvements

## 1.2.3

* Add content assist for JSP `include` tag `value` attribute
* Improve finding project resources

## 1.2.2

* Improve hyperlinks for `package` tag `extends` attribute to search for package names in other Struts configuration files
* Improve package name finding for content assist in `package` tag `extends` attribute
* Improve action name finding for content assist in Struts JSP tags

## 1.2.1

* Avoid potential NPE on traversing project resources

## 1.2.0

* Added support for Struts tags in JSP
* Contents assist and hyperlinks for `default-action-ref` tag `name` attribute
* Support Struts 2.5 feature to have multiple values in the `result` tag `name` attribute

## 1.1.0

* Content assist for `interceptor-ref` tag `name` attribute
* Content assist for `default-interceptor-ref` tag `name` attribute
* Branding for Tiles feature
* Added new set of icons
* Added quick outline icons for Tiles feature

## 1.0.9

* Branding (About Eclipse dialog)
* Validation of duplicate `constant` names
* Added ability to get resources from jar files
* Hyperlinks for `package` tag `extends` attribute
* Added line number to validator messages
* Apache Tiles support
* Ignore commented out sections in validation

## 1.0.8

* Validation of duplicate package names in struts.xml
* Content assist for `bean` tag `class` and `scope` attributes
* Content assist for `redirectAction` result namespace `param`
* Handling of namespace `param` tag in `result` tag
* Updated Tycho to 0.24.0

## 1.0.7

* Validation of struts.xml
* Filtering out current package name in `package` tag `extends` attribute proposals
* Content type for struts.xml
* Improved xml parsing
* Content assist for `constant` tag `name` attribute

## 1.0.6

* Added local package names to `package` tag `extends` attribute proposals
* Fixed scanning project output folder for Tiles configuration files
* Added sorting for content assist proposals

## 1.0.5

* Hyperlinks for action name in `redirectAction` result
* Content assist for `redirectAction` result
* Fixed issue with null-s in hyperlinks list
* Improved Quick Outline labels
* Hyperlinks for `result` tag body and location `param`

## 1.0.4

* Content assist for `result` tag `param` location
* Improved xml parser
* Added Tiles xml parser
* Content assist for `result` tag body
