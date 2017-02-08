package metro.ourthingsee.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Nhan Phan on 08-Feb-17.
 */

public class TCContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "metro.ourthingsee";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://metro.ourthingsee/thingsee/ is a valid path for
     * looking at thingsee data. content://metro.ourthingsee/thingsee/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_THINGSEE = "thingsee";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private TCContract() {
    }

    /**
     * Inner class that defines constant values for the thingsee database table.
     * Each entry in the table represents a single data.
     */
    public static final class TCEntry implements BaseColumns {
        /**
         * The content URI to access the thingsee data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,
                PATH_THINGSEE);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of thingsee data.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_THINGSEE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single thingsee data.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_THINGSEE;

        /**
         * Name of database table for thingsee data
         */
        public final static String TABLE_NAME = PATH_THINGSEE;

        /**
         * Unique ID number for the thingsee data (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Timestamp of the thingsee data received.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_TC_TIMESTAMP = "timestamp";

        /**
         * Sensor ID of the data.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_TC_SENSOR_ID = "sensor_id";

        /**
         * Sensor value of the data.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_TC_SENSOR_VALUE = "sensor_value";
    }


}
