package metro.ourthingsee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import metro.ourthingsee.POSTs.Authentication;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login activity using retrofit. User must enter email & password.
 * Then accountAuthUuid and accountAuthToken is recorded and store in sharedpref.
 *
 */
public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    EditText edtEmail, edtPassword;
    Button btnLogin;
    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControls();
        addEvents();
    }

    private void addControls() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        apiService = AppUtils.getAPIService();
    }

    private void addEvents() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = edtEmail.getText().toString();
                String strPassword = edtPassword.getText().toString();
                sendPost(strEmail, strPassword);
            }
        });
    }

    public void sendPost(final String email, String password) {
        apiService.savePost(email, password).enqueue(new Callback<Authentication>() {
            @Override
            public void onResponse(Call<Authentication> call, Response<Authentication> response) {
                Log.i(LOG_TAG, response.code() + "");
                if (response.isSuccessful()) {
                    recordLoginData(response.body());
                }
            }

            @Override
            public void onFailure(Call<Authentication> call, Throwable t) {
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    public void recordLoginData(Authentication response) {
        SharedPreferences prefs =
                getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(OurContract.PREF_AUTH_TOKEN_NAME,
                response.getAccountAuthToken().toString()).apply();
        prefs.edit().putString(OurContract.PREF_AUTH_ID_NAME,
                response.getAccountAuthUuid().toString()).apply();
        prefs.edit().putString(OurContract.PREF_AUTH_EMAIL,
                edtEmail.getText().toString()).apply();
        prefs.edit().putString(OurContract.PREF_AUTH_PASSWORD,
                edtEmail.getText().toString()).apply();
        finish();
    }


//    @Override
//    public void onLoadFinished(Loader<String> loader, String data) {
//        // If there's data then record its in SharedPref
//        // The date received in this activity is auth token
//        // Also store user's email & pass for further task
//        if (data != null && !data.isEmpty()) {
//            SharedPreferences prefs =
//                    getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
//            prefs.edit().putString(OurContract.PREF_AUTH_TOKEN_NAME, data).apply();
//            prefs.edit().putString(OurContract.PREF_AUTH_EMAIL,
//                    edtEmail.getText().toString()).apply();
//            prefs.edit().putString(OurContract.PREF_AUTH_PASSWORD,
//                    edtEmail.getText().toString()).apply();
//            Toast.makeText(this, R.string.login_toast_login_succeeded, Toast.LENGTH_SHORT).show();
//            finish();
//        } else {
//            Toast.makeText(this, R.string.login_toast_login_failed, Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    @Override
//    public void onLoaderReset(Loader<String> loader) {
//
//    }
}
