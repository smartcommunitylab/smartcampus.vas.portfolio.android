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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import eu.trentorise.smartcampus.portfolio.models.StudentInfo;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil.CategorizedData;
import eu.trentorise.smartcampus.portfolio.scutils.StudentDataRender;
import eu.trentorise.smartcampus.portfolio.scutils.StudentDataRender.PersonalInfo;
import eu.trentorise.smartcampus.portfolio.scutils.UserProducedDataRender;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.OptionItem;
import eu.trentorise.smartcampus.portfolio.utils.ToastBuilder;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * ListFragment that shows all user personal info.
 * 
 * @author Simone Casagranda
 * 
 */
public class PersonalInfoFragment extends SherlockListFragment implements OnBackPressedListener {

	private static final String PORTFOLIO = "PORTFOLIO";

	private static final String FOOTER_VISIBLE = "FOOTER_VISIBLE_UPDATA";
	private static final String OPTION_ITEMS_LIST = "OPTION_ITEMS_LIST";
	private static final String UPDATED_UP_DATA_ITEMS = "UPDATED_UP_DATA_ITEMS";
	private static final String UPDATED_INFO_DATA_ITEMS = "UPDATED_INFO_DATA_ITEMS";
	private static final String CHERRY_ITEMS = "CHERRY_INFO_ITEMS";

	private static final int EDIT_USER_INFO_DATA = 15;
	private static final int MODIFY_PORTFOLIO = 16;

	// UI References
	private View mFooter;
	private Button mSaveButton, mCancelButton;

	// Interfaces and configurations
	private ArrayList<OptionItem> mOptionItems = new ArrayList<OptionItem>();

	private NoteLayerInteractor mNoteLayerInteractor;
	private SharedPortfolio mSharedPortfolio;

	private PortfolioUtil mPortfolioUtil;

	// Other variables
	private PersonalInfoAsyncTask mPersonalInfoTask;

	private Portfolio mPortfolio;

	private PersonalInfoItemArrayAdapter mAdapter;
	private List<PersonalInfoItem> mElements = new ArrayList<PersonalInfoItem>();

