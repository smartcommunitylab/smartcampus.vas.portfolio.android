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
 * Bean that contains all the data related to an exam.
 * 
 * @author Simone Casagranda
 * 
 */
public class ExamData extends BasicObject implements Parcelable {

	private static final long serialVersionUID = 1L;

	public String cod;
	public String name;
	public String result;
	public boolean lode;
	public String weight;
	public long date;

	public ExamData() {
	}

	public ExamData(Parcel in) {
		setId(in.readString());
		cod = in.readString();
		name = in.readString();
		result = in.readString();
		lode = in.readInt() == 0 ? false : true;
		weight = in.readString();
		date = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getId());
		dest.writeString(cod);
		dest.writeString(name);
		dest.writeString(result);
		dest.writeInt(lode ? 1 : 0);
		dest.writeString(weight);
		dest.writeLong(date);
	}

}
