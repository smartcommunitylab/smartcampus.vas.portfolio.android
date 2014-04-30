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
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.PMHelper;
import it.smartcampuslab.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.FragmentLoader;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.OnBackPressedListener;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import eu.trentorise.smartcampus.portfolio.scutils.Constants;
import eu.trentorise.smartcampus.portfolio.scutils.UserProducedDataRender;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.OptionItem;
import eu.trentorise.smartcampus.portfolio.utils.ToastBuilder;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * Fragment that contains the list of cherry data.
 * 
 * @author Simone Casagranda
 * 
 */
public class CherryFragment extends SherlockListFragment implements OnBackPressedListener {

	private static final String PORTFOLIO = "PORTFOLIO";

	private static final String FOOTER_VISIBLE = "FOOTER_VISIBLE_CHERRYDATA";
	private static final String OPTION_ITEMS_LIST = "OPTION_ITEMS_LIST";
	private static final String CHERRY_ITEMS = "CHERRIES";

	private static final int EDIT_CHERRIES_DATA = 15;

	// UI References
	private View mFooter;
	private Button mSaveButton, mCancelButton;

	// Interfaces and configurations
	private ArrayList<OptionItem> mOptionItems = new ArrayList<OptionItem>();

	private FragmentLoader mFragmentLoader;
	private NoteLayerInteractor mNoteLayerInteractor;
	private SharedPortfolio mSharedPortfolio;

	// Other variables
	private CherryDataAsyncTask mCherryDataTask;

	private Portfolio mPortfolio;

	private CherryItemArrayAdapter mAdapter;
	private List<CherryItem> mElements = new ArrayList<CherryItem>();

	private HashSet<String> mCherryElements = new HashSet<String>();

	private boolean mEditEnabled;

