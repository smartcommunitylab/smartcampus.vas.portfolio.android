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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * Class that allows you to store and retrieve option item in option menu.
 * It gives a good support if you use OptionItem list in bundles.
 * 
 * @author Simone Casagranda
 *
 */
public class OptionItem implements Parcelable{
	
	public int id;
	public int icon;
	public int res;
	
	public OptionItem(int id, int icon, int res) {
		this.id = id;
		this.res = res;
		this.icon = icon;
	}
	
	public OptionItem(Parcel in){
		id = in.readInt();
		icon = in.readInt();
		res = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(icon);
		dest.writeInt(res);
	}
}
