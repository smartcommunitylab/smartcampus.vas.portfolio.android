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
 * Bean that contains all the data related to student.
 * 
 * @author Simone Casagranda
 * 
 */
public class StudentData extends BasicObject implements Parcelable {

	private static final long serialVersionUID = 1L;

	public String fiscalCode;
	public String name;
	public String surname;
	public String enrollmentYear;
	public String nation;
	public String academicYear;
	public String supplementaryYears;
	public String cfu;
	public String cfuTotal;
	public String marksNumber;
	public String marksAverage;
	public String gender;
	public String dateOfBirth;
	public String phone;
	public String mobile;
	public String address;
	public String cds;

	public StudentData() {
	}

	public StudentData(Parcel in) {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getId());
		dest.writeString(fiscalCode);
		dest.writeString(name);
		dest.writeString(surname);
		dest.writeString(enrollmentYear);
		dest.writeString(nation);
		dest.writeString(academicYear);
		dest.writeString(supplementaryYears);
		dest.writeString(cfu);
		dest.writeString(cfuTotal);
		dest.writeString(marksNumber);
		dest.writeString(marksAverage);
		dest.writeString(gender);
		dest.writeString(dateOfBirth);
		dest.writeString(phone);
		dest.writeString(mobile);
		dest.writeString(address);
		dest.writeString(cds);
	}

}
