package com.example.myapplication;

import android.content.Intent;
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

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
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
}