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

import android.os.Parcel;
import android.os.Parcelable;

import eu.trentorise.smartcampus.storage.BasicObject;

public class SharedPortfolioContainer extends BasicObject implements Parcelable {
	private static final long serialVersionUID = 2876802290985829635L;

	private Portfolio portfolio;
	private StudentInfo studentInfo;
	private ArrayList<StudentExams> sharedStudentExams;
	private ArrayList<UserProducedData> sharedProducedDatas;

	
	public SharedPortfolioContainer() {
		super();
	}
	public Portfolio getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
	public StudentInfo getStudentInfo() {
		return studentInfo;
	}
	public void setStudentInfo(StudentInfo studentInfo) {
		this.studentInfo = studentInfo;
	}
	public ArrayList<StudentExams> getSharedStudentExams() {
		return sharedStudentExams;
	}
	public void setSharedStudentExams(ArrayList<StudentExams> sharedStudentExams) {
		this.sharedStudentExams = sharedStudentExams;
	}
	public ArrayList<UserProducedData> getSharedProducedDatas() {
		return sharedProducedDatas;
	}
	public void setSharedProducedDatas(
			ArrayList<UserProducedData> sharedProducedDatas) {
		this.sharedProducedDatas = sharedProducedDatas;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	
	public SharedPortfolioContainer(Parcel parcel) {
		setPortfolio((Portfolio) parcel.readParcelable(getClass().getClassLoader()));
		setStudentInfo((StudentInfo) parcel.readParcelable(getClass().getClassLoader()));
		setSharedProducedDatas(parcel.readArrayList(getClass().getClassLoader()));
		setSharedStudentExams(parcel.readArrayList(getClass().getClassLoader()));
	} 
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(portfolio, flags);
		dest.writeParcelable(studentInfo, flags);
		if (sharedProducedDatas == null) sharedProducedDatas = new ArrayList<UserProducedData>();
		dest.writeList(sharedProducedDatas);
		if (sharedStudentExams == null) sharedStudentExams = new ArrayList<StudentExams>();
		dest.writeList(sharedStudentExams);
	}
	
	
}
