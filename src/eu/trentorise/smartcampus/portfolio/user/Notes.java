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
package eu.trentorise.smartcampus.portfolio.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Class that allows user to take care of notes.
 * 
 * @author Simone Casagranda
 *
 */
public class Notes {

	// Preferences name
	private static final String PREF_NAME = "PREF_NOTES";

	private static final String KEY_LAST_UPDATE = "KEY_LAST_UPDATE";
	private static final String KEY_NOTES = "KEY_NOTES";
	
	/** Retrieves the last store update */
	public static long lastUpdate(Context context){
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return pref.getLong(KEY_LAST_UPDATE, 0);
	}
	
	/** Retrieves the stored notes by user */
	public static String getNotes(Context context){
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return pref.getString(KEY_NOTES, "");
	}

	/** Stores user notes */
	public static void setNotes(Context context, String notes){
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
		edit.putString(KEY_NOTES, notes);
		edit.apply();		
	}
	
}
