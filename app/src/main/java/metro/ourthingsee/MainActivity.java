package metro.ourthingsee;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import metro.ourthingsee.data.TCContract.TCEntry;

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
        final TextView txtTextView = (TextView) findViewById(R.id.txtTextView);

        if (prefs.getString(OurContract.PREF_AUTH_TOKEN_NAME, "").isEmpty()) {
            Intent intent = new Intent (this, LoginActivity.class);
            startActivity(intent);
        } else {
            txtTextView.setText(prefs.getString(OurContract.PREF_AUTH_TOKEN_NAME,""));
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
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                // If there is a network connection, fetch data
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Get a reference to the LoaderManager, in order to interact with loaders.
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.destroyLoader(OurContract.LOADER_ID_DATALOADER);

                    // Initialize the loader. Pass in the int ID constant defined above and pass
                    // in null for the bundle. Pass in this activity for the LoaderCallbacks
                    // parameter (which is valid because this activity implements the
                    // LoaderCallbacks interface).
                    loaderManager.initLoader(OurContract.LOADER_ID_DATALOADER,
                            null, MainActivity.this);
                }
            }
        });
    }

    @Override
    public Loader<ThingSee> onCreateLoader(int id, Bundle args) {
        return new ThingSeeLoader(this, OurContract.URL_LOAD_DATA, OurContract.LOADER_ID_DATALOADER
                , null, null);

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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link InvCursorAdapter} with this new cursor containing updated inv data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
