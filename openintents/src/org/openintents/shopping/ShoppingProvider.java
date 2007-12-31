/* 
 * Copyright (C) 2007 OpenIntents.org
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

package org.openintents.shopping;

import java.util.HashMap;

import org.openintents.R;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Items;
import org.openintents.provider.Shopping.Lists;
import org.openintents.provider.Shopping.Status;

import android.content.ContentProvider;
import android.content.ContentProviderDatabaseHelper;
import android.content.ContentURIParser;
import android.content.ContentValues;
import android.content.QueryBuilder;
import android.content.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ContentURI;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of shopping items and shopping lists. 
 * 
 */
public class ShoppingProvider extends ContentProvider {

	private SQLiteDatabase mDB;

	private static final String TAG = "ShoppingProvider";
	private static final String DATABASE_NAME = "shopping.db";
	private static final int DATABASE_VERSION = 1; // Release 0.1.1

	private static HashMap<String, String> ITEMS_PROJECTION_MAP;
	private static HashMap<String, String> LISTS_PROJECTION_MAP;
	private static HashMap<String, String> CONTAINS_PROJECTION_MAP;
	private static HashMap<String, String> CONTAINS_FULL_PROJECTION_MAP;

	// Basic tables
	private static final int ITEMS = 1;
	private static final int ITEM_ID = 2;
	private static final int LISTS = 3;
	private static final int LIST_ID = 4;
	private static final int CONTAINS = 5;
	private static final int CONTAINS_ID = 6;
	
	// Derived tables
	private static final int CONTAINS_FULL = 101; // combined with items and lists
	private static final int CONTAINS_FULL_ID = 102;
	
	private static final ContentURIParser URL_MATCHER;

