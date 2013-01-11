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
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;

/**
 * Utility class that simplifies rendering of user produced elements.
 * 
 * @author Simone Casagranda
 * 
 */
public class UserProducedDataRender {

	/**
	 * Render view in PersonalInfo
	 * 
	 * @param context
	 * @param data
	 * @return
	 */
	public static void renderViewPersonalInfo(TextView separator, TextView title, TextView subtitle,
			ImageButton cherry, UserProducedData data) {
		subtitle.setVisibility(View.VISIBLE);
		// Matching category
		if (Constants.LANGUAGE.equalsIgnoreCase(data.category)) {
			separator.setText(R.string.languages);
			title.setVisibility(View.GONE);
			// Something like "English - Mother language"
			subtitle.setText(data.title + " - " + data.subtitle);
		} else if (Constants.SKILL.equalsIgnoreCase(data.category)) {
			separator.setText(R.string.skills);
			title.setVisibility(View.GONE);
			// Something like "Android"
			subtitle.setText(data.title);
		} else if (Constants.CONTACT.equalsIgnoreCase(data.category)) {
			separator.setText(R.string.contacts);
			cherry.setVisibility(View.GONE);
			title.setVisibility(View.VISIBLE);
			title.setText(data.title + ":");
			subtitle.setText(data.subtitle);
			// Add link to all possible link
			Linkify.addLinks(subtitle, Linkify.ALL);
		} else {
			// Appending default not matched string
			subtitle.setText(data.subtitle);
			// Add link to all possible link
			Linkify.addLinks(subtitle, Linkify.ALL);
		}
	}

	/**
	 * Render view for default
	 * 
	 * @param context
	 * @param data
	 * @return
	 */
	public static void renderTitle(TextView title, UserProducedData data) {
		StringBuilder sb = new StringBuilder();
		// Matching category
		if (Constants.LANGUAGE.equalsIgnoreCase(data.category)) {
			// Something like "English - Mother language"
			sb.append(data.title).append(" - ").append(data.subtitle);
		} else if (Constants.SKILL.equalsIgnoreCase(data.category)) {
			// Something like "Android"
			sb.append(data.title);
		} else if (Constants.CONTACT.equalsIgnoreCase(data.category)) {
			// Something like "Phone: 0123-112233"
			sb.append(data.title).append(": ").append(data.subtitle);
		} else if (Constants.PRESENTATION.equalsIgnoreCase(data.category)) {
			// Checking type
			if (Constants.RAW.equalsIgnoreCase(data.type)) {
				sb.append(data.content);
			}
			if (Constants.SIMPLE.equalsIgnoreCase(data.type)) {
				sb.append(data.title);
			}
			if (Constants.SIMPLE_PIC.equalsIgnoreCase(data.type)) {
				sb.append(data.title);
			}
			if (Constants.SIMPLE_DESC.equalsIgnoreCase(data.type)) {
				sb.append(data.title);
			}
			if (Constants.VIDEO.equalsIgnoreCase(data.type)) {
				sb.append(title.getContext().getString(R.string.video_content));
			}
			if (Constants.SYS_SIMPLE.equalsIgnoreCase(data.type)) {
				sb.append(data.title);
			}
		}/*
		 * else if(Constants.OVERVIEW.equalsIgnoreCase(data.category)){
		 * 
		 * }else if(Constants.EDUCATION.equalsIgnoreCase(data.category)){
		 * 
		 * }else if(Constants.PROFESSIONAL.equalsIgnoreCase(data.category)){
		 * 
		 * }else if(Constants.ABOUT.equalsIgnoreCase(data.category)){
		 * 
		 * }
		 */else {
			// Appending default not matched string
			sb.append(data.title == null ? "" : data.title);
		}
		// Setting text
		title.setText(sb.toString());
		// Add link to all possible link
		Linkify.addLinks(title, Linkify.ALL);
	}

	/**
	 * Render the default title of ActionBar
	 * 
	 * @param context
	 * @param category
	 * @return
	 */
	public static String renderActionBarTitle(Context context, String category) {
		// Matching category
		if (Constants.LANGUAGE.equalsIgnoreCase(category)) {
			return context.getString(R.string.languages);
		} else if (Constants.SKILL.equalsIgnoreCase(category)) {
			return context.getString(R.string.skills);
		} else if (Constants.CONTACT.equalsIgnoreCase(category)) {
			return context.getString(R.string.contacts);
		} else if (Constants.PRESENTATION.equalsIgnoreCase(category)) {
			return context.getString(R.string.presentation);
		} else if (Constants.OVERVIEW.equalsIgnoreCase(category)) {
			return context.getString(R.string.overview);
		} else if (Constants.EDUCATION.equalsIgnoreCase(category)) {
			return context.getString(R.string.study);
		} else if (Constants.PROFESSIONAL.equalsIgnoreCase(category)) {
			return context.getString(R.string.work);
		} else if (Constants.ABOUT.equalsIgnoreCase(category)) {
			return context.getString(R.string.more_about_me);
		} else {
			return context.getString(R.string.app_name);
		}
	}

}
