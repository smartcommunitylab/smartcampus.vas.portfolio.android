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
package eu.trentorise.smartcampus.common;

import eu.trentorise.smartcampus.common.LauncherException.Status;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Utility class that allows you to inspect application presence and interactions. 
 * 
 * @author Simone Casagranda
 *
 */
public final class AppInspector {
	
	// Context reference to avoid huge method signature
	private Context mContext;
	private PackageManager mPackageManager;

	// ======================================================================= //
	// CONSTRUCTOR
	// ======================================================================= //
	
	public AppInspector(Context context) {
		assert context != null;
		mContext = context;
		mPackageManager = mContext.getPackageManager();
	}

	// ======================================================================= //
	// METHODS
	// ======================================================================= //

	/**
	 * Checks if an application is installed on the current device returning its info.
	 * It's strictly control application User Id and application signature
	 * 
	 * @throws LauncherException if it's a pirate copy or doesn't have a valid User Id
	 */
	public ApplicationInfo isAppInstalled(String app) throws LauncherException{
		ApplicationInfo info = null;
		try {
			info = mPackageManager.getApplicationInfo(app, 0);
			// Checking application User ID
			if(mContext.getApplicationInfo().uid != info.uid){
				throw new LauncherException(Status.NOT_VALID_UID);
			}
			// Checking application signature
			if(mPackageManager.checkSignatures(mContext.getPackageName(), info.packageName) < PackageManager.SIGNATURE_MATCH){
				throw new LauncherException(Status.NOT_VALID_SIGNATURE);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			// Launching a new exception for upper levels
			throw new LauncherException(Status.NOT_FOUND);
		}
		// Retrieves null or the info referred to a particular application
		return info;
	}
	
	/**
	 * Utility method that allows to know if an application is updated or not
	 */
	public boolean isAppUpdated(String app, int versionCode) throws LauncherException{
		boolean res = false;
		ApplicationInfo appInfo = isAppInstalled(app);
		PackageInfo packInfo;
		try {
			packInfo = mContext.getPackageManager().getPackageInfo(appInfo.packageName, 0);
			res = packInfo.versionCode >= versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * Tries to launch an application passing parameters after checks on application validity and presence.
	 * 
	 * @throws LauncherException if application is not installed, not valid signed or with different User Id
	 */
	public void launchApp(String app, String action, String category, Bundle bundle) throws LauncherException{
		ApplicationInfo info = isAppInstalled(app);
		// Checking that it's different from null
		if(info != null){
			Intent intent = TextUtils.isEmpty(action)? new Intent() : new Intent(action);
			intent.setPackage(info.packageName);
			if(!TextUtils.isEmpty(category)){
				intent.addCategory(category);
			}
			if(bundle != null){
				intent.putExtras(bundle);
			}
			mContext.startActivity(intent);
		}
	}
	
	/**
	 * Tries to get a start Intent that grants you to launch the correct application
	 * 
	 * @throws LauncherException if application is not installed, not valid signed or with different User Id
	 */
	public Intent getStartIntent(String app, String action, String category, Bundle bundle) throws LauncherException{
		ApplicationInfo info = isAppInstalled(app);
		Intent intent = null;
		// Checking that it's different from null
		if(info != null){
			intent = TextUtils.isEmpty(action)? new Intent() : new Intent(action);
			intent.setPackage(info.packageName);
			if(!TextUtils.isEmpty(category)){
				intent.addCategory(category);
			}
			if(bundle != null){
				intent.putExtras(bundle);
			}
		}
		// Serving result
		return intent;
	}
	
}
