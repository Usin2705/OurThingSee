package metro.ourthingsee;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import metro.ourthingsee.RESTObjects.Devices;
import metro.ourthingsee.RESTObjects.Authentication;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login activity using retrofit. User must enter email & password.
 * Then accountAuthUuid and accountAuthToken is recorded and store in sharedpref.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    EditText edtEmail, edtPassword;
    Button btnLogin;
    APIService apiService;
    ProgressDialog progressDialog;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControls();
        addEvents();
    }

    /**
     * Find all button here. The code look fabulous this way.
     *
     */
    private void addControls() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        apiService = AppUtils.getAPIService();
    }

    /**
     * Do stuff here.
     *
     */

    private void addEvents() {
        if (edtEmail.getText().toString().length() == 0 ||
                edtPassword.getText().toString().length() == 0) {
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.5f);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1f);
        }
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtEmail.getText().toString().length() == 0 || edtPassword.getText().toString().length() == 0) {
                    btnLogin.setEnabled(false);
                    btnLogin.setAlpha(0.5f);
                } else {
                    btnLogin.setEnabled(true);
                    btnLogin.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtEmail.getText().toString().length() == 0 || edtEmail.getText().toString().length() == 0) {
                    btnLogin.setEnabled(false);
                    btnLogin.setAlpha(0.5f);
                } else {
                    btnLogin.setEnabled(true);
                    btnLogin.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                progressDialog.show();
                sendPostAuth(email, password);
            }
        });
    }

    /**
     * Send the login data to the ThingSee cloud service
     * After successfully login, we wil continously get our devices
     *
     * @param email     User's email taken from editText
     * @param password  User's passoword taken from editText
     */
    public void sendPostAuth(String email, String password) {
        apiService.savePostAuth(email, password).enqueue(new Callback<Authentication>() {
            @Override
            public void onResponse(Call<Authentication> call, Response<Authentication> response) {
                Log.i(LOG_TAG, response.code() + "");
                progressDialog.dismiss();
                switch (response.code()) {

                    //The request was fulfilled.
                    case 200:
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_toast_login_succeeded),
                                Toast.LENGTH_SHORT).show();
                        recordLoginData(response.body());
                        break;
                    //Unauthorized 
                    case 401:
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_toast_login_failed),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<Authentication> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(LOG_TAG, t.toString());
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_toast_login_failed_nointernet),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *
     * @param response this will show the document when you press Crtl Q
     */
    public void recordLoginData(Authentication response) {
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(OurContract.PREF_USER_AUTH_TOKEN_NAME,
                response.getAccountAuthToken().toString()).apply();
        prefs.edit().putString(OurContract.PREF_AUTH_EMAIL,
                edtEmail.getText().toString()).apply();
        prefs.edit().putString(OurContract.PREF_AUTH_PASSWORD,
                edtEmail.getText().toString()).apply();

        getUserDevices();
    }

    /**
     * Get all user's devices based on information from user authentication
     * Connect Thingsee device with App
     */
    private void getUserDevices() {
        String auth = "Bearer ";
        auth += prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
        Log.e(LOG_TAG,auth);
        apiService.getUserDevices(auth).enqueue(new Callback<Devices>() {
            @Override
            public void onResponse(Call<Devices> call, Response<Devices> response) {
                Log.e("Giang",response.code()+"");
                if (response.isSuccessful()){
                    // store pin code of device
                    recordDeviceData(response.body());
            }
            }

            @Override
            public void onFailure(Call<Devices> call, Throwable t) {
                Log.e(LOG_TAG,t.toString());
            }
        });
    }

    /**
     * Record all user's {@link Devices} Auth ID ("uuid") in shareprefs
     * Now we just do 1
     *
     * @param response the response from cloud, including List of {@link Devices} and
     *                 a timestamp
     */
    private void recordDeviceData(Devices response) {
        Log.e("Giang","HERE! DAMN YOU!");
        String strFirstUuid = response.getDevices().get(0).getUuid();
        if (!strFirstUuid.isEmpty()) {
            prefs.edit().putString(OurContract.PREF_DEVICE_AUTH_ID_NAME, strFirstUuid).apply();
        }
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}