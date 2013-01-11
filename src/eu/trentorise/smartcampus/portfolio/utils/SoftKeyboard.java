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

import android.content.Context;
import android.text.Selection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Utility class that allows you to interact with SoftKeyboard
 * 
 * @author Simone Casagranda
 *
 */
public class SoftKeyboard {
	
	/**
	 * Allows you to hide the SoftKeyboard referred to a passed EditText
	 * 
	 * @param context
	 * @param editText
	 */
	public static void hideSoftKeyboard(Context context, EditText editText){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	
	/**
	 * Allows you to show the SoftKeyboard referred to a passed EditText
	 * 
	 * @param context
	 * @param editText
	 */
	public static void showSoftKeyboard(Context context, EditText editText){
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
		// Moving cursor to last position
		Selection.setSelection(editText.getText(), editText.length());
	}

}
