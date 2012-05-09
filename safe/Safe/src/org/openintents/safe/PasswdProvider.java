/* $Id$
 * 
 * Copyright (C) 2009 OpenIntents.org
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
package org.openintents.safe;

import java.util.Arrays;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class PasswdProvider extends ContentProvider {

	private static final boolean debug = true;
	private static final String TAG = "PasswdProvider";
	
    /**
     * The database that the provider uses as its underlying data store
     */
    private static final String DATABASE_NAME = "safe";

    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 4;

	private static HashMap<String, String> sPasswordsProjectionMap;

	private static final int PASSWORDS = 1;
	private static final int PASSWORD_ID = 2;

	private static final UriMatcher sUriMatcher;

	private DatabaseHelper mOpenHelper;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Passwd.AUTHORITY, "passwords", PASSWORDS);
		sUriMatcher.addURI(Passwd.AUTHORITY, "passwords/#", PASSWORD_ID);
		
		sPasswordsProjectionMap = new HashMap<String, String>();
		sPasswordsProjectionMap.put(Passwd.Passwds.COLUMN_NAME_ID, Passwd.Passwds.COLUMN_NAME_ID);
		sPasswordsProjectionMap.put(Passwd.Passwds.COLUMN_NAME_DESCRIPTION, Passwd.Passwds.COLUMN_NAME_DESCRIPTION);
		sPasswordsProjectionMap.put(Passwd.Passwds.COLUMN_NAME_PASSWORD, Passwd.Passwds.COLUMN_NAME_PASSWORD);
		sPasswordsProjectionMap.put(
				Passwd.Passwds.COLUMN_NAME_MODIFICATION_DATE,
				Passwd.Passwds.COLUMN_NAME_MODIFICATION_DATE);

	}

	static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		* Creates the underlying database with table name and column names taken from the
		* NotePad class.
		*/
		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
	           db.execSQL("CREATE TABLE " + Passwd.Passwds.TABLE_NAME + " ("
	                   + Passwd.Passwds._ID + " INTEGER PRIMARY KEY,"
	                   + Passwd.Passwds.COLUMN_NAME_TITLE + " TEXT,"
	                   + Passwd.Passwds.COLUMN_NAME_NOTE + " TEXT,"
	                   + Passwd.Passwds.COLUMN_NAME_CREATE_DATE + " INTEGER,"
	                   + Passwd.Passwds.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
	                   + ");");
	           */
	       }

	       /**
	        */
	       @Override
	       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	           // Logs that the database is being upgraded
	           Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                   + newVersion + ", which will destroy all old data");

	           // Kills the table and existing data
	           db.execSQL("DROP TABLE IF EXISTS notes");

	           // Recreates the database with a new version
	           onCreate(db);
	       }
	   }

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String s, String[] as) {
		// not supported
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// return file extension (uri.lastIndexOf("."))
		return null; //mMimeTypes.getMimeType(uri.toString());
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		// not supported
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Passwd.Passwds.TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case PASSWORDS:
			if (debug) Log.d(TAG, "got whole table");
			if (debug) Log.d(TAG, "using ProjectionMap: "+sPasswordsProjectionMap);
			qb.setProjectionMap(sPasswordsProjectionMap);
			break;

		case PASSWORD_ID:
			if (debug) Log.d(TAG, "got specific id");
            qb.setProjectionMap(sPasswordsProjectionMap);
            qb.appendWhere(
                Passwd.Passwds._ID +    // the name of the ID column
                "=" +
                // the position of the note ID itself in the incoming URI
                uri.getPathSegments().get(Passwd.Passwds.PASSWORD_ID_PATH_POSITION));
            break;

		default:
//			return null;
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		String orderBy;
		// If no sort order is specified, uses the default
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Passwd.Passwds.DEFAULT_SORT_ORDER;
		} else {
			// otherwise, uses the incoming sort order
			orderBy = sortOrder;
		}

		// Opens the database object in "read" mode, since no writes need to be done.
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		if (debug) Log.d(TAG, "about to query: db file="+db.getPath());
		if (debug) Log.d(TAG, "about to query: db="+db);
		if (debug) Log.d(TAG, "about to query: projection="+Arrays.toString(projection));
		if (debug) Log.d(TAG, "about to query: selection="+selection);
		if (debug) Log.d(TAG, "about to query: selectionArgs="+Arrays.toString(selectionArgs));
		if (debug) Log.d(TAG, "about to query: tables="+qb.getTables());
		Cursor c = qb.query(
				db,				// The database to query
				projection,		// The columns to return from the query
				selection,		// The columns for the where clause
				selectionArgs,	// The values for the where clause
				null,			// don't group the rows
				null,			// don't filter by row groups
				orderBy			// The sort order
		);
	
		// Tells the Cursor what URI to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {
		// not supported
		return 0;
	}

}
