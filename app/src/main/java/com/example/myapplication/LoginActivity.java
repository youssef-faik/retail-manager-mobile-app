package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.AuthenticationApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.model.AuthenticationRequest;
import io.swagger.client.model.AuthenticationResponse;
import io.swagger.client.model.UserCreateDto;

public class LoginActivity extends AppCompatActivity {
  private ProgressBar mProgressBar;
  private Button loginBtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // retrieve user input
    final EditText email = findViewById(R.id.editTextTextEmailAddress);
    final EditText password = findViewById(R.id.editTextTextPassword);
    loginBtn = findViewById(R.id.buttonLogin);

    // Set click listener for login button
    loginBtn.setOnClickListener(new View.OnClickListener() {
      @SuppressLint("CheckResult")
      @Override
      public void onClick(View view) {
        // Disable the login button
        loginBtn.setEnabled(false);

        // Get the input values
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();

        // Find the progress bar view by its ID
        mProgressBar = findViewById(R.id.progressBar);

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(emailTxt)) {
          email.setError("Email is required");
          isValid = false;
        }

        if (TextUtils.isEmpty(passwordTxt)) {
          password.setError("Password is required");
          isValid = false;
        }

        // If the input values are valid, try to sign in user
        if (isValid) {
          loginBtn.setEnabled(false);
          // Perform API call for user authentication
          AuthenticationRequest body = new AuthenticationRequest();
          body.setEmail(emailTxt);
          body.setPassword(passwordTxt);

          AuthenticateTask authenticateTask = new AuthenticateTask(getApplicationContext(), mProgressBar);
          authenticateTask.execute(body);

        }
      }
    });

  }

  private class AuthenticateTask extends AsyncTask<AuthenticationRequest, Void, AuthenticationResponse> {
    public final ProgressBar mProgressBar;
    private final Context mContext;
    String errorMessage = "An error occurred while processing your request";

    public AuthenticateTask(Context context, ProgressBar progressBar) {
      mContext = context;
      mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected AuthenticationResponse doInBackground(AuthenticationRequest... authenticationRequests) {
      AuthenticationApi apiInstance = new AuthenticationApi();
      try {
        return apiInstance.authenticate(authenticationRequests[0]);
      } catch (ApiException e) {
        // Handle the error here
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }
        } catch (JSONException ex) {
          throw new RuntimeException(ex);
        }

        // Log the error details
        System.err.println("Exception when calling AuthenticationApi#authenticate");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

        // display toast with the error message
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
          }
        });
      } catch (Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
      }

      return null;
    }

    @Override
    protected void onPostExecute(AuthenticationResponse authenticationResponse) {
      // Handle the result here
      if (authenticationResponse != null) {
        if (authenticationResponse.getRole().equalsIgnoreCase(String.valueOf(UserCreateDto.RoleEnum.ADMIN))
                || authenticationResponse.getRole().equalsIgnoreCase(String.valueOf(UserCreateDto.RoleEnum.MANAGER))

        ) {
          // Save JWT token in shared preferences
          SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
          SharedPreferences.Editor editor = prefs.edit();
          editor.putString("token", authenticationResponse.getToken());
          editor.apply();

          // Set JWT token for default ApiClient
          OAuth bearer_authentication = (OAuth) Configuration.getDefaultApiClient().getAuthentication("Bearer_Authentication");
          bearer_authentication.setAccessToken(authenticationResponse.getToken());

          // Show DashboardActivity
          Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
          finish();
          startActivity(intent);
        } else {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(mContext, "You don't have permission to use the application", Toast.LENGTH_SHORT).show();
            }
          });
        }

      }
      mProgressBar.setVisibility(View.GONE);
      loginBtn.setEnabled(true);

    }

  }

}



