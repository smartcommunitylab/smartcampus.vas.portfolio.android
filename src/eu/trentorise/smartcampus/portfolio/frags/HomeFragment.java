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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.FragmentLoader;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;

/**
 * Fragment that contains all the buttons related to actions available for user.
 * 
 * @author Simone Casagranda
 * 
 */
public class HomeFragment extends SherlockFragment {

	// UI references
	private Button mPortfoliosButton, mNoticeboardButton;

	private FragmentLoader mFragmentLoader;
	private NoteLayerInteractor mNoteLayerInteractor;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof FragmentLoader){
			mFragmentLoader = (FragmentLoader) activity;
		}else{
			throw new RuntimeException("The container Activity has to be an instance of FragmentLoader");
		}
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
		View v = inflater.inflate(R.layout.frag_home, null);
		// Retrieving views
		mPortfoliosButton = (Button) v.findViewById(R.id.my_portfolios_button);
		mNoticeboardButton = (Button) v.findViewById(R.id.noticeboard_button);
		return v;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Adding click listener to buttons
		mPortfoliosButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mFragmentLoader.load(PortfoliosListFragment.class, true, null);
			}
		});
		mNoticeboardButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mFragmentLoader.load(NoticeboardListFragment.class, true, null);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.app_name);
		// Showing note layer
		mNoteLayerInteractor.setVisibility(true);
	}
}
