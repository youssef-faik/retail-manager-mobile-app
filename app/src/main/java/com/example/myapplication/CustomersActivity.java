package com.example.myapplication;

import android.os.Bundle;

import com.example.myapplication.databinding.ActivityCustomersBinding;

public class CustomersActivity extends DrawerBaseActivity {
  ActivityCustomersBinding activityCustomersBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityCustomersBinding = ActivityCustomersBinding.inflate(getLayoutInflater());
    setContentView(activityCustomersBinding.getRoot());
  }
}