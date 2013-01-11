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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

/**
 * Utility class to manage resources
 * 
 * @author Simone Casagranda
 *
 */
public class RawUtil {
	
	private final static String TAG = "RawUtil";
	private final static int BUFFER = 128;
	
	private RawUtil(){
		throw new AssertionError("You must use static methods!");
	}
	
	/**
	 * Allows you to retrieve the String related to a raw resource 
	 * 
	 * @param context 
	 * @param resId 
	 * @return 
	 */
	public static String getRawAsString(Context context, int resId){
		InputStream is = context.getResources().openRawResource(resId);
		String res = null;
		if(is!=null){
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buf = new byte[BUFFER];
			int numRead = 0;
			try {
				while((numRead = is.read(buf))>=0){
					stream.write(buf, 0, numRead);
				}
				res = new String(stream.toByteArray());
			} catch (IOException e) {
				Log.e(TAG, "Error reading resource from stream");
				e.printStackTrace();
			}finally{
				if(stream!=null){
					try {
						stream.close();
					} catch (IOException e) {
						Log.e(TAG, "Error closing ByteArrayOutputStream");
					}
				}
			}
		}
		return res;
	};
}
