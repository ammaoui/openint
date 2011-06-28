/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.historify.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * Helper class for formatting dates.
 * 
 * @author berke.andras
 */
public class DateUtils {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MMM HH:mm");
	
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}
}