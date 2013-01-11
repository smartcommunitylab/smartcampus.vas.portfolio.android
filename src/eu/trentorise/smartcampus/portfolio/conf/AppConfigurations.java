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
package eu.trentorise.smartcampus.portfolio.conf;

import eu.trentorise.smartcampus.portfolio.R;
import android.content.Context;

/**
 * Utility class for app Configurations.
 * 
 * @author Simone Casagranda
 * 
 */
public class AppConfigurations {

	// Preferences name
	// private static final String PREF_NAME = "PREF_CONF";

	private Context mContext;
	
	public AppConfigurations(Context context) {
		mContext = context;
	}
	
	/** Retrieves if the app is running in test mode or not. */
	public boolean isTestModeEnabled(){
		return Boolean.parseBoolean(mContext.getString(R.string.test_mode_enabled));
	}
	
	/** Retrieves the username used for testing. */
	public String getTestUsername(){
		return useEmailAsUsername() ? mContext.getString(R.string.test_email_as_username) : 
									  mContext.getString(R.string.test_username);
	}
	
	/** Retrieves the password used for testing. */
	public String getTestPassword(){
		return mContext.getString(R.string.test_password);
	}
	
	/** Retrieves if the login has to be performed with an email or with a simple username. */
	public boolean useEmailAsUsername(){
		return Boolean.parseBoolean(mContext.getString(R.string.use_email_as_username));
	}

	/** Retrieves the default character use to identify tags. */
	public String getDefaultTagCharacter(){
		return mContext.getString(R.string.default_tag_character);
	}

	/** Retrieves the default semantic character use to identify tags. */
	public String getDefaultSemanticTagCharacter(){
		return mContext.getString(R.string.default_semantic_tag_character);
	}
	
	/** Retrieves the default character use to divide tags. */
	public String getDefaultTagSeparator(){
		return mContext.getString(R.string.default_tag_separator);
	}
	/** Retrieves the default date format */
	public String getDefaultDateFormat(){
		return mContext.getString(R.string.date_format);
	}
}
