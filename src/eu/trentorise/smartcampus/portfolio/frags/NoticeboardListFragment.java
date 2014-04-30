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
package eu.trentorise.smartcampus.portfolio.frags;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

import it.smartcampuslab.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;

/**
 * Fragment that visualizes all news to user.
 * 
 * @author Simone Casagranda
 *
 */
public class NoticeboardListFragment extends SherlockListFragment {

	private NoteLayerInteractor mNoteLayerInteractor;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof NoteLayerInteractor){
			mNoteLayerInteractor = (NoteLayerInteractor) activity;
		}else{
			throw new RuntimeException("The container Activity has to be an instance of NoteLayerInteractor");
		}
		if(activity instanceof SharedPortfolio){
			assert ((SharedPortfolio)activity).isOwned();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout into fragment
		View v = inflater.inflate(R.layout.listfrag_noticeboard, null);
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.noticeboard);
		// Showing note layer
		mNoteLayerInteractor.setVisibility(true);
	}
	
}
