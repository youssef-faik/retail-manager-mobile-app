package com.example.myapplication;

import android.os.Bundle;

import com.example.myapplication.databinding.ActivityInvoicesBinding;

public class InvoicesActivity extends DrawerBaseActivity {
  ActivityInvoicesBinding activityInvoicesBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityInvoicesBinding = ActivityInvoicesBinding.inflate(getLayoutInflater());
    setContentView(activityInvoicesBinding.getRoot());
  }
}