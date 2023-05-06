package com.example.myapplication;

import android.os.Bundle;

import com.example.myapplication.databinding.ActivityUsersBinding;

public class UsersActivity extends DrawerBaseActivity {
  ActivityUsersBinding activityUsersBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityUsersBinding = ActivityUsersBinding.inflate(getLayoutInflater());
    setContentView(activityUsersBinding.getRoot());
  }

}