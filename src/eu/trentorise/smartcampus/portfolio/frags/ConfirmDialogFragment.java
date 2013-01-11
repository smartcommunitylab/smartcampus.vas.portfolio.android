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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.portfolio.interfaces.NoteLayerInteractor;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;



/**
 * Dialog fragment that allows you to ask to user confirmations.
 * 
 * @author Simone Casagranda
 *
 */
public abstract class ConfirmDialogFragment extends SherlockDialogFragment {
	
	private static final String TAG_DIALOG_CONFIRM = "TAG_DIALOG_CONFIRM";
	
	private static final String TITLE = "TITLE";
	private static final String MESSAGE = "MESSAGE";
	private static final String POSITIVE = "POSITIVE";
	private static final String NEGATIVE = "NEGATIVE";
	
	private NoteLayerInteractor mNoteLayerInteractor;
	
	/**
	 * Grants you the possibility to prepare a bundle that you'll pass to fragment instance
	 * @param title
	 * @param message
	 * @param positive
	 * @param negative
	 * @return
	 */
	public static Bundle prepareArguments(int title, int message, int positive, int negative){
		// Preparing arguments
		Bundle b = new Bundle();
		b.putInt(TITLE, title);
		b.putInt(MESSAGE, message);
		b.putInt(POSITIVE, positive);
		b.putInt(NEGATIVE, negative);
		return b;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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
	public void onResume() {
		super.onResume();
		// Hiding note layer
		mNoteLayerInteractor.setVisibility(false);
	}

	/**
	 * Allows you to show a passed ConfirmDialogFragment
	 * @param activity
	 * @param confirmDialogFragment
	 * @param args
	 */
	public static void show(SherlockFragmentActivity activity, ConfirmDialogFragment confirmDialogFragment, Bundle args){
		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_CONFIRM);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
		// Preparing arguments
        if(args!=null){
    		confirmDialogFragment.setArguments(args);
        }
		// Showing dialog
        confirmDialogFragment.show(ft, TAG_DIALOG_CONFIRM);
		
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Setting and filling components
		builder.setTitle(getArguments().getInt(TITLE));
		builder.setMessage(getArguments().getInt(MESSAGE));
		builder.setCancelable(false);
		builder.setPositiveButton(getArguments().getInt(POSITIVE), new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Calling on click that you have to implement
				ConfirmDialogFragment.this.onClick(true);				
			}
		});
		builder.setNegativeButton(getArguments().getInt(NEGATIVE), new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Calling on click that you have to implement
				ConfirmDialogFragment.this.onClick(false);
			}
		});
		// Creating dialog
		return builder.create();
	}
	
	/**
	 * Manage the click done by user
	 */
	public abstract void onClick(boolean positive);
	
}
