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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.common.Preferences;
import eu.trentorise.smartcampus.portfolio.PMHelper;
import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.FragmentLoader;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.OnBackPressedListener;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.Concept;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
import eu.trentorise.smartcampus.portfolio.scutils.Constants;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil;
import eu.trentorise.smartcampus.portfolio.scutils.PortfolioUtil.CategorizedData;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.OptionItem;
import eu.trentorise.smartcampus.portfolio.utils.ToastBuilder;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;

/**
 * Fragment that contains all data related to a Portfolio.
 * 
 * @author Simone Casagranda
 * 
 */
public class PortfolioFragment extends SherlockListFragment implements OnBackPressedListener,
		TaggingDialog.OnTagsSelectedListener, TaggingDialog.TagProvider {

	private static final String PORTFOLIO = "PORTFOLIO";
	private static final String FOOTER_VISIBLE = "FOOTER_VISIBLE_PORTFOLIO";
	private static final String EDIT_ENABLED = "EDIT_ENABLED";
	private static final String OPTION_ITEMS_LIST = "OPTION_ITEMS_LIST";
	private static final String UPDATED_CATEGORIES = "UPDATED_CATEGORIES";

	// UI References
	private TextView mTitleTextView, mTagsTextView;
	private View mFooter;
	private Button mSaveButton, mCancelButton;

	// Interfaces and configurations
	private ArrayList<OptionItem> mOptionItems = new ArrayList<OptionItem>();

	private FragmentLoader mFragmentLoader;
	private NoteLayerInteractor mNoteLayerInteractor;
	private SharedPortfolio mSharedPortfolio;

	private PortfolioUtil mPortfolioUtil;
	private Portfolio mPortfolio;

	// Other variables
	// private TagTask mTagTask;
	// private CategoryAsyncTask mCategoryTask;
	// private PortfolioAsyncTask mPortfolioTask;
	private LoadAsyncTask mLoadTask;

	private CategoryArrayAdapter mAdapter;
	private List<CategoryItem> mCategories = new ArrayList<CategoryItem>();

	private String[] mCategoriesOrdered;
	private String[] mForceVisualization;
	private String[] mCategoriesLabels;

	private HashSet<String> mUpdatedElements = new HashSet<String>();

	private boolean mEditEnabled;

	/**
	 * Use this method to prepare arguments that you have to pass to this
	 * Fragment
	 * 
	 * @param portfolio
	 * @return
	 */
	public static Bundle prepareArguments(Portfolio portfolio, boolean editEnabled) {
		// Portfolio cannot be null
		assert portfolio != null;
		// Preparing bundle
		Bundle b = new Bundle();
		b.putParcelable(PORTFOLIO, portfolio);
		b.putBoolean(EDIT_ENABLED, editEnabled);
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
		outState.putStringArrayList(UPDATED_CATEGORIES, new ArrayList<String>(mUpdatedElements));
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
		} else if (mSharedPortfolio != null && !mSharedPortfolio.isOwned()) {
			// Do nothing
		} else {
			throw new RuntimeException("You have to pass a portfolio to " + this.getClass().getName());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Adding all items
		 inflater.inflate(R.menu.fragment_menu, menu);
			if (!mEditEnabled) {
				 menu.findItem(R.id.mainmenu_edit).setVisible(true);
				 menu.findItem(R.id.mainmenu_modify).setVisible(true);

			}
			else {
				 menu.findItem(R.id.mainmenu_edit).setVisible(false);
				 menu.findItem(R.id.mainmenu_modify).setVisible(false);

			}
//		for (OptionItem item : mOptionItems) {
//			if (item.getVisible()) {
//				menu.add(Menu.NONE, item.id, Menu.NONE, item.res).setIcon(item.icon)
//						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//			} else {
//				menu.add(Menu.NONE, item.id, Menu.NONE, item.res).setIcon(item.icon)
//				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//			}
//		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mainmenu_tag:
			TaggingDialog taggingDialog = new TaggingDialog(getActivity(), PortfolioFragment.this,
					PortfolioFragment.this, Concept.convertToSS(mPortfolio.tags));
			taggingDialog.show();
			// mFragmentLoader.load(AddTagFragment.class, true,
			// AddTagFragment.prepareArguments(mPortfolio));
			return true;
		case R.id.mainmenu_edit:
			setEditMode(true);
			return true;
		case R.id.mainmenu_modify:
			PMHelper.openPortfolioInBrowser(getSherlockActivity());
			return true;
		case R.id.mainmenu_trash:
			deletePortfolio();
			return true;
		case R.id.mainmenu_export:
			new PortfolioExportAsyncTask().execute(mPortfolio.getId());
			return true;
		case R.id.mainmenu_share:
			PMHelper.share(mPortfolio, getActivity());
			return true;
		case R.id.refresh_portfolios:
			refreshPortfolio();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshPortfolio() {
		new PortfolioRefreshAsyncTask().execute();
		
	}
	private class PortfolioRefreshAsyncTask extends SCAsyncTask<Void, Void, List<Portfolio>> {

		public PortfolioRefreshAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Void, List<Portfolio>>(getSherlockActivity()) {
				@Override
				public List<Portfolio> performAction(Void... params) throws SecurityException, Exception {
					PMHelper.resetData();
					return PMHelper.getPortfolioList();
				}

				@Override
				public void handleResult(List<Portfolio> result) {
					if (result != null) {
						//clear data e find if there is the same portfolio
						for (Portfolio portfolio:result){
							if (portfolio.getId().equals(mPortfolio.getId()))
							{
								//aggiorna il dato
								mPortfolio = portfolio;
								mPortfolioUtil = new PortfolioUtil(getActivity(), mPortfolio);
								mLoadTask = new LoadAsyncTask();
								mLoadTask.execute(true);
								return;
							}
						}
						getSherlockActivity().getSupportFragmentManager().popBackStack();
					}

				}
			});
		}
	}
	
	private void deletePortfolio() {

		// create a dialog alarm that ask if you are sure
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		// Add the buttons
		builder.setMessage(getSherlockActivity().getString(R.string.sure_delete_portfolio));
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// // User clicked OK button
				// // Removing portfolio
				// if (mPortfolioAsyncTask != null &&
				// !mPortfolioAsyncTask.isCancelled()) {
				// mPortfolioAsyncTask.cancel(true);
				// }
				// openedItem = NOT_VALID_POSITION;
				new SCAsyncTask<Portfolio, Void, List<Portfolio>>(getSherlockActivity(), new RemoveTaskProcessor(
						getSherlockActivity())).execute(mPortfolio);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
				dialog.dismiss();
			}
		});

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		// remove the element from the array
		dialog.show();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout
		View v = inflater.inflate(R.layout.frag_portfolio, null);
		// Retrieving UI references
		mTitleTextView = (TextView) v.findViewById(R.id.title);
		mTagsTextView = (TextView) v.findViewById(R.id.tags);
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
			Bundle args = getArguments();
			mEditEnabled = args == null ? false : args.getBoolean(EDIT_ENABLED);
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
			Bundle args = getArguments();
			mEditEnabled = args == null ? false : args.getBoolean(EDIT_ENABLED);
			mFooter.setVisibility(mEditEnabled ? View.VISIBLE : View.GONE);
			// Filling items
			ArrayList<OptionItem> list = savedInstanceState.getParcelableArrayList(OPTION_ITEMS_LIST);
			mOptionItems.addAll(list);
			// Getting updated categories
			mUpdatedElements = new HashSet<String>(savedInstanceState.getStringArrayList(UPDATED_CATEGORIES));
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
			}
		});
	}

	@Override
	public void onListItemClick(ListView parent, View v, int pos, long id) {
		// Checking that user isn't leaving screen without save
		if (mEditEnabled && !mUpdatedElements.isEmpty()) {
			return;
		}
		// Bundle reference
		Bundle args;
		// Matching position of category
		switch (pos) {
		case 0:
			args = PersonalInfoFragment.prepareArguments(mPortfolio);
			// Loading next fragment
			mFragmentLoader.load(PersonalInfoFragment.class, true, args);
			break;

		// break;
		default:
			// Retrieving element
			CategoryItem item = mCategories.get(pos);
			// Checking category
			if (Constants.CHERRY_ON_THE_CAKE.equalsIgnoreCase(item.category)) {
				args = CherryFragment.prepareArguments(mPortfolio);
				// Loading next fragment
				mFragmentLoader.load(CherryFragment.class, true, args);
				break;
			} else if (Constants.PRESENTATION.equalsIgnoreCase(item.category)) {
				args = PresentationFragment.prepareArguments(mPortfolio);
				// Loading next fragment
				mFragmentLoader.load(PresentationFragment.class, true, args);
			} else {
				args = UserProducedDataFragment.prepareArguments(mPortfolio, item.category);
				// Loading next fragment
				mFragmentLoader.load(UserProducedDataFragment.class, true, args);
			}
			break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mPortfolio != null) {
			mTagsTextView.setText(Concept.toSimpleString(mPortfolio.tags));
			mTitleTextView.setText(mPortfolio.name);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(mSharedPortfolio.isOwned());
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(mSharedPortfolio.isOwned());
		getSherlockActivity().getSupportActionBar().setTitle(R.string.portfolio);
		// Check if save button has to be visible or not
		hideOrShowSaveButton();
		// Canceling any active task
		cancelAnyActiveTask();
		// Checking if we are in shared or owned mode
		mLoadTask = new LoadAsyncTask();
		if (mPortfolio == null) {
			mLoadTask.execute(true);
		} else {
			mLoadTask.execute(false);
		}
	}

	private void cancelAnyActiveTask() {
		if (mLoadTask != null && !mLoadTask.isCancelled()) {
			mLoadTask.cancel(true);
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
	}

	private void prepareAdapter() {
		// Matching adapter type
		mAdapter = new CategoryArrayAdapter(mCategories);
	}


	/*
	 * private class PortfolioTask extends AsyncTask<Void, Void, Portfolio> {
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * Showing progress bar showProgressBar(true); }
	 * 
	 * @Override protected Portfolio doInBackground(Void... params) { return
	 * PMHelper.getPortfolio(mSharedPortfolio.getPortfolioId()); }
	 * 
	 * @Override protected void onPostExecute(Portfolio result) {
	 * super.onPostExecute(result); // Hiding progress bar
	 * showProgressBar(false); mTagsTextView.setText(null); if (result != null)
	 * { // Handling result mPortfolio = result; mPortfolioUtil = new
	 * PortfolioUtil(getActivity(), mPortfolio); // Setting portfolio name
	 * mTitleTextView.setText(result.name); // Starting new task for tags if
	 * (mTagTask != null && !mTagTask.isCancelled()) { mTagTask.cancel(true); }
	 * mTagTask = new TagTask(); mTagTask.execute(); // Starting new task for
	 * categories if (mCategoryTask != null && !mCategoryTask.isCancelled()) {
	 * mCategoryTask.cancel(true); } mCategoryTask = new
	 * CategoryTask(mEditEnabled); mCategoryTask.execute();
	 * 
	 * } else { mTitleTextView.setText(null);
	 * ToastBuilder.showShort(getActivity(), R.string.cannot_find_portfolio); }
	 * }
	 * 
	 * }
	 * 
	 * 
	 * private class TagTask extends AsyncTask<Void, Void, String> {
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * Showing progress bar showProgressBar(true); }
	 * 
	 * @Override protected String doInBackground(Void... params) { // Calling
	 * this static method to prepare tags map List<String> tags =
	 * PMHelper.getTags(mPortfolio); if (tags != null) { StringBuilder sb = new
	 * StringBuilder(); for (String tag : tags) { if
	 * (!tag.startsWith(mAppConfigurations.getDefaultTagCharacter())) {
	 * sb.append(mAppConfigurations.getDefaultTagCharacter()); }
	 * sb.append(tag).append(" "); } return sb.toString(); } else return null; }
	 * 
	 * @Override protected void onPostExecute(String result) {
	 * super.onPostExecute(result); // Hiding progress bar
	 * showProgressBar(false); if (result != null) {
	 * mTagsTextView.setText(result); } else { mTagsTextView.setText(null); } }
	 * 
	 * }
	 */
	// private class CategoryAsyncTask extends SCAsyncTask<Void, Void,
	// List<CategoryItem>> {
	//
	// public CategoryAsyncTask() {
	// super(getSherlockActivity(),
	// new AbstractAsyncTaskProcessor<Void,
	// List<CategoryItem>>(getSherlockActivity()) {
	// @Override
	// public List<CategoryItem> performAction(Void... params) throws
	// SecurityException, Exception {
	// return prepareCategories();
	// }
	//
	// @Override
	// public void handleResult(List<CategoryItem> result) {
	// // Clearing old list
	// mCategories.clear();
	// // Adding result
	// if (result != null) {
	// mCategories.addAll(result);
	// }
	// // Notifying adapter
	// mAdapter.notifyDataSetChanged();
	// }
	// });
	// }
	// }

	private static class Data {
		List<CategoryItem> items;
		Portfolio portfolio;
		boolean initView;
	}

	private class LoadAsyncTask extends SCAsyncTask<Boolean, Void, Data> {

		public LoadAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Boolean, Data>(getSherlockActivity()) {
				@Override
				public Data performAction(Boolean... params) throws SecurityException, Exception {
					Data data = new Data();
					if (params != null && params.length > 0 && params[0] != null && params[0]) {
						data.initView = true;
					} else {
						data.initView = false;
					}
					if (data.initView && mPortfolio == null) {
						if (mSharedPortfolio.isOwned())
							data.portfolio = PMHelper.getPortfolio(mSharedPortfolio.getPortfolioEntityId());
						else
							data.portfolio = mSharedPortfolio.getContainer().getPortfolio();
					} else {
						data.portfolio = mPortfolio;
					}

					if (mSharedPortfolio.isOwned())
						data.items = prepareCategories(PMHelper.getUserProducedDataList());
					else
						data.items = prepareCategories(mSharedPortfolio.getContainer().getSharedProducedDatas());
					return data;
				}

				@Override
				public void handleResult(Data result) {
					if (result.initView) {
						prepareView(result.portfolio);
					}
					// Clearing old list
					mCategories.clear();
					// Adding result
					if (result.items != null) {
						mCategories.addAll(result.items);
					}
					// Notifying adapter
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	/*
	 * // AsyncTask that charges categories private class CategoryTask extends
	 * AsyncTask<Void, Void, List<CategoryItem>> {
	 * 
	 * private boolean mEditEnabled;
	 * 
	 * public CategoryTask(boolean editEnabled) { mEditEnabled = editEnabled; //
	 * Retrieving arrays if (mCategoriesLabels == null) { mCategoriesLabels =
	 * getResources().getStringArray(R.array.portfolio_categories_labels);
	 * mCategoriesOrdered =
	 * getResources().getStringArray(R.array.portfolio_categories_ordered);
	 * mForceVisualization =
	 * getResources().getStringArray(R.array.portfolio_categories_shown); } //
	 * We assert that two lengths are the same because the second one // depends
	 * on the first one assert mCategoriesLabels.length ==
	 * mCategoriesOrdered.length && mCategoriesOrdered.length ==
	 * mForceVisualization.length; }
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * Showing progress bar showProgressBar(true); }
	 * 
	 * @Override protected List<CategoryItem> doInBackground(Void... params) {
	 * List<CategoryItem> items = new ArrayList<CategoryItem>(); // Checking
	 * mode mPortfolioUtil.initializeItems(PMHelper.getUserProducedDataList());
	 * // Maps for categories CategorizedData categorizedData =
	 * mPortfolioUtil.getCategorizedData(); // Building list of category
	 * HashMap<String, List<String>> shown = categorizedData.shownMap;
	 * HashMap<String, List<String>> hidden = categorizedData.hiddenMap;
	 * HashMap<String, List<String>> cherries = categorizedData.cherriesMap; //
	 * Starting building items for (int i = 0; i < mCategoriesOrdered.length;
	 * i++) { // Getting key String rawKey = mCategoriesOrdered[i]; // Preparing
	 * list item CategoryItem item = new CategoryItem(); item.category = rawKey;
	 * item.label = mCategoriesLabels[i]; item.isEmpty = true; // Boolean that
	 * checks if we have to add category to list boolean accept = item.isShown =
	 * Boolean.parseBoolean(mForceVisualization[i]); // If we have got cherries
	 * elements we have to add its label if
	 * (getString(R.string.cherry_on_the_cake).equalsIgnoreCase(item.label)) {
	 * // This hasn't any label so we have to use this trick item.category =
	 * item.label; // Checking mode if (mEditEnabled) { accept =
	 * !cherries.isEmpty(); item.isEmpty = cherries.isEmpty(); item.isShown =
	 * !cherries.isEmpty(); } else { accept =
	 * mUpdatedElements.contains(item.category) ? cherries.isEmpty() :
	 * !cherries.isEmpty(); item.isShown = !cherries.isEmpty(); item.isEmpty =
	 * cherries.isEmpty(); } } else if (!TextUtils.isEmpty(rawKey)) { //
	 * Checking if we have // to visualize it String[] keys =
	 * rawKey.replaceAll("\\s+", "").split(","); // Checking mode if
	 * (mEditEnabled) { for (String key : keys) { // Checking that there are
	 * some elements List<String> data = shown.get(key); if (data != null &&
	 * !data.isEmpty()) { accept = true; item.isShown = true; item.isEmpty =
	 * false; } else { item.isEmpty = true && item.isEmpty; } data =
	 * hidden.get(key); if (data != null && !data.isEmpty()) { accept = true;
	 * item.isEmpty = false; } else { item.isEmpty = true && item.isEmpty; } } }
	 * else { for (String key : keys) { // Checking that there are some elements
	 * List<String> data = shown.get(key); if (data != null && !data.isEmpty())
	 * { accept = true; item.isEmpty = false; item.isShown = true; } } //
	 * Checking if user has updated element accept =
	 * mUpdatedElements.contains(item.category) ? !accept : accept; } } //
	 * Adding item if (accept) { items.add(item); } } return items; }
	 * 
	 * @Override protected void onPostExecute(List<CategoryItem> result) {
	 * super.onPostExecute(result); // Hiding progress bar
	 * showProgressBar(false); // Clearing old list mCategories.clear(); //
	 * Adding result if (result != null) { mCategories.addAll(result); } //
	 * Notifying adapter mAdapter.notifyDataSetChanged(); } }
	 */

	// private void showProgressBar(boolean show) {
	// getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
	// }

	// Adapter used for Categories
	private class CategoryArrayAdapter extends ArrayAdapter<CategoryItem> {

		public class Holder {
			ImageView cherryImageView;
			TextView categoryTextView;
			ImageButton eyeButton;
		}

		public CategoryArrayAdapter(List<CategoryItem> objects) {
			super(getActivity(), R.layout.item_category, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_category, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.cherryImageView = (ImageView) convertView.findViewById(R.id.cherry);
				holder.categoryTextView = (TextView) convertView.findViewById(R.id.category);
				holder.eyeButton = (ImageButton) convertView.findViewById(R.id.eye_button);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Retrieving category
			final CategoryItem item = getItem(position);
			// Preparing label and ImageView
			holder.categoryTextView.setText(item.label);
			// Checking if we have to hide or visualize category
			boolean updated = mUpdatedElements.contains(item.category);
			if (updated ? !item.isShown || position == 0 : item.isShown) {
				holder.cherryImageView.setImageResource(R.drawable.ic_cherry_selected);
				holder.eyeButton.setImageResource(R.drawable.ic_eye_selected);
				// Setting backround
				convertView.setBackgroundResource(R.drawable.default_view_background);
				// Managing cherry on the cake visualization
				if (getString(R.string.cherry_on_the_cake).equalsIgnoreCase(item.label)) {
					holder.categoryTextView.setTextColor(getResources().getColor(R.color.cherry));
					holder.cherryImageView.setVisibility(View.VISIBLE);
				} else {
					holder.categoryTextView.setTextColor(getResources().getColor(R.color.black));
					holder.cherryImageView.setVisibility(View.GONE);
				}
			} else {
				holder.cherryImageView.setImageResource(R.drawable.ic_cherry_unselected);
				holder.eyeButton.setImageResource(R.drawable.ic_eye_unselected);
				// Setting backround
				convertView.setBackgroundResource(R.drawable.gray_view_background);
				holder.categoryTextView.setTextColor(getResources().getColor(R.color.gray));
				// Managing cherry on the cake visualization
				if (getString(R.string.cherry_on_the_cake).equalsIgnoreCase(item.label)) {
					holder.cherryImageView.setVisibility(View.VISIBLE);
				} else {
					holder.cherryImageView.setVisibility(View.GONE);
				}
			}
			// Checking if we are editing categories
			if (mEditEnabled && !(position == 0)) {
				holder.eyeButton.setVisibility(View.VISIBLE);
			} else {
				holder.eyeButton.setVisibility(View.GONE);
			}
			// Setting listener
			holder.eyeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Checking if the user is trying to modify an existent
					// update
					if (mUpdatedElements.contains(item.category)) {
						mUpdatedElements.remove(item.category);
					} else {
						mUpdatedElements.add(item.category);
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
			if (!mSharedPortfolio.isOwned() || getArguments().getBoolean(EDIT_ENABLED)) {
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
		// Starting new task for categories
		// if (mCategoryTask != null && !mCategoryTask.isCancelled()) {
		// mCategoryTask.cancel(true);
		// }
		// mCategoryTask = new CategoryAsyncTask();
		// mCategoryTask.execute();
		mLoadTask = new LoadAsyncTask();
		mLoadTask.execute(false);
	}

	// Wrapper for Categories
	private class CategoryItem {

		private String label;
		private boolean isShown;
		private boolean isEmpty;
		private String category;

	}

	private class SaveTaskProcessor extends AbstractAsyncTaskProcessor<Void, Void> {

		public SaveTaskProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Void performAction(Void... params) throws SecurityException, Exception {
			PMHelper.savePortfolioData(mPortfolio, mUpdatedElements);
			return null;
		}

		@Override
		public void handleResult(Void result) {
			mUpdatedElements.clear();
			setEditMode(false);
		}
	}

	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return SuggestionHelper.getSuggestions(text, getActivity(), Preferences.getHost(getActivity()),
					PMHelper.getAuthToken(), Preferences.getAppToken());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
		List<Concept> list = Concept.convertSS(suggestions);
		new TaggingAsyncTask().execute(list);
	}

	private List<CategoryItem> prepareCategories(List<UserProducedData> updList) throws NameNotFoundException,
			DataException, ConnectionException, ProtocolException, SecurityException {
		// Retrieving arrays
		if (mCategoriesLabels == null) {
			mCategoriesLabels = getResources().getStringArray(R.array.portfolio_categories_labels);
			mCategoriesOrdered = getResources().getStringArray(R.array.portfolio_categories_ordered);
			mForceVisualization = getResources().getStringArray(R.array.portfolio_categories_shown);
		}
		// We assert that two lengths are the same because
		// the second one
		// depends on the first one
		assert mCategoriesLabels.length == mCategoriesOrdered.length
				&& mCategoriesOrdered.length == mForceVisualization.length;
		List<CategoryItem> items = new ArrayList<CategoryItem>();
		// Checking mode
		mPortfolioUtil.initializeItems(updList);
		// Maps for categories
		CategorizedData categorizedData = mPortfolioUtil.getCategorizedData();
		// Building list of category
		HashMap<String, List<String>> shown = categorizedData.shownMap;
		HashMap<String, List<String>> hidden = categorizedData.hiddenMap;
		HashMap<String, List<String>> cherries = categorizedData.cherriesMap;
		// Starting building items
		for (int i = 0; i < mCategoriesOrdered.length; i++) {
			// Getting key
			String rawKey = mCategoriesOrdered[i];
			// Preparing list item
			CategoryItem item = new CategoryItem();
			item.category = rawKey;
			item.label = mCategoriesLabels[i];
			item.isEmpty = true;
			// Boolean that checks if we have to add
			// category to list
			boolean accept = item.isShown = Boolean.parseBoolean(mForceVisualization[i]);
			// If we have got cherries elements we have to
			// add its label
			if (getString(R.string.cherry_on_the_cake).equalsIgnoreCase(item.label)) {
				// This hasn't any label so we have to use
				// this trick
				item.category = item.label;
				// Checking mode
				if (mEditEnabled) {
					accept = !cherries.isEmpty();
					item.isEmpty = cherries.isEmpty();
					item.isShown = !cherries.isEmpty();
				} else {
					accept = mUpdatedElements.contains(item.category) ? cherries.isEmpty() : !cherries.isEmpty();
					item.isShown = !cherries.isEmpty();
					item.isEmpty = cherries.isEmpty();
				}
			} else if (!TextUtils.isEmpty(rawKey)) { // Checking
														// if
														// we
														// have
														// to
														// visualize
														// it
				String[] keys = rawKey.replaceAll("\\s+", "").split(",");
				// Checking mode
				if (mEditEnabled) {
					for (String key : keys) {
						// Checking that there are some
						// elements
						List<String> data = shown.get(key);
						if (data != null && !data.isEmpty()) {
							accept = true;
							item.isShown = true;
							item.isEmpty = false;
						} else {
							item.isEmpty = true && item.isEmpty;
						}
						data = hidden.get(key);
						if (data != null && !data.isEmpty()) {
							accept = true;
							item.isEmpty = false;
						} else {
							item.isEmpty = true && item.isEmpty;
						}
					}
				} else {
					for (String key : keys) {
						// Checking that there are some
						// elements
						List<String> data = shown.get(key);
						if (data != null && !data.isEmpty()) {
							accept = true;
							item.isEmpty = false;
							item.isShown = true;
						}
					}
					// Checking if user has updated element
					accept = mUpdatedElements.contains(item.category) ? !accept : accept;
				}
			}
			// Adding item
			if (accept) {
				items.add(item);
			}
		}
		return items;
	}

	private void prepareView(Portfolio result) {
		if (result != null) {
			mTagsTextView.setText(Concept.toSimpleString(result.tags));
			// Handling result
			mPortfolio = result;
			mPortfolioUtil = new PortfolioUtil(getActivity(), mPortfolio);
			// Setting portfolio name
			mTitleTextView.setText(result.name);
			// // Starting new task for tags
			// if (mTagTask != null && !mTagTask.isCancelled()) {
			// mTagTask.cancel(true);
			// }
			// mTagTask = new TagTask();
			// mTagTask.execute();
			// Starting new task for categories
			// if (mCategoryTask != null && !mCategoryTask.isCancelled()) {
			// mCategoryTask.cancel(true);
			// }
			// mCategoryTask = new CategoryAsyncTask();
			// mCategoryTask.execute();

		} else {
			mTagsTextView.setText(null);
			mTitleTextView.setText(null);
			ToastBuilder.showShort(getActivity(), R.string.cannot_find_portfolio);
		}
	}

	private class TaggingAsyncTask extends SCAsyncTask<List<Concept>, Void, String> {

		public TaggingAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<List<Concept>, String>(getSherlockActivity()) {
				@Override
				public String performAction(List<Concept>... params) throws SecurityException, Exception {
					PMHelper.saveTags(mPortfolio, params[0]);
					mPortfolio.tags = params[0];
					return Concept.toSimpleString(params[0]);
				}

				@Override
				public void handleResult(String result) {
					mTagsTextView.setText(result);
				}
			});
		}
	}

	private class PortfolioExportAsyncTask extends AsyncTask<String, Void, byte[]> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress bar
			showProgressBar(true);
		}

		protected byte[] doInBackground(String... params) {
			try {
				return PMHelper.exportPortfolio(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(this.getClass().getSimpleName(), "Exception getting portfolio export from server");
			}
			return null;
		}

		@Override
		protected void onPostExecute(byte[] result) {
			// Hiding progress bar
			showProgressBar(false);
			if (result != null) {
				File saving = new File(getActivity().getExternalFilesDir(null), "portfolio.pdf");
				FileOutputStream fout;
				try {
					fout = new FileOutputStream(saving);
					fout.write(result);
					fout.flush();
					fout.close();
				} catch (FileNotFoundException e) {
					Log.e(this.getClass().getSimpleName(), saving.getAbsolutePath() + " not found");
					ToastBuilder.showShort(getActivity(), "An error occurred exporting portfolio");
				} catch (IOException e) {
					Log.e(this.getClass().getSimpleName(), saving.getAbsolutePath() + " cannot be open");
					ToastBuilder.showShort(getActivity(), "An error occurred exporting portfolio");
				}

				Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
				pdfIntent.setDataAndType(Uri.fromFile(saving), "application/pdf");
				pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				try {
					startActivity(pdfIntent);
				} catch (ActivityNotFoundException e) {
					ToastBuilder.showShort(getActivity(),
							"You have to install a pdf reader app to export the portfolio");
				}
			} else {
				ToastBuilder.showShort(getActivity(), "An error occurred exporting portfolio");
			}

		}

		private void showProgressBar(boolean show) {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
		}

	}

	private class RemoveTaskProcessor extends AbstractAsyncTaskProcessor<Portfolio, List<Portfolio>> {

		public RemoveTaskProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<Portfolio> performAction(Portfolio... params) throws SecurityException, Exception {
			PMHelper.removePortfolio(params[0]);
			return PMHelper.getPortfolioList();
		}

		@Override
		public void handleResult(List<Portfolio> result) {
			if (result != null) {
				getSherlockActivity().getSupportFragmentManager().popBackStack();
			} else
				Toast.makeText(getSherlockActivity(), R.string.problem_delete_portfolio, Toast.LENGTH_LONG).show();

		}
	}

}
