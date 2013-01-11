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

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

/**
 * 
 * @author Simone Casagranda
 * 
 */
public enum HttpClientFactory {
	
	INSTANCE;
	
	private final static int TIMEOUT = 25000;
	private final static String HTTP_SCHEMA = "http";
	private final static String HTTPS_SCHEMA = "https";
	
	private HttpClient client;

	private HttpClientFactory() {
		client = createHttpClient();
	}
	
	public HttpClient getThreadSafeHttpClient(){
		if(client==null){
			client = createHttpClient();
		}
		return client;
	}
	
	private final HttpClient createHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, HTTP.DEFAULT_CONTENT_CHARSET);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		Scheme httpScheme = new Scheme(HTTP_SCHEMA, PlainSocketFactory.getSocketFactory(), 80);
		schemeRegistry.register(httpScheme);
		Scheme httpsScheme = new Scheme(HTTPS_SCHEMA, SSLSocketFactory.getSocketFactory(), 443);
		schemeRegistry.register(httpsScheme);
		ClientConnectionManager tsConnManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		HttpClient client = new DefaultHttpClient(tsConnManager, httpParams);
		HttpConnectionParams.setSoTimeout(client.getParams(), TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(client.getParams(),TIMEOUT);
		return client;
	}

}
