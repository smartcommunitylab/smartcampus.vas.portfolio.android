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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;

/**
 * 
 * Utility class that furnishes utility methods those grant an easy interaction
 * between Portfolios and inner data.
 * 
 * @author Simone Casagranda
 * 
 */
public class PortfolioUtil {

	/**
	 * Wrapper for all information that distinguish elements id by status and
	 * category
	 */
	public class CategorizedData {

		public HashMap<String, List<String>> shownMap = new HashMap<String, List<String>>();
		public HashMap<String, List<String>> cherriesMap = new HashMap<String, List<String>>();
		public HashMap<String, List<String>> hiddenMap = new HashMap<String, List<String>>();

	}

	private Portfolio mPortfolio;
	private CategorizedData mCategorizedData;

	public PortfolioUtil(Context context, Portfolio portfolio) {
		mPortfolio = portfolio;
		mCategorizedData = new CategorizedData();
	}

	public CategorizedData getCategorizedData() {
		return mCategorizedData;
	}

	/**
	 * Initialize a CategorizedData that works as dictionary for user produced
	 * data. NB: it's better if you perform this operation far from UI Thread
	 * because it can take some time.
	 */
	public void initializeItems(List<UserProducedData> producedDatas) {
		// Preparing sets for a fast check into Portfolio
		HashSet<String> shownIds = new HashSet<String>(mPortfolio.showUserGeneratedData);
		HashSet<String> highlightedIds = new HashSet<String>(mPortfolio.highlightUserGeneratedData);
		// Clearing all data
		this.reset();
		// Checking if we have to show and/or highlight a UserProducedData
		for (UserProducedData data : producedDatas) {
			if (shownIds.contains(data.getId())) {
				// Adding element
				addCategoryToMap(mCategorizedData.shownMap, data.category, data.getId());
				// Checking if it is highlighted
				if (highlightedIds.contains(data.getId())) {
					addCategoryToMap(mCategorizedData.cherriesMap, data.category, data.getId());
				}
			} else {
				// Adding element
				addCategoryToMap(mCategorizedData.hiddenMap, data.category, data.getId());
			}
		}
	}

	/**
	 * Allows you to clear internal Categorization
	 */
	public void reset() {
		mCategorizedData.shownMap.clear();
		mCategorizedData.cherriesMap.clear();
		mCategorizedData.hiddenMap.clear();
	}

	// Add a UserProducedData to a map based on its category
	private void addCategoryToMap(HashMap<String, List<String>> map, String category, String id) {
		List<String> elems = map.get(category);
		if (elems == null) {
			elems = new ArrayList<String>();
		}
		elems.add(id);
		map.put(category, elems);
	}

}
