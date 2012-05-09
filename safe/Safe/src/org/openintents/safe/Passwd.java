/* 
 * Copyright (C) 2012 OpenIntents.org
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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 */
public final class Passwd {
	public static final String AUTHORITY = "org.openintents.safe.passwd";

	// This class cannot be instantiated
	private Passwd() {}

	/**
	 * Notes table
	 */
	public static final class Passwds implements BaseColumns {
		// This class cannot be instantiated
		private Passwds() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "passwords";

        /*
         * URI definitions
         */

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Path parts for the URIs
         */

        /**
         * Path part for the Passwords URI
         */
        private static final String PATH_PASSWORDS = "/passwords";

        /**
         * Path part for the Password ID URI
         */
        private static final String PATH_PASSWORD_ID = "/password/";

        /**
         * 0-relative position of a password ID segment in the path part of a note ID URI
         */
        public static final int PASSWORD_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_PASSWORDS);

        /**
         * The content URI base for a single note. Callers must
         * append a numeric note id to this Uri to retrieve a note
         */
        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_PASSWORD_ID);

        /**
         * The content URI match pattern for a single note, specified by its ID. Use this to match
         * incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN
            = Uri.parse(SCHEME + AUTHORITY + PATH_PASSWORD_ID + "/#");

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openintents.safe.password";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openintents.safe.password";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "lastdatetimeedit DESC";

        /*
         * Column definitions
         */

        public static final String COLUMN_NAME_ID = "id";

        /**
         * Column name for the title of the note
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_DESCRIPTION = "description";

        /**
         * Column name of the note content
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_PASSWORD = "password";

        /**
         * Column name for the modification timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "lastdatetimeedit";
	}
}
