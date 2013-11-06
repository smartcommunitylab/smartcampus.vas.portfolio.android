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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import eu.trentorise.smartcampus.storage.BasicObject;

/**
 * Bean that represents a Portfolio.
 * 
 * @author Simone Casagranda
 * 
 */
public class Portfolio extends BasicObject implements Parcelable {

	private static final long serialVersionUID = 1L;
	
	public String userId;
	public long timestamp;
	public String name;
	// public String sharedWith;
	public List<Concept> tags;
	public String entityId;
	
	public List<String> showUserGeneratedData = new ArrayList<String>();
	public List<String> highlightUserGeneratedData = new ArrayList<String>();
	public List<String> showStudentInfo = new ArrayList<String>();

	public Portfolio() {
	}

	public Portfolio(Parcel source) {
		setId(source.readString());
		userId = source.readString();
		timestamp = source.readLong();
		name = source.readString();
		entityId = source.readString();
		// sharedWith = source.readString();
		source.readStringList(showUserGeneratedData);
		source.readStringList(highlightUserGeneratedData);
		source.readStringList(showStudentInfo);
		source.readList(tags, getClass().getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getId());
		dest.writeString(userId);
		dest.writeLong(timestamp);
		dest.writeString(name);
		dest.writeString(entityId);
		// dest.writeString(sharedWith);
		dest.writeStringList(showUserGeneratedData);
		dest.writeStringList(highlightUserGeneratedData);
		dest.writeList(showStudentInfo);
		dest.writeList(tags);
	}

}
