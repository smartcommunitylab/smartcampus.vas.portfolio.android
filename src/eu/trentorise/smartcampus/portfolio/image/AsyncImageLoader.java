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
package eu.trentorise.smartcampus.portfolio.image;

import eu.trentorise.smartcampus.portfolio.utils.NetUtility;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Simple AsyncTask that loads a picture in a thread far from UI thread.
 * 
 * @author Simone Casagranda
 *
 */
public class AsyncImageLoader extends AsyncTask<String,Void,Bitmap> {
	
	private static final BitmapCache<String> imageCache = new BitmapCache<String>(); 
	/*
	 * Id for no resource
	 */
	private final static int NO_RESOURCE = -1;
	
	private String imageUrl;
	private ImageView imageView;
	private ProgressBar progressBar;
	
	private int defaultImageResource = NO_RESOURCE;
	private Bitmap defaultImage;
	
	/**
	 * Create ad AsyncImageLoader without ProgressBar
	 * 
	 * @param imageUrl The Url of the image to load
	 * @param imageView The ImageView where the image is shown when loaded
	 */
	public AsyncImageLoader(String imageUrl, ImageView imageView){
		this(imageUrl,imageView,null);
	}	
	
	/**
	 * Create ad AsyncImageLoader with ProgressBar
	 * 
	 * @param imageUrl The Url of the image to load
	 * @param imageView The ImageView where the image is shown when loaded
	 * @param progressBar The ProgressBar for loading
	 */	
	public AsyncImageLoader(String imageUrl, ImageView imageView,ProgressBar progressBar){
		this.imageUrl=imageUrl;
		this.imageView=imageView;
		this.progressBar=progressBar;
	}

	@Override
	protected void onPreExecute() {
		imageView.setImageBitmap(null);
		// We check for the Bitmap in cache
		Bitmap resultBitmap = imageCache.get(imageUrl);
		if(resultBitmap!=null && !isCancelled()){
			imageView.setImageBitmap(resultBitmap);
			hideProgressBar();
			cancel(true);
		}else if(progressBar!=null && !isCancelled()){
			// Here we show loading image rotating
			showProgressBar();
			// Show start image
			if(defaultImageResource!=NO_RESOURCE){
				imageView.setImageResource(defaultImageResource);
			}else if(defaultImage!=null){
				imageView.setImageBitmap(defaultImage);
			}			
		}
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
		// We check for the Bitmap in cache
		Bitmap resultBitmap = imageCache.get(imageUrl);
		if(resultBitmap==null && !isCancelled()){
			// We load image
			resultBitmap = NetUtility.loadBitmapfromUrl(imageUrl);
			// We save in cache
			imageCache.put(imageUrl, resultBitmap);
		}else{
			// We have the image into cache so we do anything
		}
		return resultBitmap;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		// We hide ProgressBar if present
		// Here we show loading image rotating
		hideProgressBar();		
		// Here we set the image on the ImageView
		if(result!=null){
			imageView.setImageBitmap(result);
		}
	}

	/**
	 * @param defaultImage The image to show when loading starts
	 */
	public void setDefaultImage(Bitmap defaultImage) {
		this.defaultImage = defaultImage;
	}
	
	/**
	 * @param defaultImageResource The image to show when loading starts as resource
	 */	
	public void setDefaultImageResource(int defaultImageResource) {
		this.defaultImageResource = defaultImageResource;
	}
	
	/*
	 * Utility to show ProgressBar if present
	 */
	private void showProgressBar(){
		if(progressBar!=null){
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	/*
	 * Utility to hide ProgressBar if present
	 */	
	private void hideProgressBar(){
		if(progressBar!=null){
			progressBar.setVisibility(View.GONE);
		}
	}	

}
