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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.PMHelper;
import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.OnBackPressedListener;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import eu.trentorise.smartcampus.portfolio.scutils.Constants;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil.CategorizedData;
import eu.trentorise.smartcampus.portfolio.scutils.PresentationRender;
import eu.trentorise.smartcampus.portfolio.scutils.PresentationRender.Holder;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.OptionItem;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * Fragment that visualizes all the info contained in the User presentation
 * category.
 * 
 * @author Simone Casagranda
 * 
 */
public class PresentationFragment extends SherlockListFragment implements OnBackPressedListener {

	private static final String PORTFOLIO = "PORTFOLIO";
	private static final String FOOTER_VISIBLE = "FOOTER_VISIBLE_PORTFOLIO";
	private static final String EDIT_ENABLED = "EDIT_ENABLED";
	private static final String OPTION_ITEMS_LIST = "OPTION_ITEMS_LIST";
	private static final String UPDATED_ELEMS = "UPDATED_ELEMS";

	private static final int EDIT_PRESENTATION = 15;
	private static final int MODIFY_PORTFOLIO = 16;

	// Interfaces and configurations
	private ArrayList<OptionItem> mOptionItems = new ArrayList<OptionItem>();

	private NoteLayerInteractor mNoteLayerInteractor;
	private SharedPortfolio mSharedPortfolio;

	private PortfolioUtil mPortfolioUtil;
	private Portfolio mPortfolio;

	private UPDataArrayAdapter mAdapter;
	private List<UPDataItem> mPresentationsItems = new ArrayList<UPDataItem>();

	private PresentationAsyncTask mPresentationTask;

	// UI References
	private View mFooter;
	private Button mSaveButton, mCancelButton;

	private HashSet<String> mUpdatedElements = new HashSet<String>();

	private boolean mEditEnabled;

	/**
	 * Use this method to prepare arguments that you have to pass to this
	 * Fragment
	 * 
	 * @param portfolio
	 * @return
	 */
	public static Bundle prepareArguments(Portfolio portfolio) {
		// Portfolio cannot be null
		assert portfolio != null;
		// Preparing bundle
		Bundle b = new Bundle();
		b.putParcelable(PORTFOLIO, portfolio);
		return b;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof NoteLayerInteractor) {
			mNoteLayerInteractor = (NoteLayerInteractor) activity;
		} else {
			throw new RuntimeException("The container Activity has to be an instance of NoteLayerInteractor");
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
		outState.putStringArrayList(UPDATED_ELEMS, new ArrayList<String>(mUpdatedElements));
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mSharedPortfolio.isOwned()) {
			// Asking for an option menu
			setHasOptionsMenu(true);
		}
		// Getting app preferences
		// Retrieving title of portfolio
		Bundle args = getArguments();
		if (args != null && args.containsKey(PORTFOLIO)) {
			mPortfolio = args.getParcelable(PORTFOLIO);
			mPortfolioUtil = new PortfolioUtil(getActivity(), mPortfolio);
		} else {
			throw new RuntimeException("You have to pass a portfolio to " + this.getClass().getName());
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
		case EDIT_PRESENTATION:
			setEditMode(true);
			return true;
		case MODIFY_PORTFOLIO:
			PMHelper.openPortfolioInBrowser(getSherlockActivity());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout
		View v = inflater.inflate(R.layout.listfrag_presentation_data, null);
		// Retrieving UI references
		mFooter = (View) v.findViewById(R.id.footer);
		mSaveButton = (Button) mFooter.findViewById(R.id.save);
		mCancelButton = (Button) mFooter.findViewById(R.id.cancel);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		/* Hiding footer first time */
		if (savedInstanceState == null) {
			mEditEnabled = getArguments().getBoolean(EDIT_ENABLED);
			if (mEditEnabled) {
				mFooter.setVisibility(View.VISIBLE);
			} else {
				mFooter.setVisibility(View.GONE);
			}
			// Preparing option menu
			prepareOptionItem();
			// Preparing adapter
			prepareAdapter();
		} else {
			mEditEnabled = savedInstanceState.getBoolean(FOOTER_VISIBLE);
			mFooter.setVisibility(mEditEnabled ? View.VISIBLE : View.GONE);
			// Filling items
			ArrayList<OptionItem> list = savedInstanceState.getParcelableArrayList(OPTION_ITEMS_LIST);
			mOptionItems.addAll(list);
			// Getting updated categories
			mUpdatedElements = new HashSet<String>(savedInstanceState.getStringArrayList(UPDATED_ELEMS));
			// Checking which kind of adapter we have to use
			prepareOptionItem();
			prepareAdapter();
		}
		// Hiding sliding drawer
		mNoteLayerInteractor.setVisibility(!mEditEnabled);
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
				mUpdatedElements.clear();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		if (getActivity() instanceof SherlockFragmentActivity) {
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(R.string.presentation);
		}
		// Check if save button has to be visible or not
		hideOrShowSaveButton();
		// Canceling any active task
		cancelAnyActiveTask();
		// Starting new task for person information
		mPresentationTask = new PresentationAsyncTask();
		mPresentationTask.execute();
	}

