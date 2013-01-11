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
 * Bean that works as container for exams data.
 * 
 * @author Simone Casagranda
 * 
 */
public class StudentExams extends BasicObject implements Parcelable {

	private static final long serialVersionUID = 1L;

	public String userId;
	public String unitnId;
	public List<ExamData> examData;

	public StudentExams() {
	}

	public StudentExams(Parcel in) {
		userId = in.readString();
		unitnId = in.readString();
		// Reading list
		examData = new ArrayList<ExamData>();
		in.readList(examData, null);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userId);
		dest.writeString(unitnId);
		dest.writeList(examData);
	}

}
