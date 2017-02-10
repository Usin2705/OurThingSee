package metro.ourthingsee;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ThingSee> {
    SharedPreferences prefs;

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Adapter for the ListView
     */
    TCCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);

        Button btnButton = (Button) findViewById(R.id.btnButton);

        // If user did not login before, open login activity first
        if (prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "").isEmpty()) {
            Intent intent = new Intent (this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(LOG_TAG, prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME,""));
            Log.e(LOG_TAG, prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME,""));
        }

        // Find the ListView which will be populated with the inventory data
        ListView invListView = (ListView) findViewById(R.id.listView_main);

        // Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new TCCursorAdapter(this, null);
        invListView.setAdapter(mCursorAdapter);

        btnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME,""));
                Log.e(LOG_TAG, prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME,""));
            }
        });
        // TODO set onlick listenter to display event in dababase
    }

    @Override
    public Loader<ThingSee> onCreateLoader(int id, Bundle args) {
        return null;

//        // Define a projection that specifies the columns from the table we care about.
//        String[] projection = {
//                TCEntry._ID,
//                TCEntry.COLUMN_TC_TIMESTAMP,
//                TCEntry.COLUMN_TC_SENSOR_ID,
//                TCEntry.COLUMN_TC_SENSOR_VALUE
//        };
//
//        // This loader will execute the ContentProvider's query method on a background thread
//        return new CursorLoader(this,           // Parent activity context
//                TCEntry.CONTENT_URI,            // Provider content URI to query
//                projection,                     // Columns to include in the resulting Cursor
//                null,                           // No selection clause
//                null,                           // No selection arguments
//                null);                          // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<ThingSee> loader, ThingSee data) {
        // Update {@link InvCursorAdapter} with this new cursor containing updated inv data
        //mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<ThingSee> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
