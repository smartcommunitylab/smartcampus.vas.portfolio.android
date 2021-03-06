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
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.TutorialHelper;
import com.github.espiandev.showcaseview.TutorialHelper.TutorialProvider;
import com.github.espiandev.showcaseview.TutorialItem;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.common.Preferences;
import eu.trentorise.smartcampus.portfolio.CreatePortfolioDialog;
import eu.trentorise.smartcampus.portfolio.PMHelper;
import it.smartcampuslab.portfolio.R;
import eu.trentorise.smartcampus.portfolio.interfaces.FragmentLoader;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.Concept;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.ToastBuilder;
import eu.trentorise.smartcampus.portfolio.utils.WelcomeDlgHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * Fragment that visualizes all portfolios of user.
 * 
 * @author Simone Casagranda
 * 
 */
public class PortfoliosListFragment extends SherlockListFragment implements TaggingDialog.TagProvider {

	// private static final String TAG_DIALOG_PORTFOLIO_CREATE =
	// "dialog-portfolio-create";

	public static final String TUTORIAL_HELPER = "tutorial helper";

	private  List<Portfolio> mPortfolios = new ArrayList<Portfolio>();
	private FragmentLoader mFragmentLoader;
	private NoteLayerInteractor mNoteLayerInteractor;
	private PorfolioAdapter mArrayAdapter;

