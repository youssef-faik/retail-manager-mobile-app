package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.customer.CustomersActivity;
import com.example.myapplication.invoice.InvoicesActivity;
import com.example.myapplication.product.ProductsActivity;
import com.example.myapplication.user.UsersActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import io.swagger.client.Configuration;
import io.swagger.client.auth.OAuth;

public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
  DrawerLayout drawerLayout;

  @Override
  public void setContentView(View view) {
    drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
    FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
    container.addView(view);
    super.setContentView(drawerLayout);

    Toolbar toolbar = drawerLayout.findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    NavigationView navigationView = drawerLayout.findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(this);

    // Get a reference to the SharedPreferences object
    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
    String role = prefs.getString("role", "");

    if (role.equalsIgnoreCase("manager")) {
      navigationView.getMenu().removeItem(R.id.users);
    }

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
    toggle.getDrawerArrowDrawable().setColor(getColor(R.color.linear_layout_bg_color));
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    drawerLayout.closeDrawer(GravityCompat.START);

    int id = item.getItemId();

    if (id == R.id.dashboard) {
      startActivity(new Intent(this, DashboardActivity.class));
      overridePendingTransition(0, 0);
      return true;
    } else if (id == R.id.products) {
      startActivity(new Intent(this, ProductsActivity.class));
      overridePendingTransition(0, 0);
      return true;
    } else if (id == R.id.users) {
      startActivity(new Intent(this, UsersActivity.class));
      overridePendingTransition(0, 0);
      return true;
    } else if (id == R.id.customers) {
      startActivity(new Intent(this, CustomersActivity.class));
      overridePendingTransition(0, 0);
      return true;
    } else if (id == R.id.invoices) {
      startActivity(new Intent(this, InvoicesActivity.class));
      overridePendingTransition(0, 0);
      return true;
    }

    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_logout) {
      // Delete JWT token & user role from shared preferences
      SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString("token", "");
      editor.putString("role", "");
      editor.apply();

      // Set JWT token for default ApiClient
      OAuth bearer_authentication = (OAuth) Configuration.getDefaultApiClient().getAuthentication("Bearer_Authentication");
      bearer_authentication.setAccessToken("");

      // Show DashboardActivity
      Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
      finish();
      startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}