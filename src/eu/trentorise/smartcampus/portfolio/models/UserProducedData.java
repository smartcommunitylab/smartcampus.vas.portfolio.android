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
package eu.trentorise.smartcampus.portfolio.models;

import android.os.Parcel;
import android.os.Parcelable;
import eu.trentorise.smartcampus.storage.BasicObject;

/**
 * Bean for all data produced by user.
 * 
 * @author Simone Casagranda
 * 
 */
public class UserProducedData extends BasicObject implements Parcelable {

	private static final long serialVersionUID = 1L;

	public String userId;
	public String category;
	public long timestamp;
	public String type;
	public String title;
	public String subtitle;
	public String content;

	public UserProducedData() {
	}

	public UserProducedData(Parcel source) {
		setId(source.readString());
		userId = source.readString();
		category = source.readString();
		timestamp = source.readLong();
		type = source.readString();
		title = source.readString();
		subtitle = source.readString();
		content = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getId());
		dest.writeString(userId);
		dest.writeString(category);
		dest.writeLong(timestamp);
		dest.writeString(type);
		dest.writeString(title);
		dest.writeString(subtitle);
		dest.writeString(content);
	}
}
