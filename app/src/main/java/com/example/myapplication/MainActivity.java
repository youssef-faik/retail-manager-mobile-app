package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.AuthenticationApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.model.AuthenticationRequest;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // API IP Address
    final String IP_ADDRESS = "172.26.192.1";

    // retrieve user input
    final EditText email = findViewById(R.id.editTextTextEmailAddress);
    final EditText password = findViewById(R.id.editTextTextPassword);
    final Button loginBtn = findViewById(R.id.buttonLogin);

    // Set click listener for login button
    loginBtn.setOnClickListener(new View.OnClickListener() {
      @SuppressLint("CheckResult")
      @Override
      public void onClick(View view) {
        // Get the input values
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();

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
          // Perform API call for user authentication
          Observable.fromCallable(() -> {
                    // Perform network operation here
                    AuthenticationRequest body = new AuthenticationRequest();
                    body.setEmail(emailTxt);
                    body.setPassword(passwordTxt);

                    ApiClient defaultClient = Configuration.getDefaultApiClient();
                    defaultClient.setBasePath("http://" + IP_ADDRESS + ":8080");

                    AuthenticationApi apiInstance = new AuthenticationApi();
                    return apiInstance.authenticate(body);
                  })
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(result -> {
                    // Handle the result here when success

                    // Save JWT token in shared preferences
                    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("token", result.getToken());
                    editor.apply();

                    OAuth bearer_authentication = (OAuth) Configuration.getDefaultApiClient().getAuthentication("Bearer_Authentication");
                    bearer_authentication.setAccessToken(result.getToken());

                    // Show home activity
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();

                  }, error -> {
                    // Handle the error here
                    System.err.println("Exception when calling AuthenticationApi#authenticate");
                    System.out.println(((ApiException) error).getResponseBody());

                    JSONObject json = new JSONObject(((ApiException) error).getResponseBody());
                    String message = "Error : " + json.getString("message");

                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                  });
        }
      }
    });

  }
}



