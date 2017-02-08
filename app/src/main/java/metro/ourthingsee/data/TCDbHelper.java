package metro.ourthingsee.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import metro.ourthingsee.data.TCContract.TCEntry;

/**
 * Created by Nhan Phan on 08-Feb-17.
 */

public class TCDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "ourthingsee.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link TCDbHelper}.
     *
     * @param context of the app
     */
    public TCDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventories table
        String SQL_CREATE_TC_TABLE = "CREATE TABLE " + TCEntry.TABLE_NAME + " ("
                + TCEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TCEntry.COLUMN_TC_TIMESTAMP + " INTEGER NOT NULL DEFAULT 0, "
                + TCEntry.COLUMN_TC_SENSOR_ID + " TEXT NOT NULL, "
                + TCEntry.COLUMN_TC_SENSOR_VALUE + " REAL NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_TC_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            switch (oldVersion) {

                case 1:
                    // Modify your table here
                    // Add new column "email", "pictureuri" to table "inventories"
//                    db.execSQL("ALTER TABLE " + InvEntry.TABLE_NAME + " ADD COLUMN " +
//                            InvEntry.COLUMN_INV_SUPPLIER_EMAIL + " TEXT");
//                    db.execSQL("ALTER TABLE " + InvEntry.TABLE_NAME + " ADD COLUMN " +
//                            InvEntry.COLUMN_INV_PICTURE_URI + " TEXT");
                    break;

                case 2:
                    // Modify your table here
//                  // Add new column "pictureuri" to table "inventories"
//                    db.execSQL("ALTER TABLE " + InvEntry.TABLE_NAME + " ADD COLUMN " +
//                            InvEntry.COLUMN_INV_PICTURE_URI + " TEXT");
                    break;
                default:
                    break;
            }
        }
    }
}
