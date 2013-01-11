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
package eu.trentorise.smartcampus.common;

/**
 * Exception that should be risen when an error occurs
 * 
 * @author Simone Casagranda
 *
 */
public final class LauncherException extends Exception {
	
	public static enum Status {UNKNOWN, NOT_FOUND, NOT_VALID_UID, NOT_VALID_SIGNATURE};

	private static final long serialVersionUID = 1L;
	
	private Status mStatus;

	public LauncherException(Status status) {
		mStatus = status;
	}
	
	/**
	 * Retrieves the error cause that has generated exception.
	 */
	public Status getStatus() {
		return mStatus;
	}

}
