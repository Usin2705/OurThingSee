package metro.ourthingsee.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.Authentication;
import metro.ourthingsee.RESTObjects.Devices;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.Utils;
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
     */
    private void addControls() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        apiService = Utils.getAPIService();
    }

    /**
     * Do stuff here.
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
                if (edtEmail.getText().toString().length() == 0 || edtPassword.getText().toString().length() < 4) {
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
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtEmail.getText().toString().length() == 0 || edtPassword.getText().toString().length() < 4) {
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
                edtEmail.setError(null);
                edtPassword.setError(null);
            }
        });
    }

    /**
     * Send the login data to the ThingSee cloud service
     * After successfully login, we wil continously get our devices
     *
     * @param email    User's email taken from editText
     * @param password User's passoword taken from editText
     */
    public void sendPostAuth(final String email, final String password) {
        apiService.savePostAuth(email, password).enqueue(new Callback<Authentication>() {
            @Override
            public void onResponse(Call<Authentication> call, Response<Authentication> response) {
                Drawable drawable = getDrawable(R.drawable.warning);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                progressDialog.dismiss();
                switch (response.code()) {
                    //The request was fulfilled.
                    case 200:
                        progressDialog.setMessage("Getting user's devices and settings...");
                        progressDialog.show();
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_toast_login_succeeded),
                                Toast.LENGTH_SHORT).show();
                        recordLoginData(response.body());
                        break;
                    //Unauthorized
                    case 401:
                        edtEmail.setError(getString(R.string.login_toast_login_failed), drawable);
                        //clear password and refocus to email edittext
                        edtPassword.setText("");
                        edtEmail.requestFocus();
                        break;
                    //Wrong email format and/or password requirement
                    case 400:
                        //check if email is in right format (contains @ and domain)
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
                            edtEmail.setError(getString(R.string.login_toast_invalid_email), drawable);
                            edtEmail.requestFocus();
                        }
                        //check if password has any digit
                        else if (!edtPassword.getText().toString().matches(".*\\d+.*")) {
                            edtPassword.setError(getString(R.string.login_toast_short_pw_miss_number), null);
                            edtPassword.requestFocus();
                        }
                        //clear password and refocus to email edittext
                        edtPassword.setText("");
                        break;
                    case 503:
                        sendPostAuth(email, password);
                        break;
                }
            }

            @Override
            public void onFailure(Call<Authentication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_toast_login_failed_nointernet),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void recordLoginData(Authentication response) {
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(OurContract.PREF_USER_AUTH_TOKEN_NAME,
                response.getAccountAuthToken()).apply();
        getUserDevices();
    }

    /**
     * Get all user's devices based on information from user authentication
     * Connect Thingsee device with App
     */
    private void getUserDevices() {
        String auth = "Bearer ";
        auth += prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
        apiService.getUserDevices(auth).enqueue(new Callback<Devices>() {
            @Override
            public void onResponse(Call<Devices> call, Response<Devices> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    // store pin code of device
                    recordDeviceData(response.body());
                }
            }

            @Override
            public void onFailure(Call<Devices> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_toast_login_failed_nointernet),
                        Toast.LENGTH_SHORT).show();
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
        String strFirstUuid = response.getDevices().get(0).getUuid();
        String strFirstToken = response.getDevices().get(0).getToken();
        if (!strFirstUuid.isEmpty()) {
            prefs.edit().putString(OurContract.PREF_DEVICE_AUTH_ID_NAME, strFirstUuid).apply();
            prefs.edit().putString(OurContract.PREF_DEVICE_TOKEN, strFirstToken).apply();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // hide keyboard when touch Log in
    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}