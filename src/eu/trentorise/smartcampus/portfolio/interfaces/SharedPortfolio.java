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
package eu.trentorise.smartcampus.portfolio.interfaces;

import eu.trentorise.smartcampus.portfolio.models.SharedPortfolioContainer;

/**
 * 
 * Interface that allows you to set the lock or unlock of user data.
 * It's useful when an external user reads user info.
 * 
 * @author simone casagranda
 *
 */
public interface SharedPortfolio {

	public boolean isOwned();
	
	public int getPermissionLevel();
	
	public String getPortfolioEntityId();
	
	public SharedPortfolioContainer getContainer();
}
