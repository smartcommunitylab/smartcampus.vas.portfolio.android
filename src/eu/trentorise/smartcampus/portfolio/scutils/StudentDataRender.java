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
package eu.trentorise.smartcampus.portfolio.scutils;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import it.smartcampuslab.portfolio.R;
import eu.trentorise.smartcampus.portfolio.models.StudentData;
import eu.trentorise.smartcampus.portfolio.utils.ReflectionUtils;

/**
 * Utility class that simplifies rendering of user student data elements.
 * 
 * @author Simone Casagranda
 * 
 */
public class StudentDataRender {

	/**
	 * Class that contains render string for Personal Info
	 */
	public static class PersonalInfo {
		public String title, subtitle;
		public String interestedFields;
	}

	public static String[] renderizablePersonalInfoElements(String input) {
		// Matching field
		if (Constants.NAME.equalsIgnoreCase(input)) {
			return new String[] { Constants.NAME, Constants.SURNAME };
		} else if (Constants.NATION.equalsIgnoreCase(input)) {
			return new String[] { input };
		} else if (Constants.CFU_CFU_TOTAL.equalsIgnoreCase(input)) {
			return new String[] { Constants.CFU, Constants.CFU_TOTAL };
		} else if (Constants.MARKS_AVERAGE.equalsIgnoreCase(input)) {
			return new String[] { input };
		} else if (Constants.GENDER.equalsIgnoreCase(input)) {
			return new String[] { input };
		} else if (Constants.DATE_OF_BIRTH.equalsIgnoreCase(input)) {
			return new String[] { input };
		} else if (Constants.ACADEMIC_YEAR.equalsIgnoreCase(input)) {
			return new String[] { Constants.ACADEMIC_YEAR};
		} else if (Constants.ENROLLMENT_YEAR .equalsIgnoreCase(input)) {
			return new String[] { Constants.ENROLLMENT_YEAR};
		} else if (Constants.PHONE.equalsIgnoreCase(input)) {
			return new String[] { Constants.PHONE};
		} else if (Constants.MOBILE.equalsIgnoreCase(input)) {
			return new String[] { Constants.MOBILE};
		} else if (Constants.ADDRESS.equalsIgnoreCase(input)) {
			return new String[] { Constants.ADDRESS};
		} else if (Constants.CDS.equalsIgnoreCase(input)) {
			return new String[] { input};
		} else if (Constants.MARKS.equalsIgnoreCase(input)) {
			return new String[] { input};
		} else {
			return null;
		}
	}

	/**
	 * Render title
	 * 
	 * @param context
	 * @param data
	 * @return
	 */
	public static PersonalInfo renderPersonalInfo(Context context, StudentData data, String[] fields) {
		PersonalInfo info = new PersonalInfo();
		StringBuilder sb = new StringBuilder();
		String[] values = new String[fields.length];
		// Retrieving values
		for (int i = 0; i < values.length; i++) {
			String field = fields[i];
			Object v = ReflectionUtils.getValue(StudentData.class, data, field);
			if (v != null) {
				values[i] = v.toString();
			} else {
				values = null;
				break;
			}
		}
		// Matching field
		if (values != null) {
			if (Constants.NAME.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.NAME + "," + Constants.SURNAME;
				info.title = context.getString(R.string.name_surname);
				sb.append(values[0]).append(" ").append(values[1]);
			} else if (Constants.NATION.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.NATION;
				info.title = context.getString(R.string.nation);
				sb.append(values[0]);
			} else if (Constants.CFU.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.CFU_CFU_TOTAL;
				info.title = context.getString(R.string.cfu);
				sb.append(values[0]).append("/").append(values[1]);
			} else if (Constants.MARKS_AVERAGE.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.MARKS_AVERAGE;
				info.title = context.getString(R.string.marks_average);
				sb.append(values[0]);
			} else if (Constants.GENDER.equalsIgnoreCase(fields[0])) {
				info.interestedFields = "gender";
				info.title = context.getString(R.string.gender);
				sb.append(values[0].toUpperCase());
			} else if (Constants.DATE_OF_BIRTH.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.DATE_OF_BIRTH;
				info.title = context.getString(R.string.date_of_birth);
				sb.append(values[0]);
			} else if (Constants.ACADEMIC_YEAR.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.ACADEMIC_YEAR;
				info.title = context.getString(R.string.academic_year);
				sb.append(values[0]);
			} else if (Constants.ENROLLMENT_YEAR.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.ENROLLMENT_YEAR;
				info.title = context.getString(R.string.enrollment_year);
				sb.append(values[0]);
			} else if (Constants.CDS.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.CDS;
				info.title = context.getString(R.string.study_course);
				sb.append(values[0]);
			} else if (Constants.PHONE.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.PHONE;
				info.title = context.getString(R.string.phone);
				sb.append(values[0]);
			} else if (Constants.MOBILE.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.MOBILE;
				info.title = context.getString(R.string.mobile);
				sb.append(values[0]);
			} else if (Constants.ADDRESS.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.ADDRESS;
				info.title = context.getString(R.string.address);
				sb.append(values[0]);
			} else if (Constants.MARKS.equalsIgnoreCase(fields[0])) {
				info.interestedFields = Constants.MARKS;
				info.title = context.getString(R.string.marks);
				sb.append(values[0]);
			}
			// Setting subtitle
			info.subtitle = sb.toString();
		} else {
			info = null;
		}
		// Return render data
		return info;
	}

	public static void renderViewPersonalInfo(TextView title, TextView subtitle, ImageButton cherry, PersonalInfo info) {
		title.setVisibility(View.VISIBLE);
		subtitle.setVisibility(View.VISIBLE);
		cherry.setVisibility(View.GONE);
		title.setText(info.title);
		subtitle.setText(info.subtitle);
	}

}
