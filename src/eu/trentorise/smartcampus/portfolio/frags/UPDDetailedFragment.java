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

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import eu.trentorise.smartcampus.portfolio.scutils.DetailedUPDataRender;
import eu.trentorise.smartcampus.portfolio.scutils.DetailedUPDataRender.Holder;
import eu.trentorise.smartcampus.portfolio.scutils.UserProducedDataRender;
import eu.trentorise.smartcampus.portfolio.utils.OptionItem;

/**
 * Fragment that show in detail a user produced data.
 * 
 * @author Simone Casagranda
 *
 */
public class UPDDetailedFragment extends SherlockFragment {


	private static final String USER_PRODUCED_DATA = "USER_PRODUCED_DATA";
	private static final String OPTION_ITEMS_LIST = "OPTION_ITEMS_LIST";

	// Interfaces and configurations
	private ArrayList<OptionItem> mOptionItems = new ArrayList<OptionItem>();
	
	private SharedPortfolio mSharedPortfolio;

	private UserProducedData mUserProducedData;
	
	private Holder mHolder;
	
	/**
	 * Use this method to prepare arguments that you have to pass to this Fragment
	 * @param portfolio
	 * @return
	 */
	public static Bundle prepareArguments(UserProducedData userProducedData){
		// UserProducedData cannot be null
		assert userProducedData != null;
		// Preparing bundle
		Bundle b = new Bundle();
		b.putParcelable(USER_PRODUCED_DATA, userProducedData);
		return b;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof SharedPortfolio){
			mSharedPortfolio = (SharedPortfolio) activity;
		}else{
			throw new RuntimeException("The container Activity has to be an instance of SharedPortfolio");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(OPTION_ITEMS_LIST, mOptionItems);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(mSharedPortfolio.isOwned()){
			// Asking for an option menu
	        setHasOptionsMenu(true);
		}
		// Asking for an option menu
        setHasOptionsMenu(true);
		// Retrieving title of portfolio
		Bundle args = getArguments();
		if(args!=null && args.containsKey(USER_PRODUCED_DATA)){
			mUserProducedData = args.getParcelable(USER_PRODUCED_DATA);
		}else{
			throw new RuntimeException("You have to pass an UserProducedData to " + this.getClass().getName());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Adding all items
		for(OptionItem item : mOptionItems){
	        menu.add(Menu.NONE, item.id, Menu.NONE, item.res)
	        .setIcon(item.icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);			
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Manage by id action items
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout
		View v = inflater.inflate(R.layout.frag_detailed_user_produced_data, null);
		// Retrieving UI references
		mHolder = new Holder();
		mHolder.title = (TextView) v.findViewById(R.id.title);
		mHolder.subtitle = (TextView) v.findViewById(R.id.subtitle);
		mHolder.description = (TextView) v.findViewById(R.id.description);
		mHolder.image = (ImageView) v.findViewById(R.id.image);
		mHolder.video = (VideoView) v.findViewById(R.id.video);
		mHolder.progressBar = (ProgressBar) v.findViewById(R.id.progress);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Checking which kind of adapter we have to use
		prepareOptionItem();
	}	
	
	@Override
	public void onStart() {
		super.onStart();
		// Rendering user produced data
		DetailedUPDataRender.render(mHolder, mUserProducedData);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// Canceling task
		if(mHolder.imageLoader != null && !mHolder.imageLoader.isCancelled()){
			mHolder.imageLoader.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		if(getActivity() instanceof SherlockFragmentActivity){
			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			// Setting visualized ActionBar title
			String title = UserProducedDataRender.renderActionBarTitle(getActivity(), mUserProducedData.category);
			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setTitle(title);
		}
	}
	
	private void prepareOptionItem(){
		// Clearing items
		mOptionItems.clear();
		// Refreshing options menu
		getSherlockActivity().invalidateOptionsMenu();
	}
	
}
