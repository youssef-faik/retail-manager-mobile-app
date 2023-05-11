package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.myapplication.databinding.ActivityDashboardBinding;

import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;
import io.swagger.client.auth.OAuth;

public class DashboardActivity extends DrawerBaseActivity {
  // API IP Address
  final String IP_ADDRESS = "192.168.1.100";
  ActivityDashboardBinding activityDashboardBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
    setContentView(activityDashboardBinding.getRoot());

    // Save IP_ADDRESS in shared preferences
    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

    // Get a reference to the SharedPreferences object
    String token = prefs.getString("token", "");

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://" + IP_ADDRESS + ":8080");

    if (TextUtils.isEmpty(token)) {
      Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
      startActivity(intent);
      finish();
    } else {
      // Set JWT token for default ApiClient
      OAuth bearer_authentication = (OAuth) Configuration.getDefaultApiClient().getAuthentication("Bearer_Authentication");
      bearer_authentication.setAccessToken(token);
    }

  }
}