	/**
	 * Use this method to prepare arguments that you have to pass to this
	 * Fragment
	 * 
	 * @param portfolio
	 * @return
	 */
	public static Bundle prepareArguments(Portfolio portfolio) {
		// Not null check
		assert portfolio != null;
		// Preparing bundle
		Bundle b = new Bundle();
		b.putParcelable(PORTFOLIO, portfolio);
		return b;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentLoader && activity instanceof NoteLayerInteractor) {
			mFragmentLoader = (FragmentLoader) activity;
			mNoteLayerInteractor = (NoteLayerInteractor) activity;
		} else {
			throw new RuntimeException(
					"The container Activity has to be an instance of FragmentLoader and NoteLayerInteractor");
		}
		if (activity instanceof SharedPortfolio) {
			mSharedPortfolio = (SharedPortfolio) activity;
		} else {
			throw new RuntimeException("The container Activity has to be an instance of SharedPortfolio");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(FOOTER_VISIBLE, mFooter != null ? mFooter.isShown() : false);
		outState.putParcelableArrayList(OPTION_ITEMS_LIST, mOptionItems);
		outState.putStringArrayList(CHERRY_ITEMS, new ArrayList<String>(mCherryElements));
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mSharedPortfolio.isOwned()) {
			// Asking for an option menu
			setHasOptionsMenu(true);
		}
		// Retrieving title of portfolio
		Bundle args = getArguments();
		if (args != null && args.containsKey(PORTFOLIO)) {
			mPortfolio = args.getParcelable(PORTFOLIO);
		} else {
			throw new RuntimeException("You have to use prepareArguments method!");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Adding all items
		for (OptionItem item : mOptionItems) {
			menu.add(Menu.NONE, item.id, Menu.NONE, item.res).setIcon(item.icon)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_CHERRIES_DATA:
			setEditMode(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout
		View v = inflater.inflate(R.layout.listfrag_user_produced_data, null);
		// Retrieving UI references
		mFooter = (View) v.findViewById(R.id.footer);
		mSaveButton = (Button) mFooter.findViewById(R.id.save);
		mCancelButton = (Button) mFooter.findViewById(R.id.cancel);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null) {
			// Checking if we are in edit mode or not
			mEditEnabled = savedInstanceState.getBoolean(FOOTER_VISIBLE, false);
			// Filling items
			ArrayList<OptionItem> list = savedInstanceState.getParcelableArrayList(OPTION_ITEMS_LIST);
			mOptionItems.addAll(list);
			// Getting cherry elements
			mCherryElements = new HashSet<String>(savedInstanceState.getStringArrayList(CHERRY_ITEMS));
		}
		mFooter.setVisibility(mEditEnabled ? View.VISIBLE : View.GONE);
		// Hiding sliding drawer
		mNoteLayerInteractor.setVisibility(!mEditEnabled);
		// Preparing option item
		prepareOptionItem();
		// Preparing adapter
		prepareAdapter();
		// Setting adapter to list
		getListView().setAdapter(mAdapter);
		// Setting listeners to edit buttons
		mSaveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SCAsyncTask<Void, Void, Void>(getSherlockActivity(), new SaveTaskProcessor(getSherlockActivity())).execute();
			}
		});
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setEditMode(false);
				// Clearing cache
				mCherryElements.clear();
			}
		});
	}

	@Override
	public void onListItemClick(ListView parent, View v, int pos, long id) {
		// Checking that user isn't leaving screen without save
		if (mEditEnabled && !mCherryElements.isEmpty()) {
			ToastBuilder.showShort(getActivity(), R.string.please_commit_your_changes);
			return;
		}
		// Loading user produced data at passed position
		UserProducedData data = mElements.get(pos).userProducedData;
		// Loading only if matches correct category
		if (Constants.OVERVIEW.equalsIgnoreCase(data.category) || Constants.EDUCATION.equalsIgnoreCase(data.category)
				|| Constants.PROFESSIONAL.equalsIgnoreCase(data.category)
				|| Constants.ABOUT.equalsIgnoreCase(data.category)) {
			Bundle args = UPDDetailedFragment.prepareArguments(data);
			mFragmentLoader.load(UPDDetailedFragment.class, true, args);
		} else {
			ToastBuilder.showShort(getActivity(), R.string.no_other_info_contained);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		if (getActivity() instanceof SherlockFragmentActivity) {
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(R.string.cherry_on_the_cake);
		}
		// Check if save button has to be visible or not
		hideOrShowSaveButton();
		// Canceling any active task
		cancelAnyActiveTask();
		// Starting new task for user produced data
		mCherryDataTask = new CherryDataAsyncTask();
		mCherryDataTask.execute();
	}

	private void cancelAnyActiveTask() {
		if (mCherryDataTask != null && !mCherryDataTask.isCancelled()) {
			mCherryDataTask.cancel(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelAnyActiveTask();
	}

	private void hideOrShowSaveButton() {
		if (mCherryElements.isEmpty()) {
			mSaveButton.setVisibility(View.GONE);
		} else {
			mSaveButton.setVisibility(View.VISIBLE);
		}
	}

	private void prepareOptionItem() {
		// Clearing items
		mOptionItems.clear();
		if (!mEditEnabled) {
			mOptionItems.add(new OptionItem(EDIT_CHERRIES_DATA, R.drawable.ic_filter, R.string.edit,OptionItem.VISIBLE));
		}
		// Refreshing options menu
		getSherlockActivity().invalidateOptionsMenu();
	}

	private void prepareAdapter() {
		mAdapter = new CherryItemArrayAdapter(mElements);
	}

	private class CherryDataAsyncTask extends SCAsyncTask<Void, Void, List<CherryItem>> {

		public CherryDataAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Void, List<CherryItem>>(getSherlockActivity()) {
				@Override
				public List<CherryItem> performAction(Void... params) throws SecurityException, Exception {
					List<CherryItem> items = new ArrayList<CherryItem>();
					List<UserProducedData> datas = null;
					// Sets based on category
					List<String> list = mPortfolio.highlightUserGeneratedData;
					HashSet<String> cherryIds = new HashSet<String>(list == null ? new ArrayList<String>() : list);
					if (mSharedPortfolio.isOwned())
						datas = PMHelper.getUserProducedDataList();
					else 
						datas = mSharedPortfolio.getContainer().getSharedProducedDatas();
					// Iterating over all produced data to produce correct informations
					// for result items
					for (UserProducedData data : datas) {
						// Checking category
						if (cherryIds.contains(data.getId())) {
							CherryItem item = new CherryItem();
							item.isCherry = true;
							item.userProducedData = data;
							// Checking edit mode to accept
							if (mEditEnabled ? true : mCherryElements.contains(data.getId()) ? false : true) {
								items.add(item);
							}
						}
					}
					return items;
				}
				@Override
				public void handleResult(List<CherryItem> result) {
					// Clearing old list
					mElements.clear();
					// Adding result
					if (result != null) {
						mElements.addAll(result);
					}
					// Notifying adapter
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	/*
	// Task that retrieves UserProducedData list filtering base
	private class CherryDataTask extends AsyncTask<Void, Void, List<CherryItem>> {

		private boolean mEditEnabled;

		public CherryDataTask(boolean editEnabled) {
			mEditEnabled = editEnabled;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress bar
			showProgressBar(true);
		}

		@Override
		protected List<CherryItem> doInBackground(Void... params) {
			List<CherryItem> items = new ArrayList<CherryItem>();
			List<UserProducedData> datas = null;
			// Sets based on category
			List<String> list = mPortfolio.highlightUserGeneratedData;
			HashSet<String> cherryIds = new HashSet<String>(list == null ? new ArrayList<String>() : list);
			datas = PMHelper.getUserProducedDataList();
			// Iterating over all produced data to produce correct informations
			// for result items
			for (UserProducedData data : datas) {
				// Checking category
				if (cherryIds.contains(data.getId())) {
					CherryItem item = new CherryItem();
					item.isCherry = true;
					item.userProducedData = data;
					// Checking edit mode to accept
					if (mEditEnabled ? true : mCherryElements.contains(data.getId()) ? false : true) {
						items.add(item);
					}
				}
			}
			return items;
		}

		@Override
		protected void onPostExecute(List<CherryItem> result) {
			super.onPostExecute(result);
			// Hiding progress bar
			showProgressBar(false);
			// Clearing old list
			mElements.clear();
			// Adding result
			if (result != null) {
				mElements.addAll(result);
			}
			// Notifying adapter
			mAdapter.notifyDataSetChanged();
		}

		private void showProgressBar(boolean show) {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
		}

	}
	*/

	// Adapter used for Category elements
	private class CherryItemArrayAdapter extends ArrayAdapter<CherryItem> {

		public class Holder {
			TextView titleTextView;
			ImageButton cherryButton;
		}

		public CherryItemArrayAdapter(List<CherryItem> objects) {
			super(getActivity(), R.layout.item_cherry_user_produced_data, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_cherry_user_produced_data, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.titleTextView = (TextView) convertView.findViewById(R.id.category_element);
				holder.cherryButton = (ImageButton) convertView.findViewById(R.id.cherry_button);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Getting item
			final CherryItem item = getItem(position);
			// Setting label
			UserProducedDataRender.renderTitle(holder.titleTextView, item.userProducedData);
			// Checking edit mode
			if (mEditEnabled) {
				holder.cherryButton.setEnabled(true);
				// Setting graphics
				boolean cherried = mCherryElements.contains(item.userProducedData.getId());
				if (cherried ? !item.isCherry : item.isCherry) {
					// Setting backround
					convertView.setBackgroundResource(R.drawable.default_view_background);
					holder.titleTextView.setTextColor(getResources().getColor(R.color.black));
					holder.cherryButton.setImageResource(R.drawable.ic_cherry_selected);
				} else {
					// Setting backround
					convertView.setBackgroundResource(R.drawable.gray_view_background);
					holder.titleTextView.setTextColor(getResources().getColor(R.color.gray));
					holder.cherryButton.setImageResource(R.drawable.ic_cherry_unselected);
				}
				// Setting listeners
				holder.cherryButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Checking if the user is trying to modify an
						// existent cherry element
						if (mCherryElements.contains(item.userProducedData.getId())) {
							mCherryElements.remove(item.userProducedData.getId());
						} else {
							mCherryElements.add(item.userProducedData.getId());
						}
						// Updating UI
						notifyDataSetChanged();
						// Hiding or show save button
						hideOrShowSaveButton();
					}
				});
			} else {
				holder.cherryButton.setEnabled(false);
				// Setting graphics
				convertView.setBackgroundResource(R.drawable.default_view_background);
				holder.titleTextView.setTextColor(getResources().getColor(R.color.black));
				holder.cherryButton.setImageResource(R.drawable.ic_cherry_selected);
			}
			return convertView;
		}

	}

	@Override
	public boolean onBackPressed() {
		if (mFooter.isShown()) {
			if (!mCherryElements.isEmpty()) {
				ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment() {
					@Override
					public void onClick(boolean positive) {
						if (positive) {
							dismiss();
							getSherlockActivity().getSupportFragmentManager().popBackStack();
						} else {
							dismiss();
						}
					}
				};
				// Preparing arguments
				Bundle args = ConfirmDialogFragment.prepareArguments(R.string.pay_attention,
						R.string.sure_to_continue_without_save, R.string.yes_continue, R.string.cancel);
				ConfirmDialogFragment.show(getSherlockActivity(), confirmDialogFragment, args);
				return true;
			}
			setEditMode(false);
			return true;
		} else {
			return false;
		}
	}

	// Allows to prepare e initialize gui for edit and non edit mode.
	private void setEditMode(boolean enabled) {
		mEditEnabled = enabled;
		// Hiding footer
		mFooter.setVisibility(mEditEnabled ? View.VISIBLE : View.GONE);
		// Hiding sliding drawer
		mNoteLayerInteractor.setVisibility(!mEditEnabled);
		// Exit from edit mode
		prepareOptionItem();
		// Launching task for user produced data
		if (mCherryDataTask != null && !mCherryDataTask.isCancelled()) {
			mCherryDataTask.cancel(true);
		}
		mCherryDataTask = new CherryDataAsyncTask();
		mCherryDataTask.execute();
	}

	// Wrapper for Cherry on the cake user produced data
	private class CherryItem {

		private boolean isCherry;
		private UserProducedData userProducedData;

	}
	
	private class SaveTaskProcessor extends AbstractAsyncTaskProcessor<Void, Void>{

		public SaveTaskProcessor(Activity activity) {
			super(activity);
		}


		@Override
		public Void performAction(Void... params) throws SecurityException, Exception {
			PMHelper.savePortfolioCherryData(mPortfolio, mCherryElements);
			return null;
		}
		
		

		@Override
		public void handleResult(Void result) {
			mCherryElements.clear();
			setEditMode(false);
		}
	}	
}
