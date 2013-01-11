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

import java.lang.reflect.Field;

/**
 * Class that allows you to inspect easily elements into a class
 * 
 * @author Simone Casagranda
 *
 */
public class ReflectionUtils{

	/**
	 * Allows you to obtain data from a field if contained
	 * @param clazz
	 * @param value
	 * @param field
	 * @return
	 */
	public static Object getValue(Class<?> clazz, Object value, String field){
		try{
			// Iterating fields
			for(Field f : clazz.getDeclaredFields()){
				if(f.getName().equals(field)){
					return f.get(value);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
