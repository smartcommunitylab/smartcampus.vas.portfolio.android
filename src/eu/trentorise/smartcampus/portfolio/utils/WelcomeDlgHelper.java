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

public class WelcomeDlgHelper {

	public static boolean isWelcomeShown(final Context ctx) {
		return ctx.getSharedPreferences("_welcome_dialog_shown", Context.MODE_PRIVATE).getBoolean("shown", false);
//		LayoutInflater inflatter = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
//		View view = inflatter.inflate(R.layout.welcomedialog, null);
//		builder.setView(view);
//		final AlertDialog dialog = builder.create();
//		CheckBox cb = (CheckBox) view.findViewById(R.id.welcomeCheckbox);
//		cb.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				ctx.getSharedPreferences("_welcome_dialog_shown", Context.MODE_PRIVATE).edit().putBoolean("shown", true).commit();
//				dialog.dismiss();
//			}
//		});
//		dialog.show();

	}
	
	public static void setWelcomeShown(final Context ctx) {
		ctx.getSharedPreferences("_welcome_dialog_shown", Context.MODE_PRIVATE).edit().putBoolean("shown", true).commit();
	}
	
}