	// HashSet for all related user info data fields
	private HashSet<String> mUpdatedInfoElems = new HashSet<String>();
	// HashSet for all related user produced data ids
	private HashSet<String> mUpdatedUPDataElements = new HashSet<String>();
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
		outState.putStringArrayList(UPDATED_INFO_DATA_ITEMS, new ArrayList<String>(mUpdatedInfoElems));
		outState.putStringArrayList(UPDATED_UP_DATA_ITEMS, new ArrayList<String>(mUpdatedUPDataElements));
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
		case EDIT_USER_INFO_DATA:
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
		View v = inflater.inflate(R.layout.listfrag_personal_info_data, null);
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
			mUpdatedInfoElems = new HashSet<String>(savedInstanceState.getStringArrayList(UPDATED_INFO_DATA_ITEMS));
			mUpdatedUPDataElements = new HashSet<String>(savedInstanceState.getStringArrayList(UPDATED_UP_DATA_ITEMS));
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
				mUpdatedInfoElems.clear();
				mUpdatedUPDataElements.clear();
				mCherryElements.clear();
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
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(R.string.personal_info);
		}
		// Check if save button has to be visible or not
		hideOrShowSaveButton();
		// Canceling any active task
		cancelAnyActiveTask();
		// Starting new task for user produced data and user info
		mPersonalInfoTask = new PersonalInfoAsyncTask();
		mPersonalInfoTask.execute();
	}

	private void cancelAnyActiveTask() {
		if (mPersonalInfoTask != null && !mPersonalInfoTask.isCancelled()) {
			mPersonalInfoTask.cancel(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelAnyActiveTask();
	}

	private void hideOrShowSaveButton() {
		if (mUpdatedInfoElems.isEmpty() && mUpdatedUPDataElements.isEmpty() && mCherryElements.isEmpty()) {
			mSaveButton.setVisibility(View.GONE);
		} else {
			mSaveButton.setVisibility(View.VISIBLE);
		}
	}

	private void prepareOptionItem() {
		// Clearing items
		mOptionItems.clear();

		if (!mEditEnabled) {
			mOptionItems.add(new OptionItem(MODIFY_PORTFOLIO, R.drawable.ic_edit, R.string.edit,OptionItem.VISIBLE));
			mOptionItems.add(new OptionItem(EDIT_USER_INFO_DATA, R.drawable.ic_filter, R.string.edit,OptionItem.VISIBLE));
		}

		// Refreshing options menu
		getSherlockActivity().invalidateOptionsMenu();
	}

	private void prepareAdapter() {
		mAdapter = new PersonalInfoItemArrayAdapter(mElements);
	}

	private class PersonalInfoAsyncTask extends SCAsyncTask<Void, Void, List<PersonalInfoItem>> {

		public PersonalInfoAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Void, List<PersonalInfoItem>>(
					getSherlockActivity()) {
				@Override
				public List<PersonalInfoItem> performAction(Void... params) throws SecurityException, Exception {
					List<PersonalInfoItem> items = new ArrayList<PersonalInfoItem>();
					StudentInfo studentInfo = null;
					List<UserProducedData> datas = null;
					// Sets based on category
					HashSet<String> shownIds = null;
					HashSet<String> cherryIds = null;
					// Getting user info
					if (mSharedPortfolio.isOwned())
						studentInfo = PMHelper.getStudentInfo();
					else
						studentInfo = mSharedPortfolio.getContainer().getStudentInfo();
					// Getting user produced data list
					datas = PMHelper.getUserProducedDataList();
					// Initializing map
					mPortfolioUtil.initializeItems(datas);

					// Building items those refer to user personal info
					HashSet<String> shownInfoFields = new HashSet<String>(mPortfolio.showStudentInfo);
					String[] allowed = getActivity().getResources().getStringArray(
							R.array.allowed_personal_info_student);
					for (String info : allowed) {
						String[] elems = StudentDataRender.renderizablePersonalInfoElements(info);
						if (elems != null) {
							PersonalInfoItem item = new PersonalInfoItem();
							item.isCherry = false; // Cannot be a cherry element
							item.data = StudentDataRender.renderPersonalInfo(getActivity(), studentInfo.studentData,
									elems);
							item.isShown = item.data != null
									&& (shownInfoFields.contains(info) || info.equals(allowed[0]));
							// Checking edit mode to accept
							if (item.data != null) {
								if (mEditEnabled ? true : mUpdatedInfoElems
										.contains(((PersonalInfo) item.data).interestedFields) ? !item.isShown
										: item.isShown) {
									items.add(item);
								}
							}
						}
					}
					// Maps for categories
					CategorizedData categorizedData = mPortfolioUtil.getCategorizedData();
					// Building sets for categories
					categorizedData = mPortfolioUtil.getCategorizedData();
					// We clear label map
					mAdapter.mFirstCategoryElement.clear();
					for (String category : getMatchedCategories()) {
						List<String> list = categorizedData.shownMap.get(category);
						shownIds = new HashSet<String>(list == null ? new ArrayList<String>() : list);
						list = categorizedData.cherriesMap.get(category);
						cherryIds = new HashSet<String>(list == null ? new ArrayList<String>() : list);
						// Iterating over all produced data to produce correct
						// informations for result items
						for (UserProducedData data : datas) {
							// Checking category
							if (category.equals(data.category)) {
								PersonalInfoItem item = new PersonalInfoItem();
								item.isShown = shownIds.contains(data.getId());
								item.isCherry = cherryIds.contains(data.getId());
								item.data = data;
								// Checking edit mode to accept
								if (mEditEnabled ? true : mUpdatedUPDataElements.contains(data.getId()) ? !item.isShown
										: item.isShown) {
									if (!mAdapter.mFirstCategoryElement.containsKey(data.category)) {
										mAdapter.mFirstCategoryElement.put(data.category, data.getId());
									}
									items.add(item);
								}
							}
						}
					}
					return items;
				}

				@Override
				public void handleResult(List<PersonalInfoItem> result) {
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

	// Retrieves all the categories those can be encapsulated into
	// PersonalInfoFragment
	private String[] getMatchedCategories() {
		String[] rawCategories = getResources().getStringArray(R.array.portfolio_categories_ordered);
		String raw = rawCategories == null ? null : rawCategories[0];
		return raw == null ? new String[] {} : raw.replaceAll("\\s+", "").split(",");
	}

	/*
	 * // Task that retrieves UserProducedData list filtering base private class
	 * PersonalInfoTask extends AsyncTask<Void, Void, List<PersonalInfoItem>> {
	 * 
	 * private boolean mEditEnabled;
	 * 
	 * public PersonalInfoTask(boolean editEnabled) { mEditEnabled =
	 * editEnabled; }
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * Showing progress bar showProgressBar(true); }
	 * 
	 * @Override protected List<PersonalInfoItem> doInBackground(Void... params)
	 * { List<PersonalInfoItem> items = new ArrayList<PersonalInfoItem>();
	 * StudentInfo studentInfo = null; List<UserProducedData> datas = null; //
	 * Sets based on category HashSet<String> shownIds = null; HashSet<String>
	 * cherryIds = null; // Getting user info studentInfo =
	 * PMHelper.getStudentInfo(); // Getting user produced data list datas =
	 * PMHelper.getUserProducedDataList(); // Initializing map
	 * mPortfolioUtil.initializeItems(datas);
	 * 
	 * // Building items those refer to user personal info HashSet<String>
	 * shownInfoFields = new HashSet<String>(mPortfolio.showStudentInfo);
	 * String[] allowed = getActivity().getResources().getStringArray(R.array.
	 * allowed_personal_info_student); for (String info : allowed) { String[]
	 * elems = StudentDataRender.renderizablePersonalInfoElements(info); if
	 * (elems != null) { PersonalInfoItem item = new PersonalInfoItem();
	 * item.isCherry = false; // Cannot be a cherry element item.data =
	 * StudentDataRender.renderPersonalInfo(getActivity(),
	 * studentInfo.studentData, elems); item.isShown = item.data != null &&
	 * (shownInfoFields.contains(info) || info.equals(allowed[0])); // Checking
	 * edit mode to accept if (mEditEnabled ? true :
	 * mUpdatedInfoElems.contains(((PersonalInfo) item.data).interestedFields) ?
	 * !item.isShown : item.isShown) { items.add(item); } } } // Maps for
	 * categories CategorizedData categorizedData =
	 * mPortfolioUtil.getCategorizedData(); // Building sets for categories
	 * categorizedData = mPortfolioUtil.getCategorizedData(); // We clear label
	 * map mAdapter.mFirstCategoryElement.clear(); for (String category :
	 * getMatchedCategories()) { List<String> list =
	 * categorizedData.shownMap.get(category); shownIds = new
	 * HashSet<String>(list == null ? new ArrayList<String>() : list); list =
	 * categorizedData.cherriesMap.get(category); cherryIds = new
	 * HashSet<String>(list == null ? new ArrayList<String>() : list); //
	 * Iterating over all produced data to produce correct // informations for
	 * result items for (UserProducedData data : datas) { // Checking category
	 * if (category.equals(data.category)) { PersonalInfoItem item = new
	 * PersonalInfoItem(); item.isShown = shownIds.contains(data.getId());
	 * item.isCherry = cherryIds.contains(data.getId()); item.data = data; //
	 * Checking edit mode to accept if (mEditEnabled ? true :
	 * mUpdatedUPDataElements.contains(data.getId()) ? !item.isShown :
	 * item.isShown) { if
	 * (!mAdapter.mFirstCategoryElement.containsKey(data.category)) {
	 * mAdapter.mFirstCategoryElement.put(data.category, data.getId()); }
	 * items.add(item); } } } } return items; }
	 * 
	 * @Override protected void onPostExecute(List<PersonalInfoItem> result) {
	 * super.onPostExecute(result); // Hiding progress bar
	 * showProgressBar(false); // Clearing old list mElements.clear(); // Adding
	 * result if (result != null) { mElements.addAll(result); } // Notifying
	 * adapter mAdapter.notifyDataSetChanged(); }
	 * 
	 * // Retrieves all the categories those can be encapsulated into //
	 * PersonalInfoFragment private String[] getMatchedCategories() { String[]
	 * rawCategories =
	 * getResources().getStringArray(R.array.portfolio_categories_ordered);
	 * String raw = rawCategories == null ? null : rawCategories[0]; return raw
	 * == null ? new String[] {} : raw.replaceAll("\\s+", "").split(","); }
	 * 
	 * private void showProgressBar(boolean show) {
	 * getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
	 * }
	 * 
	 * }
	 */

	// Adapter used for Category elements
	private class PersonalInfoItemArrayAdapter extends ArrayAdapter<PersonalInfoItem> {

		private Map<String, String> mFirstCategoryElement = Collections.synchronizedMap(new HashMap<String, String>());

		public class Holder {
			TextView separator, title, subtitle;
			ImageButton eye, cherry;
		}

		public PersonalInfoItemArrayAdapter(List<PersonalInfoItem> objects) {
			super(getActivity(), R.layout.item_user_personal_info, objects); // We
																				// can
																				// use
																				// same
																				// layout
																				// for
																				// UserProducedData
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_user_personal_info, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.separator = (TextView) convertView.findViewById(R.id.separator);
				holder.title = (TextView) convertView.findViewById(R.id.title_element);
				holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle_element);
				holder.eye = (ImageButton) convertView.findViewById(R.id.eye_button);
				holder.cherry = (ImageButton) convertView.findViewById(R.id.cherry_button);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Getting item
			final PersonalInfoItem item = getItem(position);
			// Matching type of data
			if (item.data instanceof UserProducedData) {
				final UserProducedData data = (UserProducedData) item.data;
				// Checking if we have to hide or visualize category
				boolean updated = mUpdatedUPDataElements.contains(data.getId());
				boolean cherried = mCherryElements.contains(data.getId());
				if (updated ? !item.isShown : item.isShown) {
					// Setting backround
					convertView.setBackgroundResource(R.drawable.default_view_background);
					holder.subtitle.setTextColor(getResources().getColor(R.color.black));
					holder.eye.setImageResource(R.drawable.ic_eye_selected);
					if (cherried ? !item.isCherry : item.isCherry) {
						holder.cherry.setImageResource(R.drawable.ic_cherry_selected);
					} else {
						holder.cherry.setImageResource(R.drawable.ic_cherry_unselected);
					}
				} else {
					// Setting backround
					convertView.setBackgroundResource(R.drawable.gray_view_background);
					holder.subtitle.setTextColor(getResources().getColor(R.color.gray));
					holder.eye.setImageResource(R.drawable.ic_eye_unselected);
					holder.cherry.setImageResource(R.drawable.ic_cherry_unselected);
				}
				// Checking edit mode
				if (mEditEnabled) {
					holder.eye.setVisibility(View.VISIBLE);
					holder.cherry.setVisibility(View.VISIBLE);
					holder.cherry.setEnabled(true);
					// Setting listeners
					holder.eye.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// Checking if the user is trying to modify an
							// existent update
							if (mUpdatedUPDataElements.contains(data.getId())) {
								mUpdatedUPDataElements.remove(data.getId());
							} else {
								mUpdatedUPDataElements.add(data.getId());
							}
							// Updating UI
							notifyDataSetChanged();
							// Hiding or show save button
							hideOrShowSaveButton();

						}
					});
					holder.cherry.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							boolean updated = mUpdatedUPDataElements.contains(data.getId());
							if (updated ? !item.isShown : item.isShown) {
								// Checking if the user is trying to modify
								// an existent cherry element
								if (mCherryElements.contains(data.getId())) {
									mCherryElements.remove(data.getId());
								} else {
									mCherryElements.add(data.getId());
								}
								// Updating UI
								notifyDataSetChanged();
								// Hiding or show save button
								hideOrShowSaveButton();
							} else {
								// Showing a toast to user that notifies him
								// that item is not shown
								ToastBuilder.showShort(getActivity(), R.string.item_is_not_shown);
							}
						}
					});
				} else {
					holder.eye.setVisibility(View.GONE);
					holder.cherry.setEnabled(false);
					// Checking if it's a cherry item
					if (cherried ? !item.isCherry : item.isCherry) {
						holder.cherry.setVisibility(View.VISIBLE);
					} else {
						holder.cherry.setVisibility(View.GONE);
					}
				}
				// Setting title for separator
				if (data.getId().equals(mFirstCategoryElement.get(data.category))) {
					holder.separator.setVisibility(View.VISIBLE);
				} else {
					holder.separator.setVisibility(View.GONE);
				}
				// Rendering view
				UserProducedDataRender.renderViewPersonalInfo(holder.separator, holder.title, holder.subtitle,
						holder.cherry, data);
			} else if (item.data instanceof PersonalInfo) {
				final PersonalInfo info = (PersonalInfo) item.data;

				// Checking if we have to hide or visualize category
				boolean updated = mUpdatedInfoElems.contains(info.interestedFields);
				if (updated ? !item.isShown : item.isShown) {
					// Setting backround
					convertView.setBackgroundResource(R.drawable.default_view_background);
					holder.subtitle.setTextColor(getResources().getColor(R.color.black));
					holder.eye.setImageResource(R.drawable.ic_eye_selected);
				} else {
					// Setting backround
					convertView.setBackgroundResource(R.drawable.gray_view_background);
					holder.subtitle.setTextColor(getResources().getColor(R.color.gray));
					holder.eye.setImageResource(R.drawable.ic_eye_unselected);
				}
				// Checking edit mode
				if (mEditEnabled) {
					holder.eye.setVisibility(View.VISIBLE);
					// Setting listeners
					holder.eye.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// Checking if the user is trying to modify an
							// existent update
							if (mUpdatedInfoElems.contains(info.interestedFields)) {
								mUpdatedInfoElems.remove(info.interestedFields);
							} else {
								mUpdatedInfoElems.add(info.interestedFields);
							}
							// Updating UI
							notifyDataSetChanged();
							// Hiding or show save button
							hideOrShowSaveButton();
						}
					});
				} else {
					// Hiding eye button
					holder.eye.setVisibility(View.GONE);
				}
				// Checking if contains name field
				if (info.interestedFields.contains("name")) {
					holder.eye.setVisibility(View.GONE);
				}
				// Setting title for separator
				holder.separator.setText(R.string.identity_info);
				if (position == 0) {
					holder.separator.setVisibility(View.VISIBLE);
				} else {
					holder.separator.setVisibility(View.GONE);
				}
				// Rendering view
				StudentDataRender.renderViewPersonalInfo(holder.title, holder.subtitle, holder.cherry, info);
			}
			return convertView;
		}

	}

	@Override
	public boolean onBackPressed() {
		if (mFooter.isShown()) {
			if (!mUpdatedInfoElems.isEmpty() || !mUpdatedUPDataElements.isEmpty() || !mCherryElements.isEmpty()) {
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
		if (mPersonalInfoTask != null && !mPersonalInfoTask.isCancelled()) {
			mPersonalInfoTask.cancel(true);
		}
		mPersonalInfoTask = new PersonalInfoAsyncTask();
		mPersonalInfoTask.execute();
	}

	// Wrapper for Personal Info
	private class PersonalInfoItem {

		private boolean isShown;
		private boolean isCherry;
		private Object data;

	}

	private class SaveTaskProcessor  extends AbstractAsyncTaskProcessor<Void, Void>{

		public SaveTaskProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Void performAction(Void... params) throws SecurityException, Exception {
			PMHelper.savePersonalInfo(mPortfolio, mUpdatedInfoElems, mUpdatedUPDataElements, mCherryElements);
			return null;
		}

		@Override
		public void handleResult(Void result) {
			mUpdatedInfoElems.clear();
			mUpdatedUPDataElements.clear();
			mCherryElements.clear();
			setEditMode(false);
		}
	}

}
