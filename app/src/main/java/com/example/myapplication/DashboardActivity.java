package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.myapplication.databinding.ActivityDashboardBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.TableauDeBordApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.model.ChartDataDto;

public class DashboardActivity extends DrawerBaseActivity {
  // API IP Address
  final String IP_ADDRESS = "192.168.1.101";
  ActivityDashboardBinding activityDashboardBinding;
  LineChart customersChart;
  BarChart ordersChart;
  BarChart monthlySalesChart;
  private TextView totalCustomersTextView;
  private TextView totalOrdersTextView;
  private TextView totalMonthlySalesTextView;
  private ProgressBar ordersProgressBar;
  private ProgressBar monthlySalesProgressBar;
  private ProgressBar customersProgressBar;

  private boolean wasRedirectedToLogin = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    super.onCreate(savedInstanceState);
    activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
    setContentView(activityDashboardBinding.getRoot());

    // Get a reference to the SharedPreferences object
    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
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

      new CheckTokenTask().execute();

      ordersChart = findViewById(R.id.ordersChart);
      ordersProgressBar = findViewById(R.id.ordersProgressBar);
      totalOrdersTextView = findViewById(R.id.totalOrdersTextView);

      customersChart = findViewById(R.id.customersChart);
      customersProgressBar = findViewById(R.id.customersProgressBar);
      totalCustomersTextView = findViewById(R.id.totalCustomersTextView);

      monthlySalesChart = findViewById(R.id.monthlySalesChart);
      monthlySalesProgressBar = findViewById(R.id.monthlySalesProgressBar);
      totalMonthlySalesTextView = findViewById(R.id.totalMonthlySalesTextView);

