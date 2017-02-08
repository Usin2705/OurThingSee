package metro.ourthingsee.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import metro.ourthingsee.data.TCContract.TCEntry;
/**
 * Created by Nhan Phan on 08-Feb-17.
 */

public class TCProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = TCProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the thingsee table
     */
    private static final int URI_TC_TABLE = 100;

    /**
     * URI matcher code for the content URI for a single thingsee data in the thingsee table
     */
    private static final int URI_TC_ITEM = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form
        // "content://metro.ourthingsee/thingsee" will map to the
        // integer code {@link #URI_TC_TABLE}. This URI is used to provide access to MULTIPLE rows
        // of the thingsee table.
        sUriMatcher.addURI(TCContract.CONTENT_AUTHORITY, TCContract.PATH_THINGSEE, URI_TC_TABLE);

        // The content URI of the form
        // "content://metro.ourthingsee/thingsee/#" will map to the
        // integer code {@link #URI_TC_ITEM}. This URI is used to provide access to ONE single row
        // of the thingsee table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://metro.ourthingsee/thingsee/3" matches, but
        // "content://metro.ourthingsee/thingsee/"
        // (without a number at the end) doesn't match.
        sUriMatcher.addURI(TCContract.CONTENT_AUTHORITY,
                TCContract.PATH_THINGSEE + "/#", URI_TC_ITEM);
    }

    /**
     * Database helper object
     */
    private TCDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new TCDbHelper(getContext());
        return true;
    }


    // Query also needed for whenever you need to access database, for example when
    // CursorAdapter update the listview. Also will update the Cursor Adapter whenever
    // data changed.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_TC_TABLE:
                // For the URI_TC_TABLE code, query the inventories table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the thingsee table.
                cursor = database.query(TCEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case URI_TC_ITEM:
                // For the URI_TC_ITEM code, extract out the ID from the URI.
                // For an example URI such as
                // "content://metro.ourthingsee/thingsee/3",
                // the selection will be "_id=?" and the selection argument will be a String array
                // containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = TCEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the thingssee table where the _id equals 3
                // to return a Cursor containing that row of the table.
                cursor = database.query(TCEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        // Need to call this, so all insert, update and delete action will be updated in the
        // CursorAdapter
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_TC_TABLE:
                return TCEntry.CONTENT_LIST_TYPE;
            case URI_TC_ITEM:
                return TCEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_TC_TABLE:
                return insertTCData(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a thingsee data into the database with the given content values. Return the new
     * content URI for that specific row in the database.
     */
    private Uri insertTCData(Uri uri, ContentValues contentValues) {
        // Because primitive  int don't have null value, we don't need to check for timestamp

        // Check that the sensor id it not null
        String strSensorID = contentValues.getAsString(TCEntry.COLUMN_TC_SENSOR_ID);
        if (strSensorID.isEmpty()){
            throw new IllegalArgumentException("Product requires valid sensor ID");
        }

        // Because primitive  float don't have null value, we don't need to check for sensor value

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new thingsee data with the given values
        long id = database.insert(TCEntry.TABLE_NAME, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_TC_TABLE:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TCEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_TC_ITEM:
                // Delete a single row given by the ID in the URI
                selection = TCEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(TCEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    // We do not use update in our table
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
