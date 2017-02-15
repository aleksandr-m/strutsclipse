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
package com.amashchenko.eclipse.strutsclipse;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import com.amashchenko.eclipse.strutsclipse.xmlparser.ElementRegion;

public class ParseUtil {
	private ParseUtil() {
	}

	// ElementRegion.name = prefix
	public static ElementRegion parseElementValue(final String value,
			String prefix, final String valueSeparator, int valueOffset) {
		String resultValue = value;

		if (valueSeparator != null && !valueSeparator.isEmpty()
				&& value.contains(valueSeparator)) {
			int startSeprIndx = prefix.lastIndexOf(valueSeparator) + 1;

			// first value in `value`
			if (startSeprIndx <= 0) {
				resultValue = value.substring(0, value.indexOf(valueSeparator));
				prefix = prefix.trim();
			} else {
				prefix = prefix.substring(startSeprIndx).trim();

				int endSeprIndx = value.indexOf(valueSeparator, startSeprIndx);
				if (endSeprIndx <= 0) {
					// last value in `value`
					resultValue = value.substring(startSeprIndx);
				} else {
					// somewhere in the middle of `value`
					resultValue = value.substring(startSeprIndx, endSeprIndx);
				}
			}

			valueOffset = valueOffset + startSeprIndx;
		}

		// don't trim not multiple attribute values
		if (valueSeparator != null && !valueSeparator.isEmpty()) {
			valueOffset = valueOffset + countLeftWhitespaces(resultValue);
			resultValue = resultValue.trim();
		}

		return new ElementRegion(prefix, resultValue, valueOffset);
	}

	private static int countLeftWhitespaces(String value) {
		int len = value.length();
		int st = 0;
		char[] val = value.toCharArray();

		while ((st < len) && (val[st] <= ' ')) {
			st++;
		}
		return st;
	}

	public static Set<String> delimitedStringToSet(String str, String delimiter) {
		Assert.isNotNull(delimiter);

		Set<String> set = new HashSet<String>();
		if (str != null) {
			String[] strArr = str.split(delimiter);
			for (String s : strArr) {
				String trimmed = s.trim();
				if (!trimmed.isEmpty()) {
					set.add(trimmed);
				}
			}
		}
		return set;
	}
}
