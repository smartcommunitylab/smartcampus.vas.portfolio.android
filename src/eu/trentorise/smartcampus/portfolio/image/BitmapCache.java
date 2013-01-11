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

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * BitmapCache that works as simple cache for bitmaps.
 * 
 * @author Simone Casagranda
 *
 */
public class BitmapCache<K> {
	
	/*
	 * Riferimento alla cache
	 */
	private Map<K, SoftReference<Bitmap>> bitmapCache = new LinkedHashMap<K, SoftReference<Bitmap>>();
	
	public synchronized Bitmap get(K key){
		SoftReference<Bitmap> reference = bitmapCache.get(key);
		if(reference!=null){
			return reference.get();
		}else{
			return null;
		}
	}
	
	public synchronized void put(K key,Bitmap bitmap){
		SoftReference<Bitmap> refBitmap = new SoftReference<Bitmap>(bitmap);
		bitmapCache.put(key, refBitmap);
	}	
	
	public synchronized void putWithPersistence(Context context,K key,Bitmap bitmap){
		put(key,bitmap);
	}	
	
	public synchronized boolean isCached(K key){
		SoftReference<Bitmap> reference = bitmapCache.get(key);
		if(reference!=null){
			if(reference.get()==null){
				bitmapCache.remove(key);
			}
			return reference.get()!=null;
		}else{
			return false;
		}		
	}

}
