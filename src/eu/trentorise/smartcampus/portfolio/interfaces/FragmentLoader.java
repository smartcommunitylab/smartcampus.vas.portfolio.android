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
package eu.trentorise.smartcampus.portfolio.interfaces;

import android.os.Bundle;


/**
 * Interface that allow to create a mechanism to load and unload fragment in a
 * FragmentActivity through a child Fragment.
 * 
 * @author Simone Casagranda
 * 
 */
public interface FragmentLoader {

	/**
	 * Allows to load a Fragment keeping trace of old one active.
	 * @param frag the fragment that you want to instance
	 * @param keepOldInStack true if you want to build a stack with old one
	 * @param args null if you won't to pass data to next fragment
	 */
	public void load(Class<?> frag, boolean keepOldInStack, Bundle args);
	
}
