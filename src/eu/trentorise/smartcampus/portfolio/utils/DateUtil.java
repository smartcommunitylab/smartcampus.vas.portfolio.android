/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.portfolio.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class that allows you to format dates
 * 
 * @author Simone Casagranda
 *
 */
public class DateUtil {

	public static String format(String pattern, long mills){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(new Date(mills));
	}
	
}