	private void cancelAnyActiveTask() {
		if (mPresentationTask != null && !mPresentationTask.isCancelled()) {
			mPresentationTask.cancel(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelAnyActiveTask();
	}

	private void hideOrShowSaveButton() {
		if (mUpdatedElements.isEmpty()) {
			mSaveButton.setVisibility(View.GONE);
		} else {
			mSaveButton.setVisibility(View.VISIBLE);
		}
	}

	private void prepareOptionItem() {
		// Clearing items
		mOptionItems.clear();

		// Preparing option items
		if (!mEditEnabled) {
			mOptionItems.add(new OptionItem(EDIT_PRESENTATION, R.drawable.ic_filter, R.string.edit,OptionItem.VISIBLE));
			mOptionItems.add(new OptionItem(MODIFY_PORTFOLIO, R.drawable.ic_edit, R.string.edit,OptionItem.VISIBLE));

		}

		// Refreshing options menu
		getSherlockActivity().invalidateOptionsMenu();
	}

	private void prepareAdapter() {
		// Matching adapter type
		mAdapter = new UPDataArrayAdapter(mPresentationsItems);
	}

	private class PresentationAsyncTask extends SCAsyncTask<Void, Void, List<UPDataItem>> {

		public PresentationAsyncTask() {
			super(getActivity(), new AbstractAsyncTaskProcessor<Void, List<UPDataItem>>(getActivity()) {
				@Override
				public List<UPDataItem> performAction(Void... params) throws SecurityException, Exception {
					List<UPDataItem> items = new ArrayList<UPDataItem>();
					List<UserProducedData> datas = null;
					// Retrieving UserProducedData
					if (mSharedPortfolio.isOwned())
						datas = PMHelper.getUserProducedDataList();
					else 
						datas = mSharedPortfolio.getContainer().getSharedProducedDatas();
					// Initializing map
					mPortfolioUtil.initializeItems(datas);
					// Maps for categories
					CategorizedData categorizedData = mPortfolioUtil.getCategorizedData();
					// Building list of category
					List<String> list = categorizedData.shownMap.get(Constants.PRESENTATION);
					HashSet<String> shown = list == null ? new HashSet<String>() : new HashSet<String>(list);
					// Building items
					if (datas != null) {
						// Iterating over all UserProducedData
						for (UserProducedData data : datas) {
							// Matching category
							if (Constants.PRESENTATION.equalsIgnoreCase(data.category)) {
								UPDataItem item = new UPDataItem();
								item.isShown = shown.contains(data.getId());
								item.data = data;
								// Checking edit mode to accept
								if (mEditEnabled ? true : mUpdatedElements.contains(data.getId()) ? !item.isShown
										: item.isShown) {
									items.add(item);
								}
							}
						}
					}
					return items;
				}
				@Override
				public void handleResult(List<UPDataItem> result) {
					// Clearing old list
					mPresentationsItems.clear();
					// Adding result
					if (result != null) {
						mPresentationsItems.addAll(result);
					}
					// Notifying adapter
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}
	/*
	// AsyncTask that charges categories
	private class PresentationTask extends AsyncTask<Void, Void, List<UPDataItem>> {

		private boolean mEditEnabled;

		public PresentationTask(boolean editEnabled) {
			mEditEnabled = editEnabled;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress bar
			showProgressBar(true);
		}

		@Override
		protected List<UPDataItem> doInBackground(Void... params) {
			List<UPDataItem> items = new ArrayList<UPDataItem>();
			List<UserProducedData> datas = null;
			// Retrieving UserProducedData
			datas = PMHelper.getUserProducedDataList();
			// Initializing map
			mPortfolioUtil.initializeItems(datas);
			// Maps for categories
			CategorizedData categorizedData = mPortfolioUtil.getCategorizedData();
			// Building list of category
			List<String> list = categorizedData.shownMap.get(Constants.PRESENTATION);
			HashSet<String> shown = list == null ? new HashSet<String>() : new HashSet<String>(list);
			// Building items
			if (datas != null) {
				// Iterating over all UserProducedData
				for (UserProducedData data : datas) {
					// Matching category
					if (Constants.PRESENTATION.equalsIgnoreCase(data.category)) {
						UPDataItem item = new UPDataItem();
						item.isShown = shown.contains(data.getId());
						item.data = data;
						// Checking edit mode to accept
						if (mEditEnabled ? true : mUpdatedElements.contains(data.getId()) ? !item.isShown
								: item.isShown) {
							items.add(item);
						}
					}
				}
			}
			return items;
		}

		@Override
		protected void onPostExecute(List<UPDataItem> result) {
			super.onPostExecute(result);
			// Hiding progress bar
			showProgressBar(false);
			// Clearing old list
			mPresentationsItems.clear();
			// Adding result
			if (result != null) {
				mPresentationsItems.addAll(result);
			}
			// Notifying adapter
			mAdapter.notifyDataSetChanged();
		}

		private void showProgressBar(boolean show) {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
		}
	}
	*/

	// Adapter used for Presentation ListFragment
	private class UPDataArrayAdapter extends ArrayAdapter<UPDataItem> {

		public UPDataArrayAdapter(List<UPDataItem> objects) {
			super(getActivity(), R.layout.item_presentation_list, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_presentation_list, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.video = (VideoView) convertView.findViewById(R.id.video);
				holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
				holder.eyeButton = (ImageButton) convertView.findViewById(R.id.eye_button);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Retrieving item
			final UPDataItem item = getItem(position);
			// Checking result of render
			PresentationRender.render(holder, item.data);
			// Checking if we have to hide or visualize category
			boolean updated = mUpdatedElements.contains(item.data.getId());
			if (updated ? !item.isShown : item.isShown) {
				// Setting backround
				convertView.setBackgroundResource(R.drawable.default_view_background);
				holder.subtitle.setTextColor(getResources().getColor(R.color.black));
				holder.image.setEnabled(true);
				holder.eyeButton.setImageResource(R.drawable.ic_eye_selected);
			} else {
				// Setting backround
				convertView.setBackgroundResource(R.drawable.gray_view_background);
				holder.subtitle.setTextColor(getResources().getColor(R.color.gray));
				holder.image.setEnabled(false);
				holder.eyeButton.setImageResource(R.drawable.ic_eye_unselected);
			}
			// Checking if we are editing categories
			if (mEditEnabled) {
				holder.eyeButton.setVisibility(View.VISIBLE);
			} else {
				holder.eyeButton.setVisibility(View.GONE);
			}
			// Setting listener
			holder.eyeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// update
					if (mUpdatedElements.contains(item.data.getId())) {
						mUpdatedElements.remove(item.data.getId());
					} else {
						mUpdatedElements.add(item.data.getId());
					}
					// Updating UI
					notifyDataSetChanged();
					// Hiding or show save button
					hideOrShowSaveButton();
				}
			});
			return convertView;
		}

	}

	@Override
	public boolean onBackPressed() {
		if (mFooter.isShown()) {
			if (!mUpdatedElements.isEmpty() /* || add other cache controls... */) {
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
			// Check if the user had called it in edit mode or not
			if (getArguments().getBoolean(EDIT_ENABLED)) {
				return false;
			} else {
				setEditMode(false);
				return true;
			}
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
		// Starting new task for presentation
		if (mPresentationTask != null && !mPresentationTask.isCancelled()) {
			mPresentationTask.cancel(true);
		}
		mPresentationTask = new PresentationAsyncTask();
		mPresentationTask.execute();
	}

	// Wrapper for UserProducedData
	private class UPDataItem {

		private boolean isShown;
		private UserProducedData data;

	}

	private class SaveTaskProcessor extends AbstractAsyncTaskProcessor<Void, Void>{

		public SaveTaskProcessor(Activity activity) {
			super(activity);
		}



		@Override
		public Void performAction(Void... params) throws SecurityException, Exception {
			PMHelper.savePresentationData(mPortfolio, mUpdatedElements);
			return null;
		}

		@Override
		public void handleResult(Void result) {
			mUpdatedElements.clear();
			setEditMode(false);
		}
	}	

}
