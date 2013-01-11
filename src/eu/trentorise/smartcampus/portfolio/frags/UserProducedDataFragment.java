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
import android.text.TextUtils;
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

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.portfolio.PMHelper;
import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.FragmentLoader;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.OnBackPressedListener;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import eu.trentorise.smartcampus.portfolio.scutils.Constants;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil.CategorizedData;
import eu.trentorise.smartcampus.portfolio.scutils.UserProducedDataRender;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.OptionItem;
import eu.trentorise.smartcampus.portfolio.utils.ToastBuilder;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * Fragment that contains the list of user produced data elements referring to a
 * particular category.
 * 
 * @author Simone Casagranda
 * 
 */
public class UserProducedDataFragment extends SherlockListFragment implements OnBackPressedListener {

	private static final String PORTFOLIO = "PORTFOLIO";
	private static final String CATEGORY = "CATEGORY";

	private static final String FOOTER_VISIBLE = "FOOTER_VISIBLE_UPDATA";
	private static final String OPTION_ITEMS_LIST = "OPTION_ITEMS_LIST";
	private static final String UPDATED_ITEMS = "UPDATED_ITEMS";
	private static final String CHERRY_ITEMS = "CHERRY_ITEMS";

	private static final int EDIT_USER_PRODUCED_DATA = 15;

	// UI References
	private View mFooter;
	private Button mSaveButton, mCancelButton;

	// Interfaces and configurations
	private ArrayList<OptionItem> mOptionItems = new ArrayList<OptionItem>();

	private FragmentLoader mFragmentLoader;
	private NoteLayerInteractor mNoteLayerInteractor;
	private SharedPortfolio mSharedPortfolio;

	private PortfolioUtil mPortfolioUtil;

	// Other variables
	private UserProducedDataAsyncTask mUPDataTask;

	private Portfolio mPortfolio;
	private String mCategory;

	private UPItemArrayAdapter mAdapter;
	private List<UPItem> mElements = new ArrayList<UPItem>();

	private HashSet<String> mUpdatedElements = new HashSet<String>();
	private HashSet<String> mCherryElements = new HashSet<String>();

	private boolean mEditEnabled;
	private boolean mUserConfirm;

