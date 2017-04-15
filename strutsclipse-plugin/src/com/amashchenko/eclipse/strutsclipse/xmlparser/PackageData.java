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
package com.amashchenko.eclipse.strutsclipse.xmlparser;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.IDocument;

import com.amashchenko.eclipse.strutsclipse.JarEntryStorage;

public class PackageData {
	private final IDocument document;
	private final JarEntryStorage jarEntryStorage;

	private String name;
	private Set<String> extending;
	private List<TagRegion> tagRegions;

	private ElementRegion region;

	public PackageData(IDocument document) {
		this.document = document;
		this.jarEntryStorage = null;
	}

	public PackageData(JarEntryStorage jarEntryStorage) {
		this.document = null;
		this.jarEntryStorage = jarEntryStorage;
	}

	public PackageData(PackageData packageData) {
		this.document = packageData.getDocument();
		this.jarEntryStorage = packageData.getJarEntryStorage();
	}

	public IDocument getDocument() {
		return document;
	}

	public JarEntryStorage getJarEntryStorage() {
		return jarEntryStorage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getExtending() {
		return extending;
	}

	public void setExtending(Set<String> extending) {
		this.extending = extending;
	}

	public List<TagRegion> getTagRegions() {
		return tagRegions;
	}

	public void setTagRegions(List<TagRegion> tagRegions) {
		this.tagRegions = tagRegions;
	}

	public ElementRegion getRegion() {
		return region;
	}

	public void setRegion(ElementRegion region) {
		this.region = region;
	}
}
