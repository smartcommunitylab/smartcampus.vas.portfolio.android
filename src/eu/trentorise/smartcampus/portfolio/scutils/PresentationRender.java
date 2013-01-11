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

import eu.trentorise.smartcampus.portfolio.image.AsyncImageLoader;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Render for presentation fragment
 * 
 * @author Simone Casagranda
 * 
 */
public class PresentationRender {

	public static class Holder {
		public TextView title, subtitle;
		public ImageView image;
		public VideoView video;
		public ImageButton eyeButton;
		public ProgressBar progressBar;
		public AsyncImageLoader imageLoader;
	}

	/**
	 * Render view related to holder. Check result for action to do.
	 */
	public static void render(Holder holder, UserProducedData data) {
		// Hiding progressbar
		holder.progressBar.setVisibility(View.GONE);
		// Stopping video
		holder.video.stopPlayback();
		// Checking image loader
		if(holder.imageLoader != null && !holder.imageLoader.isCancelled()){
			holder.imageLoader.cancel(true);
			holder.imageLoader = null;
		}
		// Matching type
		if (Constants.RAW.equalsIgnoreCase(data.type)) {
			// We hide all views
			holder.title.setVisibility(View.GONE);
			holder.subtitle.setVisibility(View.VISIBLE);
			holder.image.setVisibility(View.GONE);
			holder.video.setVisibility(View.GONE);
			// Setting values
			holder.subtitle.setText(data.content);
		} else if (Constants.SIMPLE.equalsIgnoreCase(data.type)) {
			// We hide all views
			holder.title.setVisibility(View.VISIBLE);
			holder.subtitle.setVisibility(View.VISIBLE);
			holder.image.setVisibility(View.GONE);
			holder.video.setVisibility(View.GONE);
			// Setting values
			holder.title.setText(data.title);
			holder.subtitle.setText(data.subtitle);
		} else if (Constants.SIMPLE_PIC.equalsIgnoreCase(data.type)) {
			// We hide all views
			holder.title.setVisibility(View.VISIBLE);
			holder.subtitle.setVisibility(View.VISIBLE);
			holder.image.setVisibility(View.VISIBLE);
			holder.video.setVisibility(View.GONE);
			// Setting values
			holder.title.setText(data.title);
			holder.subtitle.setText(data.subtitle);
			holder.imageLoader = new AsyncImageLoader(data.content, holder.image, holder.progressBar);
			holder.imageLoader.execute();
		} else if (Constants.VIDEO.equalsIgnoreCase(data.type)) {
			// We hide all views
			holder.title.setVisibility(View.GONE);
			holder.subtitle.setVisibility(View.GONE);
			holder.image.setVisibility(View.VISIBLE);
			holder.video.setVisibility(View.GONE);
			holder.imageLoader = new AsyncImageLoader(data.content, holder.image, holder.progressBar);
			holder.imageLoader.execute();
		} else {
			// We hide all views
			holder.title.setVisibility(View.GONE);
			holder.subtitle.setVisibility(View.GONE);
			holder.image.setVisibility(View.GONE);
			holder.video.setVisibility(View.GONE);
		}
	}

}