	private PortfolioListAsyncTask mPortfolioAsyncTask;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentLoader) {
			mFragmentLoader = (FragmentLoader) activity;
		} else {
			throw new RuntimeException("The container Activity has to be an instance of FragmentLoader");
		}
		if (activity instanceof NoteLayerInteractor) {
			mNoteLayerInteractor = (NoteLayerInteractor) activity;
		} else {
			throw new RuntimeException("The container Activity has to be an instance of NoteLayerInteractor");
		}
		if (activity instanceof SharedPortfolio) {
			assert ((SharedPortfolio) activity).isOwned();
		}
		// Getting app configurations
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		 inflater.inflate(R.menu.fragmentlist_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.create_portfolio:
			Intent intent = new Intent(getActivity(), CreatePortfolioDialog.class);
			startActivity(intent);
			return true;
		case R.id.modify_portfolio:
			PMHelper.openPortfolioInBrowser(getSherlockActivity());
			return true;
		case R.id.tutorial_portfolio:
			TutorialHelper mTutorialHelper = new TutorialHelper(getActivity(), mTutorialProvider);
			mTutorialHelper.showTutorials();
			return true;
		case R.id.refresh_portfolios:
			refreshPortfolios();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshPortfolios() {
		new PortfolioListAsyncTask().execute(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout into fragment
		View v = inflater.inflate(R.layout.listfrag_portfolios, null);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Preparing adapter
		mArrayAdapter = new PorfolioAdapter(getActivity(), mPortfolios);
		// Setting adapter to ListView
		getListView().setAdapter(mArrayAdapter);
		getListView().setOnItemClickListener(mArrayAdapter);
		getListView().setOnScrollListener(mArrayAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.my_portfolios);
		// Showing note layer
		mNoteLayerInteractor.setVisibility(true);
		// Updating data
		mPortfolioAsyncTask = new PortfolioListAsyncTask();
		mPortfolioAsyncTask.execute();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mPortfolioAsyncTask != null && !mPortfolioAsyncTask.isCancelled()) {
			mPortfolioAsyncTask.cancel(true);
		}
	}

	private class PortfolioListAsyncTask extends SCAsyncTask<Boolean, Void, List<Portfolio>> {

		public PortfolioListAsyncTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Boolean, List<Portfolio>>(getSherlockActivity()) {
				@Override
				public List<Portfolio> performAction(Boolean... params) throws SecurityException, Exception {
					if (params != null && params.length > 0 && params[0] != null && params[0]) {
						PMHelper.resetData();
					}
					return PMHelper.getPortfolioList();
				}

				@Override
				public void handleResult(List<Portfolio> result) {
					if (result != null) {
						// Clearing old data
						mPortfolios.clear();
						// Adding new data
						mPortfolios.addAll(result);
						// Notifying adapter about new elements
						mArrayAdapter.notifyDataSetChanged();
					}

					if (!WelcomeDlgHelper.isWelcomeShown(getActivity())) {
						new TutorialHelper(getActivity(), mTutorialProvider).showTourDialog(getString(R.string.welcome_title), getString(R.string.welcome_msg3), getString(com.github.espiandev.showcaseview.R.string.begin_tut));
						WelcomeDlgHelper.setWelcomeShown(getActivity());
					}
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

	private class PorfolioAdapter extends ArrayAdapter<Portfolio> implements OnScrollListener, OnItemClickListener {

		private static final int NOT_VALID_POSITION = -1;

		private int openedItem = NOT_VALID_POSITION;
		private Context mContext;

		public class Holder {
			View visibleView, hiddenView;
			TextView portfolioNameTextView;
			ImageButton trashButton, exportButton, shareButton;
			ImageButton tagButton, editButton;
		}

		public PorfolioAdapter(Context context, List<Portfolio> objects) {
			super(context, R.layout.item_portfoliolist, objects);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_portfoliolist, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.portfolioNameTextView = (TextView) convertView.findViewById(R.id.portfolio_name);
				holder.visibleView = (View) convertView.findViewById(R.id.visible_view);
				holder.hiddenView = (View) convertView.findViewById(R.id.hidden_view);
				holder.trashButton = (ImageButton) convertView.findViewById(R.id.trash);
				holder.exportButton = (ImageButton) convertView.findViewById(R.id.export);
				holder.shareButton = (ImageButton) convertView.findViewById(R.id.share);
				holder.tagButton = (ImageButton) convertView.findViewById(R.id.tag);
				holder.editButton = (ImageButton) convertView.findViewById(R.id.edit);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Retrieving Portfolio at position n
			final Portfolio p = getItem(position);
			// Filling data
			holder.portfolioNameTextView.setText(p.name);
			// Adding button listeners
			holder.trashButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// create a dialog alarm that ask if you are sure
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					// Add the buttons
					builder.setMessage(mContext.getString(R.string.sure_delete_portfolio));
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
							// Removing portfolio
							if (mPortfolioAsyncTask != null && !mPortfolioAsyncTask.isCancelled()) {
								mPortfolioAsyncTask.cancel(true);
							}
							openedItem = NOT_VALID_POSITION;
							new SCAsyncTask<Portfolio, Void, List<Portfolio>>(PortfoliosListFragment.this
									.getSherlockActivity(), new RemoveTaskProcessor(getSherlockActivity())).execute(p);
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
					/*
					 * // Removing portfolio if (mPortfolioAsyncTask != null &&
					 * !mPortfolioAsyncTask.isCancelled()) {
					 * mPortfolioAsyncTask.cancel(true); } openedItem =
					 * NOT_VALID_POSITION; new SCAsyncTask<Portfolio, Void,
					 * List<Portfolio>>(
					 * PortfoliosListFragment.this.getSherlockActivity(), new
					 * RemoveTaskProcessor(getSherlockActivity())).execute(p);
					 */
				}
			});

			holder.exportButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new PortfolioExportAsyncTask().execute(p.getId());
				}
			});
			holder.shareButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PMHelper.share(p, getActivity());
				}
			});
			holder.tagButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TaggingDialog taggingDialog = new TaggingDialog(getActivity(),
							new TaggingDialog.OnTagsSelectedListener() {

								@SuppressWarnings("unchecked")
								@Override
								public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
									new TaggingAsyncTask(p).execute(Concept.convertSS(suggestions));
								}
							}, PortfoliosListFragment.this, Concept.convertToSS(p.tags));
					taggingDialog.show();
					// mFragmentLoader.load(AddTagFragment.class, true,
					// AddTagFragment.prepareArguments(p));
				}
			});
			holder.editButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Loading edit portfolio fragment
					mFragmentLoader.load(PortfolioFragment.class, true, PortfolioFragment.prepareArguments(p, true));
				}
			});
			// Hiding visible view if it's the one hidden at position n
			if (openedItem == position) {
				holder.hiddenView.setVisibility(View.VISIBLE);
				holder.visibleView.setVisibility(View.INVISIBLE);
			} else {
				holder.visibleView.setVisibility(View.VISIBLE);
				holder.hiddenView.setVisibility(View.INVISIBLE);
			}
			// Visualizing hidden view
			if (position == openedItem && holder.visibleView.isShown()) {
				holder.visibleView.setVisibility(View.INVISIBLE);
				holder.hiddenView.setVisibility(View.VISIBLE);
			} else if (holder.hiddenView.isShown()) {
				holder.hiddenView.setVisibility(View.INVISIBLE);
				holder.visibleView.setVisibility(View.VISIBLE);
			}
			// Returning just filled view
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			// Loading next fragment
			Portfolio p = mArrayAdapter.getItem(pos);
			mFragmentLoader.load(PortfolioFragment.class, true, PortfolioFragment.prepareArguments(p, false));
		}

		// @Override
		// public boolean onItemLongClick(AdapterView<?> parent, View v, int
		// pos, long id) {
		// if (pos != openedItem) {
		// // Updating item selected position
		// openedItem = pos;
		// notifyDataSetChanged();
		// }
		// return true;
		// }

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (openedItem != NOT_VALID_POSITION) {
				openedItem = NOT_VALID_POSITION;
				notifyDataSetChanged();
			}
		}
	}

	private class RemoveTaskProcessor extends AbstractAsyncTaskProcessor<Portfolio, List<Portfolio>> {

		public RemoveTaskProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<Portfolio> performAction(Portfolio... params) throws SecurityException, Exception {
			PMHelper.removePortfolioFromList(params[0]);
			return PMHelper.getPortfolioList();
		}

		@Override
		public void handleResult(List<Portfolio> result) {
			if (result != null) {
				mPortfolios.clear();
				mPortfolios.addAll(result);
				mArrayAdapter.notifyDataSetChanged();
			}

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

	private class TaggingAsyncTask extends SCAsyncTask<List<Concept>, Void, Void> {

		public TaggingAsyncTask(final Portfolio p) {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<List<Concept>, Void>(getSherlockActivity()) {
				@Override
				public Void performAction(List<Concept>... params) throws SecurityException, Exception {
					PMHelper.saveTags(p, params[0]);
					p.tags = params[0];
					return null;
				}

				@Override
				public void handleResult(Void result) {
					ToastBuilder.showShort(getActivity(), R.string.tags_successfully_added);
				}
			});
		}
	}

	private TutorialProvider mTutorialProvider = new TutorialProvider() {
		int[] tutorialId = new int[]{R.id.modify_portfolio,R.id.create_portfolio};
		TutorialItem[] tutorial = new TutorialItem[]{
				new TutorialItem("modify", null, 0, R.string.tut_title_modify, R.string.tut_text_modify),
				new TutorialItem("create", null, 0, R.string.tut_title_create, R.string.tut_text_create),
		}; 

		
		@Override
		public void onTutorialFinished() {
		}
		
		@Override
		public void onTutorialCancelled() {
		}
		
		@Override
		public TutorialItem getItemAt(int i) {
			View v = getActivity().findViewById(tutorialId[i]);
			if (v != null) {
				tutorial[i].position = new int[2];
				v.getLocationOnScreen(tutorial[i].position);
				tutorial[i].width = v.getWidth();
			}
			return tutorial[i];
		}
		
		@Override
		public int size() {
			return tutorial.length;
		}
	};

}
