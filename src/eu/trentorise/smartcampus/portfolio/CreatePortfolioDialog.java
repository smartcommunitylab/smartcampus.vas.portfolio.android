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

import it.smartcampuslab.portfolio.R;

import java.util.List;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.validation.ValidatorHelper;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * Activity that works as Dialog to allow user the insertion of a new Portfolio.
 * (CreatePortfolioFragment works but has some bad aspects)
 * 
 * @author Simone Casagranda
 * 
 */
public class CreatePortfolioDialog extends SherlockActivity {

	private Button mPositiveButton, mNegativeButton;
	private EditText mPortfolioNameEditText;
	boolean exist = false;

//	private ProgressBar mProgressBar;
	
	private CreatePortFolioTask mCreatePortFolioAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_portfolio_dialog);
		// Retrieving UI references
		mPositiveButton = (Button) findViewById(R.id.create_portfolio_button);
		mNegativeButton = (Button) findViewById(R.id.cancel);
		mPortfolioNameEditText = (EditText) findViewById(R.id.portfolio_title);
//		mProgressBar = (ProgressBar) findViewById(R.id.progress);
		// Setting listeners
		mPositiveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(TextUtils.isEmpty(mPortfolioNameEditText.getText())){
					ValidatorHelper.highlight(CreatePortfolioDialog.this, mPortfolioNameEditText, getString(R.string.fill_title_portfolio_field));
					return;
				}
				
				if(mCreatePortFolioAsyncTask!=null && !mCreatePortFolioAsyncTask.isCancelled()){
					mCreatePortFolioAsyncTask.cancel(true);
				}
				mCreatePortFolioAsyncTask = new CreatePortFolioTask();
				mCreatePortFolioAsyncTask.execute(mPortfolioNameEditText.getText().toString());
			}
		});
		mNegativeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(mCreatePortFolioAsyncTask!=null && !mCreatePortFolioAsyncTask.isCancelled()){
			mCreatePortFolioAsyncTask.cancel(true);
		}
	}
	
	private class CreatePortFolioTask extends SCAsyncTask<String, Void, Void> {

		public CreatePortFolioTask() {
			super(CreatePortfolioDialog.this, new AbstractAsyncTaskProcessor<String, Void>(CreatePortfolioDialog.this) {
				@Override
				public Void performAction(String... params) throws SecurityException, Exception {
					//check se gia' esiste
					exist = false;
					List<Portfolio> listPortolio = PMHelper.getPortfolioList();
					for (Portfolio portfolio: listPortolio){
						if (portfolio.name.compareTo(params[0].trim())==0)
							exist=true;
					}
					if (!exist)
						{
						PMHelper.createEmptyPortfolio(params[0].trim());
						}

					return null;
				}
				@Override
				public void handleResult(Void result) {
					if (exist)
						Toast.makeText(getApplicationContext(), getString(R.string.portfolio_already_exist), Toast.LENGTH_SHORT).show();
					else
						finish();
				}
			});
		}
	}

	/*
	private class CreatePortFolioAsyncTask extends AsyncTask<Void, Void, Integer>{
		
		private static final int OK = 0, NO_CONNECTION = 1, NOT_VALID_NAME = 2, SERVER_UNREACHABLE = 3;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mPositiveButton.setEnabled(false);
			mNegativeButton.setEnabled(false);
			mPortfolioNameEditText.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			String name = mPortfolioNameEditText.getText().toString();
			if(!TextUtils.isEmpty(name)){
				PMHelper.createEmptyPortfolio(name);
				return OK;	
			}else{
				return NOT_VALID_NAME;
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			// Checking result
			switch (result) {
			case OK:
				finish();
				return;
			case NO_CONNECTION:
				ToastBuilder.showShort(CreatePortfolioDialog.this, R.string.no_connection_available);					
				break;
			case NOT_VALID_NAME:
				if(TextUtils.isEmpty(mPortfolioNameEditText.getText())){
					ToastBuilder.showShort(CreatePortfolioDialog.this, R.string.fill_title_portfolio_field);					
				}else{
					ToastBuilder.showShort(CreatePortfolioDialog.this, R.string.not_valid_name);
				}
				ViewAnimator.animate(CreatePortfolioDialog.this, mPortfolioNameEditText, R.anim.shake);
				break;
			case SERVER_UNREACHABLE:
				ToastBuilder.showShort(CreatePortfolioDialog.this, R.string.no_server_available);					
				break;
			}
			mPositiveButton.setEnabled(true);
			mNegativeButton.setEnabled(true);
			mPortfolioNameEditText.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			String token = data.getExtras().getString(
					AccountManager.KEY_AUTHTOKEN);
			if (token == null) {
				PMHelper.endAppFailure(this, R.string.app_failure_security);
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
