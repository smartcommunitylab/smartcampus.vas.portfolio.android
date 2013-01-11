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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that works as validator for passed strings
 * 
 * @author Simone Casagranda
 *
 */
public class TextValidator {
	 
	  private static final String EMAIL = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	  
	  public static boolean isAValidEmail(CharSequence input){
		  Pattern pattern = Pattern.compile(EMAIL);
		  Matcher matcher = pattern.matcher(input);
		  return matcher.matches();
	  }
}
