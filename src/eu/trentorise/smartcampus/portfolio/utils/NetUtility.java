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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Utility class which works with network.
 * 
 * @author Simone Casagranda
 * 
 */
public class NetUtility {

	private NetUtility() {
		throw new AssertionError("You have to use static methods");
	}

	public static Bitmap loadBitmapfromUrl(String imageUrl) {
		Bitmap resultImage = null;
		HttpGet getRequest = new HttpGet();
		try {
			URI imageURI = new URI(imageUrl);
			getRequest.setURI(imageURI);
			HttpClient httpClient = HttpClientFactory.INSTANCE.getThreadSafeHttpClient();
			HttpResponse response = httpClient.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w(NetUtility.class.getName(), "Error: " + statusCode	+ " image url: " + imageUrl);
			} else {
				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = null;
					try {
						inputStream = new FlushedInputStream(entity.getContent());
						resultImage = BitmapFactory.decodeStream(inputStream);
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			}
		} catch (Exception e) {
			getRequest.abort();
			e.printStackTrace();
			Log.w(NetUtility.class.getName(), "Error image url: " + imageUrl);
		}
		return resultImage;
	}

	private static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int byteRead = read();
					if (byteRead < 0) {
						break;
					} else {
						bytesSkipped = 1; 
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

}