	/**
	 * Use this method to prepare arguments that you have to pass to this
	 * Fragment
	 * 
	 * @param portfolio
	 * @return
	 */
	public static Bundle prepareArguments(Portfolio portfolio, String category) {
		// No empty or null list
		assert portfolio != null && !TextUtils.isEmpty(category);
		// Preparing bundle
		Bundle b = new Bundle();
		b.putParcelable(PORTFOLIO, portfolio);
		b.putString(CATEGORY, category);
		return b;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentLoader && activity instanceof NoteLayerInteractor) {
			mFragmentLoader = (FragmentLoader) activity;
			mNoteLayerInteractor = (NoteLayerInteractor) activity;
		} else {
			throw new RuntimeException("The container Activity has to be an instance of FragmentLoader");
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
		outState.putStringArrayList(UPDATED_ITEMS, new ArrayList<String>(mUpdatedElements));
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
		if (args != null && args.containsKey(CATEGORY) && args.containsKey(PORTFOLIO)) {
			mPortfolio = args.getParcelable(PORTFOLIO);
			mCategory = args.getString(CATEGORY);
			mPortfolioUtil = new PortfolioUtil(getActivity(), mPortfolio);
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
		case EDIT_USER_PRODUCED_DATA:
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
			// Getting updated and cherry elements
			mUpdatedElements = new HashSet<String>(savedInstanceState.getStringArrayList(UPDATED_ITEMS));
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
				new SCAsyncTask<Void, Void, Void>(getSherlockActivity(), new SaveTaskProcessor(getSherlockActivity()))
						.execute();
			}
		});
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setEditMode(false);
				// Clearing cache
				mUpdatedElements.clear();
				mCherryElements.clear();
			}
		});
	}

	@Override
	public void onListItemClick(ListView parent, View v, int pos, long id) {
		// Checking that user isn't leaving screen without save
		if (mEditEnabled && (!mUpdatedElements.isEmpty() || !mCherryElements.isEmpty())) {
			ToastBuilder.showShort(getActivity(), R.string.please_commit_your_changes);
			return;
		}
		// Loading user produced data at passed position
		UserProducedData data = mElements.get(pos).userProducedData;
		if (Constants.OVERVIEW.equalsIgnoreCase(data.category) || Constants.EDUCATION.equalsIgnoreCase(data.category)
				|| Constants.PROFESSIONAL.equalsIgnoreCase(data.category)
				|| Constants.ABOUT.equalsIgnoreCase(data.category)) {
			// Checking Unitn state
			boolean isUnitn = (Constants.EDUCATION.equalsIgnoreCase(data.category) && data.type
					.equalsIgnoreCase(Constants.SYS_SIMPLE));
			if (isUnitn) {
				Bundle args = UnitnListFragment.prepareArguments(mPortfolio);
				mFragmentLoader.load(UnitnListFragment.class, true, args);
			} else {
				Bundle args = UPDDetailedFragment.prepareArguments(data);
				mFragmentLoader.load(UPDDetailedFragment.class, true, args);
			}
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
			// Setting visualized ActionBar title
			String title = UserProducedDataRender.renderActionBarTitle(getActivity(), mCategory);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(title);
		}
		// Check if save button has to be visible or not
		hideOrShowSaveButton();
		// Canceling any active task
		cancelAnyActiveTask();
		// Starting new task for user produced data
		mUPDataTask = new UserProducedDataAsyncTask();
		mUPDataTask.execute();
	}

	private void cancelAnyActiveTask() {
		if (mUPDataTask != null && !mUPDataTask.isCancelled()) {
			mUPDataTask.cancel(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelAnyActiveTask();
	}

	private void hideOrShowSaveButton() {
		if (mUpdatedElements.isEmpty() && mCherryElements.isEmpty()) {
			mSaveButton.setVisibility(View.GONE);
		} else {
			mSaveButton.setVisibility(View.VISIBLE);
		}
	}

	private void prepareOptionItem() {
		// Clearing items
		mOptionItems.clear();
		if (!mEditEnabled) {
			mOptionItems.add(new OptionItem(EDIT_USER_PRODUCED_DATA, R.drawable.ic_edit, R.string.edit));
		}
		// Refreshing options menu
		getSherlockActivity().invalidateOptionsMenu();
	}

	private void prepareAdapter() {
		mAdapter = new UPItemArrayAdapter(mElements);
	}

	private class UserProducedDataAsyncTask extends SCAsyncTask<Void, Void, List<UPItem>> {

		public UserProducedDataAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Void, List<UPItem>>(getSherlockActivity()) {
				@Override
				public List<UPItem> performAction(Void... params) throws SecurityException, Exception {
					List<UPItem> items = new ArrayList<UPItem>();
					List<UserProducedData> datas = null;
					// Sets based on category
					HashSet<String> shownIds = null;
					HashSet<String> cherryIds = null;

					if (mSharedPortfolio.isOwned())
						datas = PMHelper.getUserProducedDataList();
					else
						datas = mSharedPortfolio.getContainer().getSharedProducedDatas();
					// Initializing map
					mPortfolioUtil.initializeItems(datas);
					if (Constants.EDUCATION.equalsIgnoreCase(mCategory)) {
						// UPItem item = new UPItem();
						// item.isShown = true;
						// item.isCherry = false;
						// UserProducedData data = new UserProducedData();
						// data.title = getString(R.string.unitn);
						// data.category = mCategory;
						// item.userProducedData = data;
						// // Adding just created item
						// items.add(item);
						new String("").equalsIgnoreCase("");
					}
					// Maps for categories
					CategorizedData categorizedData = mPortfolioUtil.getCategorizedData();
					// Building sets for categories
					categorizedData = mPortfolioUtil.getCategorizedData();
					List<String> list = categorizedData.shownMap.get(mCategory);
					shownIds = new HashSet<String>(list == null ? new ArrayList<String>() : list);
					list = categorizedData.cherriesMap.get(mCategory);
					cherryIds = new HashSet<String>(list == null ? new ArrayList<String>() : list);
					// Iterating over all produced data to produce correct
					// informations
					// for result items
					for (UserProducedData data : datas) {
						// Checking category
						if (mCategory.equals(data.category)) {
							UPItem item = new UPItem();
							item.isShown = shownIds.contains(data.getId());
							item.isCherry = cherryIds.contains(data.getId());
							item.userProducedData = data;
							// Checking edit mode to accept
							if (mEditEnabled ? true : mUpdatedElements.contains(data.getId()) ? !item.isShown
									: item.isShown) {
								items.add(item);
							}
						}
					}
					return items;
				}

				@Override
				public void handleResult(List<UPItem> result) {
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
	 * // Task that retrieves UserProducedData list filtering base private class
	 * UserProducedDataTask extends AsyncTask<Void, Void, List<UPItem>> {
	 * 
	 * private boolean mEditEnabled;
	 * 
	 * public UserProducedDataTask(boolean editEnabled) { mEditEnabled =
	 * editEnabled; }
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * Showing progress bar showProgressBar(true); }
	 * 
	 * @Override protected List<UPItem> doInBackground(Void... params) {
	 * List<UPItem> items = new ArrayList<UPItem>(); List<UserProducedData>
	 * datas = null; // Sets based on category HashSet<String> shownIds = null;
	 * HashSet<String> cherryIds = null;
	 * 
	 * datas = PMHelper.getUserProducedDataList(); // Initializing map
	 * mPortfolioUtil.initializeItems(datas); if
	 * (Constants.EDUCATION.equalsIgnoreCase(mCategory)) { UPItem item = new
	 * UPItem(); item.isShown = true; item.isCherry = false; UserProducedData
	 * data = new UserProducedData(); data.title = getString(R.string.unitn);
	 * data.category = mCategory; item.userProducedData = data; // Adding just
	 * created item items.add(item); } // Maps for categories CategorizedData
	 * categorizedData = mPortfolioUtil.getCategorizedData(); // Building sets
	 * for categories categorizedData = mPortfolioUtil.getCategorizedData();
	 * List<String> list = categorizedData.shownMap.get(mCategory); shownIds =
	 * new HashSet<String>(list == null ? new ArrayList<String>() : list); list
	 * = categorizedData.cherriesMap.get(mCategory); cherryIds = new
	 * HashSet<String>(list == null ? new ArrayList<String>() : list); //
	 * Iterating over all produced data to produce correct informations // for
	 * result items for (UserProducedData data : datas) { // Checking category
	 * if (mCategory.equals(data.category)) { UPItem item = new UPItem();
	 * item.isShown = shownIds.contains(data.getId()); item.isCherry =
	 * cherryIds.contains(data.getId()); item.userProducedData = data; //
	 * Checking edit mode to accept if (mEditEnabled ? true :
	 * mUpdatedElements.contains(data.getId()) ? !item.isShown : item.isShown) {
	 * items.add(item); } } } return items; }
	 * 
	 * @Override protected void onPostExecute(List<UPItem> result) {
	 * super.onPostExecute(result); // Hiding progress bar
	 * showProgressBar(false); // Clearing old list mElements.clear(); // Adding
	 * result if (result != null) { mElements.addAll(result); } // Notifying
	 * adapter mAdapter.notifyDataSetChanged(); }
	 * 
	 * private void showProgressBar(boolean show) {
	 * getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
	 * }
	 * 
	 * }
	 */

	// Adapter used for Category elements
	private class UPItemArrayAdapter extends ArrayAdapter<UPItem> {

		public class Holder {
			TextView titleTextView;
			ImageButton eyeButton, cherryButton;
		}

		public UPItemArrayAdapter(List<UPItem> objects) {
			super(getActivity(), R.layout.item_user_produced_data, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_user_produced_data, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.titleTextView = (TextView) convertView.findViewById(R.id.category_element);
				holder.eyeButton = (ImageButton) convertView.findViewById(R.id.eye_button);
				holder.cherryButton = (ImageButton) convertView.findViewById(R.id.cherry_button);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Getting item
			final UPItem item = getItem(position);
			// Setting label
			UserProducedDataRender.renderTitle(holder.titleTextView, item.userProducedData);
			// Checking if we have to hide or visualize category
			boolean updated = mUpdatedElements.contains(item.userProducedData.getId());
			boolean cherried = mCherryElements.contains(item.userProducedData.getId());
			if (updated ? !item.isShown : item.isShown) {
				// Setting background
				convertView.setBackgroundResource(R.drawable.default_view_background);
				holder.titleTextView.setTextColor(getResources().getColor(R.color.black));
				holder.eyeButton.setImageResource(R.drawable.ic_eye_selected);
				holder.eyeButton.setTag("selected");
				if (cherried ? !item.isCherry : item.isCherry) {
					holder.cherryButton.setImageResource(R.drawable.ic_cherry_selected);
				} else {
					holder.cherryButton.setImageResource(R.drawable.ic_cherry_unselected);
				}
			} else {
				// Setting background
				convertView.setBackgroundResource(R.drawable.gray_view_background);
				holder.titleTextView.setTextColor(getResources().getColor(R.color.gray));
				holder.eyeButton.setImageResource(R.drawable.ic_eye_unselected);
				holder.eyeButton.setTag("unselected");
				holder.cherryButton.setImageResource(R.drawable.ic_cherry_unselected);
			}
			// Checking edit mode
			if (mEditEnabled) {
				holder.eyeButton.setVisibility(View.VISIBLE);
				holder.cherryButton.setVisibility(View.VISIBLE);
				holder.cherryButton.setEnabled(true);
				// Setting listeners
				holder.eyeButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Checking if the item is sys_entry, if true show
						// dialog alert
						if (((String) v.getTag()).equalsIgnoreCase("unselected")
								&& item.userProducedData.type.equalsIgnoreCase(Constants.SYS_SIMPLE)) {
							ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment() {
								@Override
								public void onClick(boolean positive) {
									if (positive) {
										if (mUpdatedElements.contains(item.userProducedData.getId())) {
											mUpdatedElements.remove(item.userProducedData.getId());
										} else {
											mUpdatedElements.add(item.userProducedData.getId());
										}

										// Updating UI
										notifyDataSetChanged();
										// Hiding or show save button
										hideOrShowSaveButton();
									}
									dismiss();
								}
							};
							// Preparing arguments
							Bundle args = ConfirmDialogFragment.prepareArguments(R.string.be_careful,
									R.string.sure_to_show_these_data, R.string.yes_continue, R.string.cancel);
							ConfirmDialogFragment.show(getSherlockActivity(), confirmDialogFragment, args);
						} else {
							// Checking if the user is trying to modify an
							// existent update
							if (mUpdatedElements.contains(item.userProducedData.getId())) {
								mUpdatedElements.remove(item.userProducedData.getId());
							} else {
								mUpdatedElements.add(item.userProducedData.getId());
							}
							// Updating UI
							notifyDataSetChanged();
							// Hiding or show save button
							hideOrShowSaveButton();
						}
					}
				});
				holder.cherryButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean updated = mUpdatedElements.contains(item.userProducedData.getId());
						if (updated ? !item.isShown : item.isShown) {
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
						} else {
							// Showing a toast to user that notifies him that
							// item is not shown
							ToastBuilder.showShort(getActivity(), R.string.item_is_not_shown);
						}
					}
				});
			} else {
				holder.eyeButton.setVisibility(View.GONE);
				holder.cherryButton.setEnabled(false);
				// Checking if it's a cherry item
				if (cherried ? !item.isCherry : item.isCherry) {
					holder.cherryButton.setVisibility(View.VISIBLE);
				} else {
					holder.cherryButton.setVisibility(View.GONE);
				}
			}
			return convertView;
		}

	}

	@Override
	public boolean onBackPressed() {
		if (mFooter.isShown()) {
			if (!mUpdatedElements.isEmpty() || !mCherryElements.isEmpty()) {
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
		if (mUPDataTask != null && !mUPDataTask.isCancelled()) {
			mUPDataTask.cancel(true);
		}
		mUPDataTask = new UserProducedDataAsyncTask();
		mUPDataTask.execute();
	}

	// Wrapper for UserProducedData
	private class UPItem {

		private boolean isShown;
		private boolean isCherry;
		private UserProducedData userProducedData;

	}

	private class SaveTaskProcessor extends AbstractAsyncTaskProcessor<Void, Void> {

		public SaveTaskProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Void performAction(Void... params) throws SecurityException, Exception {
			PMHelper.saveUserProducedData(mPortfolio, mUpdatedElements, mCherryElements);
			return null;
		}

		@Override
		public void handleResult(Void result) {
			mUpdatedElements.clear();
			mCherryElements.clear();
			setEditMode(false);
		}
	}

}
