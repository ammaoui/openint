/*
 * Copyright (C) 2010 Karl Ostmo
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

package org.openintents.calendarpicker.activity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.adapter.EventListAdapter;
import org.openintents.calendarpicker.adapter.EventListAdapter.ExtraQuantityInfo;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;


public abstract class AbstractEventsListActivity extends ListActivity {

	static final String TAG = "AbstractEventsListActivity"; 

	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_EVENT_TIMESTAMP = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TIMESTAMP;
	public static final String KEY_EVENT_TITLE = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TITLE;

	// ========================================================================
    abstract Cursor requery();

	// ========================================================================
    abstract DateFormat getDateFormat();
    
    // ========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.list_activity_event_list);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        // Initialize sort bucket
        for (SortCriteria x : SortCriteria.values()) sorting_order.add(x);

    	
    	Cursor cursor = requery();
    	
    	ExtraQuantityInfo[] extra_quantity_info = new ExtraQuantityInfo[CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length];
    	for (int i=0; i<CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length; i++) {
    		String extra_name = CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES[i];
    		if (getIntent().hasExtra(extra_name)) {
    			
    			String column_name = getIntent().getStringExtra(extra_name);
    			extra_quantity_info[i] = new ExtraQuantityInfo(column_name);

        		if (getIntent().hasExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_FORMATS[i]))
        			extra_quantity_info[i].format_string = getIntent().getStringExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_FORMATS[i]);
    		}
    	}
    	
        setListAdapter(new EventListAdapter(
        		this,
        		R.layout.list_item_event,
        		cursor, getDateFormat(), extra_quantity_info));

        getListView().setOnItemClickListener(category_choice_listener);

//    	registerForContextMenu( category_listview );
    	

		if (savedInstanceState != null) {
//			autocomplete_textview.setText( savedInstanceState.getString("search_text") );
		}
		
		
        final StateRetainer a = (StateRetainer) getLastNonConfigurationInstance();
        if (a != null) {
        	sorting_order = a.sorting_order;
        } else {
        	
        }
    }

    // ========================================================================
    OnItemClickListener category_choice_listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> adapter_view, View arg1, int position, long id) {
			
			if (!Intent.ACTION_PICK.equals(getIntent().getAction()))
				return;
			
			Cursor cursor = (Cursor) ((CursorAdapter) adapter_view.getAdapter()).getItem(position);

			Intent i = new Intent();
			i.putExtra(BaseColumns._ID, id);

			int epoch_column = cursor.getColumnIndex(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TIMESTAMP);
			long event_epoch = cursor.getLong(epoch_column);
			i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_EPOCH, event_epoch);
			i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_DATETIME, MonthActivity.HYPEHENATED_ISO_DATE_FORMATTER.format(new Date(event_epoch)));
			
	        setResult(Activity.RESULT_OK, i);
			finish();
		}
    };    

    // ========================================================================
    class StateRetainer {
    	Cursor cursor;
    	Stack<SortCriteria> sorting_order;
    }

    // ========================================================================
    @Override
    public Object onRetainNonConfigurationInstance() {
    	
    	StateRetainer state = new StateRetainer();
    	state.cursor = ((CursorAdapter) getListAdapter()).getCursor();
    	state.sorting_order = sorting_order;
        return state;
    }

    // ========================================================================
    @Override
    protected void onSaveInstanceState(Bundle out_bundle) {
    	Log.i(TAG, "onSaveInstanceState");

    }

    // ========================================================================
    @Override
    protected void onRestoreInstanceState(Bundle in_bundle) {
    	Log.i(TAG, "onRestoreInstanceState");
    	
    }

    // ========================================================================
    @Override
    protected Dialog onCreateDialog(int id) {
    	
        switch (id) {
 
        }
        
        return null;
    }

    // ========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_event_list, menu);

        return true;
    }

    // ========================================================================
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    // ========================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_sort_alpha:
        {
        	sortList(SortCriteria.ALPHA);
            return true;
        }
        case R.id.menu_sort_recent:
        {
        	sortList(SortCriteria.DATE);
            return true;
        }
        /*
        case R.id.menu_sort_usage:
        {
        	sortList(SortCriteria.FREQUENCY);
            return true;
        }
        */
        }

        return super.onOptionsItemSelected(item);
    }

    // ========================================================================
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_CANCELED) {
	  	   	switch (requestCode) {

	   		default:
		    	break;
		   }
		}
    }
    

    // ========================================================================
    // NOTE: The criteria are read from right-to-left in the queue; Highest priority is
    // on top of the stack.
    Stack<SortCriteria> sorting_order = new Stack<SortCriteria>();
    enum SortCriteria {
    	ALPHA, DATE
    }
    String[] sort_column_names = {
		KEY_EVENT_TITLE,
		KEY_EVENT_TIMESTAMP
	};
    boolean[] default_ascending = {true, true};
    
    String constructOrderByString() {
    	List<String> sort_pieces = new ArrayList<String>();
    	for (int i=0; i<sorting_order.size(); i++) {
    		int sort_col = sorting_order.get(i).ordinal();
    		sort_pieces.add( sort_column_names[sort_col] + " " + (default_ascending[sort_col] ? "ASC" : "DESC") );
    	}
    	Collections.reverse(sort_pieces);
    	return TextUtils.join(", ", sort_pieces);
    }
    
    
    void sortList(SortCriteria criteria) {
    	sorting_order.remove(criteria);
    	sorting_order.push(criteria);
    	
    	((ResourceCursorAdapter) getListAdapter()).changeCursor( requery() );		
    }
}

