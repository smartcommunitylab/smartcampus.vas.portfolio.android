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
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.portfolio.PMHelper;
import eu.trentorise.smartcampus.portfolio.R;
import eu.trentorise.smartcampus.portfolio.conf.AppConfigurations;
import eu.trentorise.smartcampus.portfolio.interfaces.SharedPortfolio;
import eu.trentorise.smartcampus.portfolio.models.ExamData;
import eu.trentorise.smartcampus.portfolio.models.Portfolio;
import eu.trentorise.smartcampus.portfolio.models.StudentExams;
import eu.trentorise.smartcampus.portfolio.utils.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.portfolio.utils.DateUtil;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * Fragment that contains all the elements related to student's unitn career
 * 
 * @author Simone Casagranda
 * 
 */
public class UnitnListFragment extends SherlockListFragment {

	private static final String PORTFOLIO = "PORTFOLIO";

	private AppConfigurations mAppConfigurations;

	// Other variables
	private StudentExamsAsyncTask mUPDataTask;

	private Portfolio mPortfolio;
	private SharedPortfolio mSharedPortfolio;

	private UPItemArrayAdapter mAdapter;
	private List<ExamItem> mElements = new ArrayList<ExamItem>();


	/**
	 * Use this method to prepare arguments that you have to pass to this
	 * Fragment
	 * 
	 * @param portfolio
	 * @return
	 */
	public static Bundle prepareArguments(Portfolio portfolio) {
		// Not null portfolio
		assert portfolio != null;
		// Preparing bundle
		Bundle b = new Bundle();
		b.putParcelable(PORTFOLIO, portfolio);
		return b;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof SharedPortfolio) {
			mSharedPortfolio = (SharedPortfolio) activity;
		} else {
			throw new RuntimeException("The container Activity has to be an instance of SharedPortfolio");
		}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Getting app preferences
		mAppConfigurations = new AppConfigurations(getActivity());
		// Retrieving title of portfolio
		Bundle args = getArguments();
		if (args != null && args.containsKey(PORTFOLIO)) {
			mPortfolio = args.getParcelable(PORTFOLIO);
		} else {
			throw new RuntimeException("You have to use prepareArguments method!");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflating layout
		View v = inflater.inflate(R.layout.listfrag_student_exam, null);
		// Retrieving UI references
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Preparing adapter
		prepareAdapter();
		// Setting adapter to list
		getListView().setAdapter(mAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		if (getActivity() instanceof SherlockFragmentActivity) {
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			// Setting visualized ActionBar title
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(R.string.unitn);
		}
		// Canceling any active task
		cancelAnyActiveTask();
		// Starting new task for student exams
		mUPDataTask = new StudentExamsAsyncTask();
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

	private void prepareAdapter() {
		mAdapter = new UPItemArrayAdapter(mElements);
	}

	private class StudentExamsAsyncTask extends SCAsyncTask<Void, Void, List<ExamItem>> {

		public StudentExamsAsyncTask() {
			super(getActivity(), new AbstractAsyncTaskProcessor<Void, List<ExamItem>>(getActivity()) {
				@Override
				public List<ExamItem> performAction(Void... params) throws SecurityException, Exception {
					List<ExamItem> items = new ArrayList<ExamItem>();
					List<ExamData> exams = null;
					//TODO
					StudentExams studentExams = null;
					if (mSharedPortfolio.isOwned()) 
						studentExams = PMHelper.getStudentExams(getActivity());
					else if (mSharedPortfolio.getContainer().getSharedStudentExams() != null && !mSharedPortfolio.getContainer().getSharedStudentExams().isEmpty())
						studentExams = mSharedPortfolio.getContainer().getSharedStudentExams().get(0);
					if (studentExams != null) {
						exams = studentExams.examData;
					}
					if (exams != null) {
						// Iterating over all exams (Do inside your controls)
						for (ExamData exam : exams) {
							ExamItem item = new ExamItem();
							item.data = exam;
							// Adding just created item
							items.add(item);
						}
					}
					return items;
				}

				@Override
				public void handleResult(List<ExamItem> result) {
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
	 * StudentExamsTask extends AsyncTask<Void, Void, List<ExamItem>>{
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * Showing progress bar showProgressBar(true); }
	 * 
	 * @Override protected List<ExamItem> doInBackground(Void... params) {
	 * List<ExamItem> items = new ArrayList<ExamItem>(); List<ExamData> exams =
	 * null; exams = PMHelper.getStudentExams(getActivity()).examData; if(exams
	 * != null){ // Iterating over all exams (Do inside your controls)
	 * for(ExamData exam : exams){ ExamItem item = new ExamItem(); item.data =
	 * exam; // Adding just created item items.add(item); } } return items; }
	 * 
	 * @Override protected void onPostExecute(List<ExamItem> result) {
	 * super.onPostExecute(result); // Hiding progress bar
	 * showProgressBar(false); // Clearing old list mElements.clear(); // Adding
	 * result if(result!=null){ mElements.addAll(result); } // Notifying adapter
	 * mAdapter.notifyDataSetChanged(); }
	 * 
	 * private void showProgressBar(boolean show){
	 * getSherlockActivity().setSupportProgressBarIndeterminateVisibility(show);
	 * }
	 * 
	 * }
	 */
	// Adapter used for Category elements
	private class UPItemArrayAdapter extends ArrayAdapter<ExamItem> {

		public class Holder {

			public static final int EXAM = 0, GRAPH = 1;

			TextView subject, date, cfu, mark;

			public void prepare(int code) {
				switch (code) {
				case EXAM:
					subject.setVisibility(View.VISIBLE);
					date.setVisibility(View.VISIBLE);
					cfu.setVisibility(View.VISIBLE);
					mark.setVisibility(View.VISIBLE);
					break;
				case GRAPH:
					subject.setVisibility(View.GONE);
					date.setVisibility(View.GONE);
					cfu.setVisibility(View.GONE);
					mark.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			}
		}

		public UPItemArrayAdapter(List<ExamItem> objects) {
			super(getActivity(), R.layout.item_mark, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_mark, null);
				// Create Holder
				holder = new Holder();
				// Find items in view
				holder.subject = (TextView) convertView.findViewById(R.id.subject);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.cfu = (TextView) convertView.findViewById(R.id.cfu);
				holder.mark = (TextView) convertView.findViewById(R.id.mark);
				// add Holder to View
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// Getting item
			final ExamItem item = getItem(position);
			if (item.data instanceof ExamData) {
				// Preparing holder
				holder.prepare(Holder.EXAM);
				// Retrieving default date format
				String pattern = mAppConfigurations.getDefaultDateFormat();
				// Getting exam
				ExamData exam = (ExamData) item.data;
				// Filling fields
				holder.subject.setText(exam.name);
				holder.date.setText(getString(R.string.date) + " " + DateUtil.format(pattern, exam.date));
				holder.cfu.setText(getString(R.string.credit_points) + ": " + exam.weight);
				holder.mark.setText(exam.lode ? exam.result + "L" : exam.result);
			}
			return convertView;
		}

	}

	// Wrapper for Exam or GraphInfo
	private class ExamItem {

		private Object data;

	}
}
