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
package com.amashchenko.eclipse.strutsclipse.tilesxml;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.wst.sse.ui.quickoutline.AbstractQuickOutlineConfiguration;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeContentProvider;

public class TilesXmlQuickOutlineConf extends AbstractQuickOutlineConfiguration {

	@Override
	public ILabelProvider getLabelProvider() {
		return new TilesQuickLabelProvider();
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		// FIXME Discouraged access JFaceNodeContentProvider
		return new JFaceNodeContentProvider();
	}
}
