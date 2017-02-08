package metro.ourthingsee;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {
    SharedPreferences prefs;

    private static final int NEWS_LOADER_ID = 1;

    TextView txtTextView;

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
                    loaderManager.destroyLoader(NEWS_LOADER_ID);

                    // Initialize the loader. Pass in the int ID constant defined above and pass
                    // in null for the bundle. Pass in this activity for the LoaderCallbacks
                    // parameter (which is valid because this activity implements the
                    // LoaderCallbacks interface).
                    loaderManager.initLoader(NEWS_LOADER_ID, null, MainActivity.this);
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return null;
        //return new ThingSeeLoader(this, THINGSEE_URL);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null && !data.isEmpty()) {
            txtTextView.setText(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
    }
}
