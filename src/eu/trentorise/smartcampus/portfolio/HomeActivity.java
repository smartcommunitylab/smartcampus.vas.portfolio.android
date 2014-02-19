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
package eu.trentorise.smartcampus.portfolio;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.LauncherHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.portfolio.frags.PortfolioFragment;
import eu.trentorise.smartcampus.portfolio.frags.PortfoliosListFragment;
import eu.trentorise.smartcampus.portfolio.interfaces.FragmentLoader;
import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.OnBackPressedListener;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.SharedPortfolioContainer;
import eu.trentorise.smartcampus.portfolio.scutils.Constants;
import eu.trentorise.smartcampus.portfolio.user.Notes;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.SoftKeyboard;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * FragmentActivity that works as container for all Fragments.
 * 
 * @author Simone Casagranda
 * 
 */
public class HomeActivity extends SherlockFragmentActivity implements
		FragmentLoader, NoteLayerInteractor, SharedPortfolio {

	private Button mSlidingButton;
	private EditText mNotesEditText;
	private SlidingDrawer mSlidingDrawer;

	private NotesAsyncTask mNotesTask;

	private Boolean owned = null;
	private String mPortfolioEntityId;

	private SharedPortfolioContainer sharedPortfolioContainer;
	private boolean initialized = false;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("initialized", initialized);
		if (sharedPortfolioContainer != null) {
			outState.putParcelable("sharedPortfolio", sharedPortfolioContainer);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		boolean managed = false;
		// Closing sliding drawer
		if (mSlidingDrawer.isShown() && mSlidingDrawer.isOpened()) {
			mSlidingDrawer.animateClose();
		} else {
			Fragment currentFragment = getSupportFragmentManager()
					.findFragmentById(R.id.fragment_container);
			// Checking if there is a fragment that it's listening for back
			// button
			if (currentFragment != null
					&& currentFragment instanceof OnBackPressedListener) {
				managed = ((OnBackPressedListener) currentFragment)
						.onBackPressed();
			}
			// If it's not managed we can continue
			if (!managed) {
				super.onBackPressed();
			}
		}
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			PMHelper.init(getApplicationContext());
		} catch (Exception e) {
			PMHelper.endAppFailure(this, R.string.app_failure_setup);
		}
	}

	private boolean initData(Bundle savedInstanceState) {

		try {
			// Loading first fragment that works as home for application.
			// Getting token
			if (!isViewer()
					&& (savedInstanceState == null || !savedInstanceState
							.getBoolean("initialized"))) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				Fragment frag = new PortfoliosListFragment();
				ft.replace(R.id.fragment_container, frag)
						.commitAllowingStateLoss();
			} else if (savedInstanceState != null
					&& savedInstanceState.containsKey("sharedPortfolio")) {
				sharedPortfolioContainer = savedInstanceState
						.getParcelable("sharedPortfolio");
				if (!sharedPortfolioContainer.getPortfolio().entityId
						.equals(mPortfolioEntityId)) {
					new LoadPortfolioAsyncTask().execute(mPortfolioEntityId);
				}
			} else {
				new LoadPortfolioAsyncTask().execute(mPortfolioEntityId);
			}

		} catch (Exception e1) {
			PMHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (LauncherHelper.isLauncherInstalled(this, true)) {
			// Checking start action
			if (isViewer()) {
				mPortfolioEntityId = getIntent().getStringExtra(
						getString(R.string.view_intent_arg_object_id));
			}

			initDataManagement(savedInstanceState);

			try {
				if (!PMHelper.getAccessProvider().login(this, null)) {
					initData(savedInstanceState);
				}
			} catch (AACException e) {
				PMHelper.endAppFailure(this, R.string.app_failure_setup);
				return;
			}

			setupContent();

			initialized = true;
		}
	}

	private void setupContent() {
		// Asking for windows features
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		// Setting content view
		setContentView(R.layout.activity_home);
		// Getting UI references
		mSlidingButton = (Button) findViewById(R.id.handle);
		mNotesEditText = (EditText) findViewById(R.id.note_edittext);
		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		// Setting listeners
		mSlidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			@Override
			public void onDrawerOpened() {
				// Acquiring keyboard
				mNotesEditText.requestFocus();
				SoftKeyboard
						.showSoftKeyboard(HomeActivity.this, mNotesEditText);
				// Changing notes button
				mSlidingButton.setBackgroundResource(R.drawable.btn_closenotes);
				// Starting task
				cancelNotesTask();
				mNotesTask = new NotesAsyncTask();
				mNotesTask.execute(false);
				Toast.makeText(HomeActivity.this,
						getString(R.string.notes_hint), Toast.LENGTH_LONG)
						.show();
			}
		});
		mSlidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
			@Override
			public void onDrawerClosed() {
				// Hiding keyboard
				SoftKeyboard
						.hideSoftKeyboard(HomeActivity.this, mNotesEditText);
				// Changing notes button
				mSlidingButton.setBackgroundResource(R.drawable.btn_opennotes);
				if (areNotesChanged() && isOwned()) {
					// Starting task
					cancelNotesTask();
					mNotesTask = new NotesAsyncTask();
					mNotesTask.execute(true);
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Hiding progress bar
		setSupportProgressBarIndeterminateVisibility(false);
		// Starting notes task for loading notes
		cancelNotesTask();
		// mNotesTask = new NotesAsyncTask();
		// mNotesTask.execute(false);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Cancelling any active task
		cancelNotesTask();
		// Closing sliding drawer
		if(mSlidingDrawer!=null)
			mSlidingDrawer.close();
	}

	private void cancelNotesTask() {
		if (mNotesTask != null && !mNotesTask.isCancelled()) {
			mNotesTask.cancel(true);
		}
	}

	@Override
	public void load(Class<?> frag, boolean keepOldInStack, Bundle args) {
		// Closing Sliding drawer
		if (mSlidingDrawer.isOpened()) {
			mSlidingDrawer.animateClose();
		}
		// Starting transaction
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment fragment = (Fragment) Fragment.instantiate(this,
				frag.getName());
		// Setting data
		if (args != null) {
			fragment.setArguments(args);
		}
		// Replacing old fragment with new one
		ft.replace(R.id.fragment_container, fragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		if (keepOldInStack) {
			ft.addToBackStack(null);
		}
		ft.commit();
	}

	@Override
	public boolean isOwned() {
		if (owned == null) {
			if (!isViewer())
				owned = true;
			else {
				try {
					owned = PMHelper.isOwnPortfolio(mPortfolioEntityId);
					;
				} catch (Exception e) {
					Log.e(getClass().getName(), "" + e.getMessage());
					owned = false;
				}
			}
		}
		return owned;
	}

	private boolean isViewer() {
		return Constants.ACTION_VIEW.equals(getIntent().getAction());
	}

	@Override
	public int getPermissionLevel() {
		// Here you can manage permission level for children
		return 0;
	}

	@Override
	public String getPortfolioEntityId() {
		return mPortfolioEntityId;
	}

	@Override
	public void setVisibility(boolean visible) {
		if (!isOwned()) {
			mSlidingDrawer.setVisibility(View.GONE);
			return;
		}
		mSlidingDrawer.close();
		// Checking if we have to visualize or not the sliding drawer
		if (visible) {
			mSlidingDrawer.setVisibility(View.VISIBLE);
		} else {
			mSlidingDrawer.setVisibility(View.GONE);
		}
	}

	private boolean areNotesChanged() {
		String actual = mNotesEditText.getText().toString();
		// Checking if we have got a new String
		return !actual.equals(Notes.getNotes(this));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		// if (resultCode == RESULT_OK) {
		// String token = data.getExtras().getString(
		// AccountManager.KEY_AUTHTOKEN);
		// if (token == null) {
		// PMHelper.endAppFailure(this, R.string.app_failure_security);
		// } else {
		// initData(token, null);
		// }
		// } else if (resultCode == RESULT_CANCELED && requestCode ==
		// SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
		// PMHelper.endAppFailure(this,
		// eu.trentorise.smartcampus.ac.R.string.token_required);
		// }

		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(
						AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					PMHelper.endAppFailure(this, R.string.app_failure_security);
				} else {
					initData(null);
				}
			} else if (resultCode == RESULT_CANCELED
					&& requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
				PMHelper.endAppFailure(this, R.string.token_required);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	/**
	 * AsyncTask that allows to store and retrieve notes
	 */
	private class NotesAsyncTask extends SCAsyncTask<Boolean, Void, String> {

		public NotesAsyncTask() {
			super(HomeActivity.this,
					new AbstractAsyncTaskProcessor<Boolean, String>(
							HomeActivity.this) {
						@Override
						public String performAction(Boolean... params)
								throws SecurityException, Exception {
							if (params[0]) {
								PMHelper.setNotes(mNotesEditText.getText()
										.toString());
							}
							return PMHelper.getNotes();
						}

						@Override
						public void handleResult(String result) {
							mNotesEditText.setText(result);
						}
					});
		}
	}

	/**
	 * AsyncTask that allows to store and retrieve notes
	 */
	private class LoadPortfolioAsyncTask extends
			SCAsyncTask<String, Void, Portfolio> {

		public LoadPortfolioAsyncTask() {
			super(HomeActivity.this,
					new AbstractAsyncTaskProcessor<String, Portfolio>(
							HomeActivity.this) {
						@Override
						public Portfolio performAction(String... params)
								throws SecurityException, Exception {
							Portfolio p = null;
							if (params != null && params.length > 0
									&& params[0] != null) {
								owned = PMHelper.isOwnPortfolio(params[0]);
								if (owned) {
									p = PMHelper.findPortfolio(params[0]);
								} else {
									sharedPortfolioContainer = PMHelper
											.getSharedPortfolioContainer(params[0]);
									if (sharedPortfolioContainer == null)
										return null;
									p = sharedPortfolioContainer.getPortfolio();
								}
							}
							return p;
						}

						@Override
						public void handleResult(Portfolio result) {
							if (result != null) {
								FragmentTransaction ft = getSupportFragmentManager()
										.beginTransaction();
								Fragment frag = new PortfolioFragment();
								frag.setArguments(PortfolioFragment
										.prepareArguments(result, false));
								ft.replace(R.id.fragment_container, frag)
										.commit();
							} else {
								Toast.makeText(HomeActivity.this,
										R.string.not_found_portfolios,
										Toast.LENGTH_LONG).show();
							}
						}
					});
		}
	}

	@Override
	public SharedPortfolioContainer getContainer() {
		return sharedPortfolioContainer;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