	/**
	 * ShoppingProvider maintains the following tables:
	 *  * items: items you want to buy
	 *  * lists: shopping lists ("My shopping list", "Bob's shopping list")
	 *  * contains: which item/list/(recipe) is contained 
	 *              in which shopping list.
	 */
	private static class DatabaseHelper extends ContentProviderDatabaseHelper {
		/**
		 * Creates tables "items", "lists", and "contains".
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE items ("
					+ "_id INTEGER PRIMARY KEY," // Database Version 1
					+ "name VARCHAR," // V1
					+ "image VARCHAR," // V1
					+ "created INTEGER," // V1
					+ "modified INTEGER," // V1
					+ "accessed INTEGER" // V1
					+ ");");
			db.execSQL("CREATE TABLE lists ("
					+ "_id INTEGER PRIMARY KEY," // Database Version 1
					+ "name VARCHAR," // V1
					+ "image VARCHAR," // V1
					+ "created INTEGER," // V1
					+ "modified INTEGER," // V1
					+ "accessed INTEGER" // V1
					+ ");");
			db.execSQL("CREATE TABLE contains ("
					+ "_id INTEGER PRIMARY KEY," // Database Version 1
					+ "item_id INTEGER," // V1
					+ "list_id INTEGER," // V1
					+ "quantity VARCHAR," // V1
					+ "status INTEGER," // V1
					+ "created INTEGER," // V1
					+ "modified INTEGER," // V1
					+ "accessed INTEGER" // V1
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS items");
			db.execSQL("DROP TABLE IF EXISTS lists");
			db.execSQL("DROP TABLE IF EXISTS contains");
			onCreate(db);
		}
	}

	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		return mDB != null;
	}

	@Override
	public Cursor query(ContentURI url, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sort) {
		QueryBuilder qb = new QueryBuilder();

		Log.i(TAG, "Query for URL: " + url);
		
		String defaultOrderBy = null;
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			qb.setTables("items");
			qb.setProjectionMap(ITEMS_PROJECTION_MAP);
			defaultOrderBy = Items.DEFAULT_SORT_ORDER;
			break;

		case ITEM_ID:
			qb.setTables("items");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;

		case LISTS:
			qb.setTables("lists");
			qb.setProjectionMap(LISTS_PROJECTION_MAP);
			defaultOrderBy = Lists.DEFAULT_SORT_ORDER;
			break;
			
		case LIST_ID:
			qb.setTables("lists");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;
			
		case CONTAINS:
			qb.setTables("contains");
			qb.setProjectionMap(CONTAINS_PROJECTION_MAP);
			defaultOrderBy = Contains.DEFAULT_SORT_ORDER;
			break;

		case CONTAINS_ID:
			qb.setTables("contains");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;
		
		case CONTAINS_FULL:
			qb.setTables("contains, items, lists");
			qb.setProjectionMap(CONTAINS_FULL_PROJECTION_MAP);
			qb.appendWhere("contains.item_id = items._id AND contains.list_id = lists._id");
			defaultOrderBy = ContainsFull.DEFAULT_SORT_ORDER;
			break;

		case CONTAINS_FULL_ID:
			qb.setTables("contains, items, lists");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			qb.appendWhere("contains.item_id = items._id AND contains.list_id = lists._id");
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		// If no sort order is specified use the default

		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = defaultOrderBy;
		} else {
			orderBy = sort;
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, groupBy,
				having, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
	public ContentURI insert(ContentURI url, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		// insert is supported for items or lists
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			return insertItem(url, values);
			
		case LISTS:
			return insertList(url, values);
			
		case CONTAINS:
			return insertContains(url, values);
			
		case CONTAINS_FULL:
			throw new IllegalArgumentException("Insert not supported for " + url
					+ ", use CONTAINS instead of CONTAINS_FULL.");
			
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}
	
	public ContentURI insertItem(ContentURI url, ContentValues values) {
		long rowID;
		
		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();
		

		// Make sure that the fields are all set
		if (!values.containsKey(Items.NAME)) {
			values.put(Items.NAME, r.getString(R.string.new_item));
		}
		
		if (!values.containsKey(Items.IMAGE)) {
			values.put(Items.IMAGE, "");
		}
		
		if (!values.containsKey(Items.CREATED_DATE)) {
			values.put(Items.CREATED_DATE, now);
		}

		if (!values.containsKey(Items.MODIFIED_DATE)) {
			values.put(Items.MODIFIED_DATE, now);
		}

		if (!values.containsKey(Items.ACCESSED_DATE)) {
			values.put(Items.ACCESSED_DATE, now);
		}
		
		// TODO: Here we should check, whether item exists already. 
		// (see TagsProvider)
		// insert the item. 
		rowID = mDB.insert("items", "items", values);
		if (rowID > 0) {
			ContentURI uri = Items.CONTENT_URI.addId(rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		// If everything works, we should not reach the following line:
		throw new SQLException("Failed to insert row into " + url);
	}

	public ContentURI insertList(ContentURI url, ContentValues values) {
		long rowID;
		
		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();
		

		// Make sure that the fields are all set
		if (!values.containsKey(Lists.NAME)) {
			values.put(Lists.NAME, r.getString(R.string.new_list));
		}
		
		if (!values.containsKey(Lists.IMAGE)) {
			values.put(Lists.IMAGE, "");
		}
		
		if (!values.containsKey(Lists.CREATED_DATE)) {
			values.put(Lists.CREATED_DATE, now);
		}

		if (!values.containsKey(Lists.MODIFIED_DATE)) {
			values.put(Lists.MODIFIED_DATE, now);
		}

		if (!values.containsKey(Lists.ACCESSED_DATE)) {
			values.put(Lists.ACCESSED_DATE, now);
		}
		
		// TODO: Here we should check, whether item exists already.
		// (see TagsProvider)
		
		// insert the tag. 
		rowID = mDB.insert("lists", "lists", values);
		if (rowID > 0) {
			ContentURI uri = Items.CONTENT_URI.addId(rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		// If everything works, we should not reach the following line:
		throw new SQLException("Failed to insert row into " + url);
		
	}
	
	public ContentURI insertContains(ContentURI url, ContentValues values) {		
		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();

		// Make sure that the fields are all set
		if (!(values.containsKey(Contains.ITEM_ID)
				&& values.containsKey(Contains.LIST_ID))) {
			// At least these values should exist.
			throw new SQLException("Failed to insert row into " + url 
					+ ": ITEM_ID and LIST_ID must be given.");
		}
		
		// TODO: Check here that ITEM_ID and LIST_ID
		//       actually exist in the tables. 
		
		if (!values.containsKey(Contains.QUANTITY)) {
			values.put(Contains.QUANTITY, "");
		}
		
		if (!values.containsKey(Contains.STATUS)) {
			values.put(Contains.STATUS, Status.WANT_TO_BUY);
		} else {
			// Check here that STATUS is valid.
			long s = values.getAsInteger(Contains.STATUS);
			
			if (!Status.isValid(s)) {
				throw new SQLException("Failed to insert row into " + url
						+ ": Status " + s + " is not valid.");
			}
		}
		
		if (!values.containsKey(Items.CREATED_DATE)) {
			values.put(Items.CREATED_DATE, now);
		}

		if (!values.containsKey(Items.MODIFIED_DATE)) {
			values.put(Items.MODIFIED_DATE, now);
		}

		if (!values.containsKey(Items.ACCESSED_DATE)) {
			values.put(Items.ACCESSED_DATE, now);
		}
		
		// TODO: Here we should check, whether item exists already. 
		// (see TagsProvider)
		
		// insert the item. 
		long rowId = mDB.insert("contains", "contains", values);
		if (rowId > 0) {
			ContentURI uri = Contains.CONTENT_URI.addId(rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		// If everything works, we should not reach the following line:
		throw new SQLException("Failed to insert row into " + url);
	}

	@Override
	public int delete(ContentURI url, String where, String[] whereArgs) {
		int count;
		//long rowId;
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			count = mDB.delete("items", where, whereArgs);
			break;

		case ITEM_ID:
			String segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("items", "_id=" + segment + whereString, whereArgs);
			break;

		case LISTS:
			count = mDB.delete("lists", where, whereArgs);
			break;

		case LIST_ID:
			segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("lists", "_id=" + segment + whereString, whereArgs);
			break;

		case CONTAINS:
			count = mDB.delete("contains", where, whereArgs);
			break;

		case CONTAINS_ID:
			segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("contains", "_id=" + segment + whereString, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public int update(ContentURI url, ContentValues values, String where,
			String[] whereArgs) {
		Log.i(TAG, "update called for: " + url);
		int count;
		//long rowId;
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			count = mDB.update("items", values, where, whereArgs);
			break;

		case ITEM_ID:
			String segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.update("items", values, 
							"_id=" + segment + whereString, whereArgs);
			break;

		case LISTS:
			count = mDB.update("lists", values, where, whereArgs);
			break;

		case LIST_ID:
			segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.update("lists", values, "_id=" + segment + whereString, whereArgs);
			break;

		case CONTAINS:
			count = mDB.update("contains", values, where, whereArgs);
			break;

		case CONTAINS_ID:
			segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.update("contains", values, "_id=" + segment + whereString, whereArgs);
			break;

		default:
			Log.e(TAG, "Update received unknown URL: " + url);
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public String getType(ContentURI url) {
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			return "vnd.openintents.cursor.dir/shopping.item";

		case ITEM_ID:
			return "vnd.openintents.cursor.item/shopping.item";

		case LISTS:
			return "vnd.openintents.cursor.dir/shopping.list";

		case LIST_ID:
			return "vnd.openintents.cursor.item/shopping.list";

		case CONTAINS:
			return "vnd.openintents.cursor.dir/shopping.contains";

		case CONTAINS_ID:
			return "vnd.openintents.cursor.item/shopping.contains";

		case CONTAINS_FULL:
			return "vnd.openintents.cursor.dir/shopping.containsfull";

		case CONTAINS_FULL_ID:
			return "vnd.openintents.cursor.item/shopping.containsfull";

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	static {
		URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.shopping", "items", ITEMS);
		URL_MATCHER.addURI("org.openintents.shopping", "items/#", ITEM_ID);
		URL_MATCHER.addURI("org.openintents.shopping", "lists", LISTS);
		URL_MATCHER.addURI("org.openintents.shopping", "lists/#", LIST_ID);
		URL_MATCHER.addURI(
				"org.openintents.shopping", "contains", CONTAINS);
		URL_MATCHER.addURI(
				"org.openintents.shopping", "contains/#", CONTAINS_ID);
		URL_MATCHER.addURI(
				"org.openintents.shopping", "containsfull", CONTAINS_FULL);
		URL_MATCHER.addURI(
				"org.openintents.shopping", "containsfull/#", CONTAINS_FULL_ID);

		ITEMS_PROJECTION_MAP = new HashMap<String, String>();
		ITEMS_PROJECTION_MAP.put(Items._ID, "items._id");
		ITEMS_PROJECTION_MAP.put(Items.NAME, "items.name");
		ITEMS_PROJECTION_MAP.put(Items.IMAGE, "items.image");
		ITEMS_PROJECTION_MAP.put(Items.CREATED_DATE, "items.created");
		ITEMS_PROJECTION_MAP.put(Items.MODIFIED_DATE, "items.modified");
		ITEMS_PROJECTION_MAP.put(Items.ACCESSED_DATE, "items.accessed");
		
		LISTS_PROJECTION_MAP = new HashMap<String, String>();
		LISTS_PROJECTION_MAP.put(Lists._ID, "lists._id");
		LISTS_PROJECTION_MAP.put(Lists.NAME, "lists.name");
		LISTS_PROJECTION_MAP.put(Lists.IMAGE, "lists.image");
		LISTS_PROJECTION_MAP.put(Lists.CREATED_DATE, "lists.created");
		LISTS_PROJECTION_MAP.put(Lists.MODIFIED_DATE, "lists.modified");
		LISTS_PROJECTION_MAP.put(Lists.ACCESSED_DATE, "lists.accessed");
		
		CONTAINS_PROJECTION_MAP = new HashMap<String, String>();
		CONTAINS_PROJECTION_MAP.put(Contains._ID, "contains._id");
		CONTAINS_PROJECTION_MAP.put(Contains.ITEM_ID, "contains.item_id");
		CONTAINS_PROJECTION_MAP.put(Contains.LIST_ID, "contains.list_id");
		CONTAINS_PROJECTION_MAP.put(Contains.QUANTITY, "contains.quantity");
		CONTAINS_PROJECTION_MAP.put(Contains.STATUS, "contains.status");
		CONTAINS_PROJECTION_MAP.put(Contains.CREATED_DATE, "contains.created");
		CONTAINS_PROJECTION_MAP.put(
				Contains.MODIFIED_DATE, "contains.modified");
		CONTAINS_PROJECTION_MAP.put(
				Contains.ACCESSED_DATE, "contains.accessed");
		
		CONTAINS_FULL_PROJECTION_MAP = new HashMap<String, String>();
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull._ID, "contains._id");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.ITEM_ID, "contains.item_id");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.LIST_ID, "contains.list_id");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.QUANTITY, "contains.quantity");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.STATUS, "contains.status");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.CREATED_DATE, "contains.created");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.MODIFIED_DATE, "contains.modified");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.ACCESSED_DATE, "contains.accessed");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.ITEM_NAME, "items.name as item_name");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.ITEM_IMAGE, "items.image as item_image");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.LIST_NAME, "lists.name as list_name");
		CONTAINS_FULL_PROJECTION_MAP.put(
				ContainsFull.LIST_IMAGE, "lists.image as list_image");
		
	}
}
