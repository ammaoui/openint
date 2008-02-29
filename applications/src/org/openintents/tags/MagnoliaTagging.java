/*
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openintents.tags;


import org.openintents.R;
import org.openintents.provider.Tag;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;
import org.openintents.lib.DeliciousApiHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ProgressBar;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;

public class  MagnoliaTagging extends Activity{


	public static final String _TAG="MagnoliaTagging";
	public static final String PREFS_NAME = "MagnoliaSettings";

	public static final String URI="URI";
	public static final String DESCRIPTION="DESC";
	public static final String TITLE="TITLE";


	public static final String MAGNOLIA_API="https://ma.gnolia.com/api/mirrord/v1";


	private static final int MENU_MAGNOLIASETTINGS=1001;

	private String mScreenName=new String();
	private String mPassWd=new String();
    private String mAPIKey=new String();


	protected static final String ALL = "ALL"; // TODO: Put string into resource

	private ListView mTags;
	private ListView mListContents;
	private Spinner mTagFilter;
	private String mFilter = null;

	private EditText mOutboundTags;
	private EditText mCURL;
	private EditText mDescription;
	private CheckBox mShared;
	private Button bInstance;
	private ProgressBar mProgress;


	protected void onCreate(Bundle icycle){
		super.onCreate(icycle);
       // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mScreenName= settings.getString("screenName", "");
        mPassWd= settings.getString("passWd", "");
	    mAPIKey= settings.getString("apiKey", "");
		//cant use if no values for account. start settings activity
		if ((mScreenName==null||mScreenName.equals(""))||(mPassWd==null||mPassWd.equals("")))
		{
			Log.w(_TAG,"Screenname or passwd could'nt be read from prefs, starting settings activity");
			Intent i=new Intent(this,org.openintents.tags.MagnoliaSettings.class);
			startActivity(i);
		}
		Bundle b=getIntent().getExtras();
		String myUri=b.getString(URI);
		String myDesc=b.getString(DESCRIPTION);
		//String myTitle=b.getString(TITLE);

		requestWindowFeature(android.view.Window.FEATURE_PROGRESS);
		//getWindow().setFeatureInt(android.view.Window.FEATURE_PROGRESS, android.view.Window.PROGRESS_INDETERMINATE_ON);


		//mProgress.setVisibility(ProgressBar.INVISIBLE);
		//mProgress.requestWindowFeature(android.view.Window.PROGRESS_INDETERMINATE_ON);
		setContentView(R.layout.magnoliatagging);

		mProgress=(ProgressBar)findViewById(R.id.magnoliatagging_progressbar);
		Log.d(_TAG,"PROFRESSBAR >>"+mProgress+"<<");

		mOutboundTags=(EditText) findViewById(R.id.magnoliatagging_outboundtags);
		mCURL=(EditText) findViewById(R.id.magnoliatagging_curi);
	
		if (myUri!=null && !myUri.equals(""))
		{
			mCURL.setText(myUri);
		}


		mDescription=(EditText) findViewById(R.id.magnoliatagging_description);

		if (myDesc!=null && ! myDesc.equals(""))
		{
			mDescription.setText(myDesc);
		}

		mTagFilter = (Spinner) findViewById(R.id.magnoliatagging_tagfilter);
		mTagFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				mFilter = ((TextView) v).getText().toString();
				if (ALL.equals(mFilter)) {
					mFilter = null;
				}
				fillDataTags();
			}

			public void onNothingSelected(AdapterView arg0) {
				mFilter = null;
				fillDataTags();
			}

		});
		//Log.d(_TAG,"findViewById return>>"+findViewById(R.id.magnoliatagging_tags)+"<<");
		mTags = (ListView) findViewById(R.id.magnoliatagging_tags);
		mTags.setOnItemClickListener(
			new OnItemClickListener() {
				
				public void onItemClick(AdapterView parent, 
						View v, int pos, long id) {
					Cursor c = (Cursor) parent.obtainItem(pos);
					addOutboundTag(c);
				}
				
		});

		bInstance=(Button)findViewById(R.id.magnoliatagging_cancel);
		bInstance.setOnClickListener(
			new OnClickListener(){
				public void onClick(View arg0) {
					MagnoliaTagging.this.setResult(Activity.RESULT_CANCELED);
					MagnoliaTagging.this.finish();							
				}		
			}
		);
		bInstance=(Button)findViewById(R.id.magnoliatagging_save);
		bInstance.setOnClickListener(
			new OnClickListener(){
				public void onClick(View arg0) {
					MagnoliaTagging.this.saveBookmark();
				}		
			}
		);
		//init cbx as false, private bookmarks as default.
		mShared=(CheckBox) findViewById(R.id.magnoliatagging_shared);
		if (mShared.isChecked())
		{
			mShared.setChecked(false);
		}
		fillDataTagFilter();
		fillDataTags();
		
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		boolean res=super.onCreateOptionsMenu(menu);
		
		menu.add(0,MENU_MAGNOLIASETTINGS,"Magnolia Settings");
		return res;
	}

	@Override
	public boolean onOptionsItemSelected(Item item){
		Log.v(_TAG,"onOptionsItemSelected: item.id>>"+item.getId()+"<<");
		int iID=item.getId();
		if (iID==MENU_MAGNOLIASETTINGS)
		{
			Intent i=new Intent(this,org.openintents.tags.MagnoliaSettings.class);
			startActivity(i);

		}
		return super.onOptionsItemSelected(item);
	}


	private void fillDataTagFilter() {
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Contents.CONTENT_URI,
				new String[] { Contents._ID, Contents.URI, Contents.TYPE },
				"type like 'TAG%'", null, Contents.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(_TAG, "missing tag provider");
			mTagFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		ArrayList<String> list = new ArrayList<String>();
		list.add(ALL);
		while (c.next()) {
			list.add(c.getString(1));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		mTagFilter.setAdapter(adapter);

	}


	private void fillDataTags() {
		Log.d(_TAG,"fillDataTags: entering");
		String filter = null;
		String[] filterArray = null;
		
		if (mFilter != null) {
			filter = "uri_1 = ?";
			filterArray = new String[] { mFilter };
		}
		
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(
				Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.TAG_ID, Tags.CONTENT_ID,
						Tags.URI_1, Tags.URI_2 }, filter, filterArray,
				Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		Log.d(_TAG,"# of cursoritems>>"+c.count()+"<<");
		if (c == null) {
			Log.e(_TAG, "missing tag provider");
			mTags.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		ListAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.tag_row,
				// Give the cursor to the list adapter
				c,
				// Map the TAG / CONTENT columns in the database to...
				new String[] { Tags.URI_1},
				// The "text1" view defined in the XML template
				new int[] {R.id.tag_uri_1}
				);
		Log.d(_TAG,"Adapater is >>"+adapter.toString()+"<<");

		mTags.setAdapter(adapter);

	}

	private void addOutboundTag(Cursor c){

		String otags=mOutboundTags.getText().toString();

		String s=c.getString(c.getColumnIndex(Tags.URI_1));
		otags+=s+",";
		mOutboundTags.setText(otags);

		
	}

	public void saveBookmark(){
		Log.d(_TAG,"saveBookmark:entering");
		boolean remoteResult=false;
		String bookmarkURL=mCURL.getText().toString();
		String strOtags=mOutboundTags.getText().toString();
		String description=mDescription.getText().toString();
		boolean shared=mShared.isChecked();
	
		String[] oTags=strOtags.split(",");
		Log.d(_TAG,"tag array>>"+oTags.toString()+"<<");

		//
		if (!bookmarkURL.startsWith("http://"))
		{
			Log.d(_TAG,"bookmarUrl didnt have http header, corrected that");
			bookmarkURL="http://"+bookmarkURL;
		}

		DeliciousApiHelper dah= new DeliciousApiHelper(
			DeliciousApiHelper.MAGNOLIA_API,
			mScreenName,
			mPassWd			
		);
		

		mProgress.setIndeterminate(true);
		mProgress.setVisibility(ProgressBar.VISIBLE);
		
		//getWindow().setFeatureInt(android.view.Window.FEATURE_PROGRESS,10);
		try
		{
			remoteResult=dah.addPost(
				bookmarkURL,
				description,
				null,
				oTags,
				shared
				);
			
/*
			//debug
			remoteResult=true;
			if (false)
			{
				throw new java.io.IOException();
			}
			try {
			// 1 Sekunde = 1000 ms warten!
			Thread.sleep(6000);
			} catch (InterruptedException e) {
			} 
			*/
		}
		catch (java.io.IOException ioe)
		{
			Log.e(_TAG,"IOE while posting to Magnolia>>"+ioe.getMessage()+"<<");
		}
		
		//only add local tags if rpc was successfull
		//so user has a chance of retrying without local dublettes
		if (remoteResult)
		{
			int otl=oTags.length ;
			for (int i=0;i<otl;i++ )
			{
				insertTag(oTags[i],bookmarkURL);
			}
			setResult(Activity.RESULT_OK);
			finish();	
		}

//		getWindow().setFeatureInt(android.view.Window.FEATURE_PROGRESS, android.view.Window.PROGRESS_INDETERMINATE_OFF);
		
		mProgress.setIndeterminate(false);
		mProgress.setVisibility(ProgressBar.GONE);
		
		
		Log.d(_TAG,"saveBookmark:leaving");
		
	}

	protected void insertTag(String tag, String content) {
		android.content.ContentValues values = new android.content.ContentValues(2);
		values.put(Tags.URI_1, tag);
		values.put(Tags.URI_2, content);

		try {
			getContentResolver().insert(Tags.CONTENT_URI, values);
		} catch (Exception e) {
			Log.i(_TAG, "insert failed", e);
			return;
		}

	}

	protected void onResume(){
		super.onResume();
	}


	protected void onStop(){
		super.onStop();

	}

}/*eoc*/