      new LoadOrdersChatDataTask().execute();
      new LoadCustomersChatDataTask().execute();
      new LoadMonthlySalesChatDataTask().execute();
    }

  }


  private void clearTokenAndRedirectToLogin() {
    wasRedirectedToLogin = true;
    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("token", "");
    editor.putString("role", "");
    editor.apply();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getApplicationContext(), "Votre session a expiré.", Toast.LENGTH_SHORT).show();

      }
    });
    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
    startActivity(intent);
    finish();
  }

  private class CheckTokenTask extends AsyncTask<Void, Void, Void> {
    String errorMessage = "Une erreur s'est produite lors du traitement de votre demande";

    @Override
    protected Void doInBackground(Void... voids) {
      TableauDeBordApi apiInstance = new TableauDeBordApi();
      try {
        apiInstance.getOrders(LocalDate.now().minusDays(1), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Échec de la connexion au serveur.";
          }
        } catch (JSONException ex) {
          if (!wasRedirectedToLogin) {
            clearTokenAndRedirectToLogin();
          }
        }

        // Log the error details
        System.err.println("Exception when calling AuthenticationApi#authenticate");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

      } catch (Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
      }

      return null;
    }


  }


  private class LoadOrdersChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "Une erreur s'est produite lors du traitement de votre demande";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      ordersChart.setVisibility(View.GONE);
      totalOrdersTextView.setVisibility(View.GONE);
      ordersProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      TableauDeBordApi apiInstance = new TableauDeBordApi();
      try {
        return apiInstance.getOrders(LocalDate.now().minusDays(30), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Échec de la connexion au serveur.";
          }
        } catch (JSONException ex) {
          if (!wasRedirectedToLogin) {
            clearTokenAndRedirectToLogin();
          }
        }

        // Log the error details
        System.err.println("Exception when calling AuthenticationApi#authenticate");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

      } catch (Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
      }

      return null;
    }

    @Override
    protected void onPostExecute(ChartDataDto ordersData) {
      ordersProgressBar.setVisibility(View.GONE);

      if (ordersData != null) {
        List<BarEntry> entries = new ArrayList<BarEntry>();

        for (int i = 0; i < ordersData.getData().size(); i++) {
          entries.add(new BarEntry(i, ordersData.getData().get(i)));
        }

        // add entries to dataset
        BarDataSet dataSet = new BarDataSet(entries, "Commandes");
        dataSet.setColor(getColor(R.color.purple_bg_color));
        dataSet.setValueTextColor(getColor(R.color.purple_bg_color));

        BarData lineData = new BarData(dataSet);
        ordersChart.setData(lineData);

        // the labels that should be drawn on the XAxis
        final String[] dates = ordersData.getDates().toArray(new String[0]);
        ValueFormatter formatter = new ValueFormatter() {
          @Override
          public String getAxisLabel(float value, AxisBase axis) {
            return dates[(int) value];
          }
        };

        ordersChart.getDescription().setEnabled(false);
        ordersChart.getLegend().setEnabled(false);

        XAxis xAxis = ordersChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setXOffset(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        ordersChart.getAxisLeft().setDrawAxisLine(false);
        ordersChart.getAxisLeft().setDrawZeroLine(true);
        ordersChart.getAxisRight().setEnabled(false);

        ordersChart.animateX(2000, Easing.EaseInOutExpo);

        Float total = 0f;

        for (float i : ordersData.getData()) {
          total += i;
        }

        totalOrdersTextView.setText(total.intValue() + "Commande(s)");
        totalOrdersTextView.setVisibility(View.VISIBLE);
      }
      ordersChart.setVisibility(View.VISIBLE);

    }

  }

  private class LoadMonthlySalesChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "Une erreur s'est produite lors du traitement de votre demande";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      monthlySalesChart.setVisibility(View.GONE);
      totalMonthlySalesTextView.setVisibility(View.GONE);
      monthlySalesProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      TableauDeBordApi apiInstance = new TableauDeBordApi();
      try {
        return apiInstance.getSales(LocalDate.now().minusDays(30), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Échec de la connexion au serveur.";
          }
        } catch (JSONException ex) {
          if (!wasRedirectedToLogin) {
            clearTokenAndRedirectToLogin();
          }
        }

        // Log the error details
        System.err.println("Exception when calling AuthenticationApi#authenticate");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

      } catch (Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
      }

      return null;
    }

    @Override
    protected void onPostExecute(ChartDataDto monthlySalesData) {
      monthlySalesProgressBar.setVisibility(View.GONE);

      if (monthlySalesData != null) {
        List<BarEntry> entries = new ArrayList<BarEntry>();

        for (int i = 0; i < monthlySalesData.getData().size(); i++) {
          entries.add(new BarEntry(i, monthlySalesData.getData().get(i)));
        }

        // add entries to dataset
        BarDataSet dataSet = new BarDataSet(entries, "Ventes mensuelles");
        dataSet.setColor(getColor(R.color.purple_bg_color));
        dataSet.setValueTextColor(getColor(R.color.purple_bg_color));

        BarData barData = new BarData(dataSet);
        monthlySalesChart.setData(barData);

        monthlySalesChart.getDescription().setEnabled(false);
        monthlySalesChart.getLegend().setEnabled(false);

        final String[] dates = monthlySalesData.getDates().toArray(new String[0]);
        ValueFormatter formatter = new ValueFormatter() {
          @Override
          public String getAxisLabel(float value, AxisBase axis) {
            return dates[(int) value];
          }
        };

        XAxis xAxis = monthlySalesChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setXOffset(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        monthlySalesChart.getAxisLeft().setDrawAxisLine(false);
        monthlySalesChart.getAxisLeft().setDrawZeroLine(true);
        monthlySalesChart.getAxisRight().setEnabled(false);

        monthlySalesChart.animateX(2000, Easing.EaseInOutExpo);

        Float total = 0f;

        for (float i : monthlySalesData.getData()) {
          total += i;
        }

        totalMonthlySalesTextView.setText(String.format("%.2f", total.floatValue()));
        totalMonthlySalesTextView.setVisibility(View.VISIBLE);
      }
      monthlySalesChart.setVisibility(View.VISIBLE);

    }

  }

  private class LoadCustomersChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "Une erreur s'est produite lors du traitement de votre demande";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      customersChart.setVisibility(View.GONE);
      totalCustomersTextView.setVisibility(View.GONE);
      customersProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      TableauDeBordApi apiInstance = new TableauDeBordApi();
      try {
        return apiInstance.getCustomers(LocalDate.now().minusDays(30), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Échec de la connexion au serveur.";
          }
        } catch (JSONException ex) {
          if (!wasRedirectedToLogin) {
            clearTokenAndRedirectToLogin();
          }
        }

        // Log the error details
        System.err.println("Exception when calling AuthenticationApi#authenticate");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

      } catch (Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
      }

      return null;
    }

    @Override
    protected void onPostExecute(ChartDataDto customersData) {
      customersProgressBar.setVisibility(View.GONE);

      if (customersData != null) {
        List<Entry> entries = new ArrayList<Entry>();

        for (int i = 0; i < customersData.getData().size(); i++) {
          entries.add(new Entry(i, customersData.getData().get(i)));
        }

        // add entries to dataset
        LineDataSet dataSet = new LineDataSet(entries, "clients");
        dataSet.setColor(getColor(R.color.purple_bg_color));
        dataSet.setValueTextColor(getColor(R.color.purple_bg_color));

        LineData lineData = new LineData(dataSet);
        customersChart.setData(lineData);

        // the labels that should be drawn on the XAxis
        final String[] dates = customersData.getDates().toArray(new String[0]);
        ValueFormatter formatter = new ValueFormatter() {
          @Override
          public String getAxisLabel(float value, AxisBase axis) {
            return dates[(int) value];
          }
        };

        customersChart.getDescription().setEnabled(false);
        customersChart.getLegend().setEnabled(false);

        XAxis xAxis = customersChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setXOffset(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        customersChart.getAxisLeft().setDrawAxisLine(false);
        customersChart.getAxisLeft().setDrawZeroLine(true);
        customersChart.getAxisRight().setEnabled(false);

        customersChart.animateX(2000, Easing.EaseInOutExpo);

        Float total = 0f;

        for (float i : customersData.getData()) {
          total += i;
        }

        totalCustomersTextView.setText(total.intValue() + "Client(s)");
        totalCustomersTextView.setVisibility(View.VISIBLE);
      }
      customersChart.setVisibility(View.VISIBLE);

    }

  }

}
