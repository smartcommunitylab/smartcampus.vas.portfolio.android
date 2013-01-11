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
package eu.trentorise.smartcampus.portfolio.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Utility class that simplifies view animations through static methods.
 * 
 * @author Simone Casagranda
 *
 */
public class ViewAnimator {
	
	/**
	 * Allows you to perform an animation on a passed view.
	 * @param context
	 * @param view
	 * @param animationId
	 */
	public static void animate(Context context, View view, int animationId){
		Animation animation = AnimationUtils.loadAnimation(context, animationId);
		view.startAnimation(animation);
	}

	/**
	 * Allows you to inflate an animation in a passed view.
	 * @param context
	 * @param view
	 * @param animationId
	 */
	public static void inflate(Context context, View view, int animationId){
		Animation animation = AnimationUtils.loadAnimation(context, animationId);
		view.setAnimation(animation);
	}
}
