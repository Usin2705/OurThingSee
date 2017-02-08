package metro.ourthingsee;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Usin on 06-Feb-17.
 */

public class LoginActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    EditText edtEmail, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                // If there is a network connection, fetch data
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Get a reference to the LoaderManager, in order to interact with loaders.
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.destroyLoader(OurContract.LOADER_ID_REGISTER);

                    // Initialize the loader. Pass in the int ID constant defined above and pass
                    // in null for the bundle. Pass in this activity for the LoaderCallbacks
                    // parameter (which is valid because this activity implements the
                    // LoaderCallbacks interface).
                    loaderManager.initLoader(OurContract.LOADER_ID_REGISTER, null,
                            LoginActivity.this);
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new ThingSeeLoader(this, OurContract.URL_REGISTER, OurContract.LOADER_ID_REGISTER,
                edtEmail.getText().toString(), edtPassword.getText().toString());
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // If there's data then record its in SharedPref
        // The date received in this activity is auth token
        // Also store user's email & pass for further task
        if (data != null && !data.isEmpty()) {
            SharedPreferences prefs =
                    getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
            prefs.edit().putString(OurContract.PREF_AUTH_TOKEN_NAME, data).apply();
            prefs.edit().putString(OurContract.PREF_AUTH_EMAIL,
                    edtEmail.getText().toString()).apply();
            prefs.edit().putString(OurContract.PREF_AUTH_PASSWORD,
                    edtEmail.getText().toString()).apply();
            Toast.makeText(this, R.string.login_toast_login_succeeded, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.login_toast_login_failed, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
