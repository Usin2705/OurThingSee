package metro.ourthingsee;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    EditText et_email, et_pw;
    Button btn_login;
    APIService apiService;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControls();
        addEvents();
    }

    private void addControls() {
        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        et_email = (EditText) findViewById(R.id.et_email);
        et_pw = (EditText) findViewById(R.id.et_pw);
        btn_login = (Button) findViewById(R.id.btn_login);
        apiService = AppUtils.getAPIService();
    }

    private void addEvents() {
        if (et_email.getText().toString().length() == 0 || et_pw.getText().toString().length() == 0) {
            btn_login.setEnabled(false);
            btn_login.setAlpha(0.5f);
        } else {
            btn_login.setEnabled(true);
            btn_login.setAlpha(1f);
        }
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_email.getText().toString().length() == 0 || et_pw.getText().toString().length() == 0) {
                    btn_login.setEnabled(false);
                    btn_login.setAlpha(0.5f);
                } else {
                    btn_login.setEnabled(true);
                    btn_login.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_email.getText().toString().length() == 0 || et_pw.getText().toString().length() == 0) {
                    btn_login.setEnabled(false);
                    btn_login.setAlpha(0.5f);
                } else {
                    btn_login.setEnabled(true);
                    btn_login.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String email = et_email.getText().toString();
                String password = et_pw.getText().toString();
                progressDialog.show();
                sendPostAuth(email, password);
            }
        });
    }

    public void sendPostAuth(final String email, String password) {
        apiService.savePostAuth(email, password).enqueue(new Callback<Authentication>() {
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
                et_email.getText().toString()).apply();
        prefs.edit().putString(OurContract.PREF_AUTH_PASSWORD,
                et_pw.getText().toString()).apply();
        finish();
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
