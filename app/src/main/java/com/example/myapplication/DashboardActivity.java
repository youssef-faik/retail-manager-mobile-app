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
import io.swagger.client.api.DashboardApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.model.ChartDataDto;

public class DashboardActivity extends DrawerBaseActivity {
  // API IP Address
  final String IP_ADDRESS = "192.168.1.101";
  ActivityDashboardBinding activityDashboardBinding;
  LineChart revenueChart;
  LineChart customersChart;
  BarChart ordersChart;
  BarChart monthlySalesChart;
  private TextView totalRevenueTextView;
  private TextView totalCustomersTextView;
  private TextView totalOrdersTextView;
  private TextView totalMonthlySalesTextView;
  private ProgressBar revenueProgressBar;
  private ProgressBar ordersProgressBar;
  private ProgressBar monthlySalesProgressBar;
  private ProgressBar customersProgressBar;


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

      ordersChart = (BarChart) findViewById(R.id.ordersChart);
      ordersProgressBar = findViewById(R.id.ordersProgressBar);
      totalOrdersTextView = findViewById(R.id.totalOrdersTextView);

      revenueChart = (LineChart) findViewById(R.id.revenueCart);
      revenueProgressBar = findViewById(R.id.revenueProgressBar);
      totalRevenueTextView = findViewById(R.id.totalRevenueTextView);

      monthlySalesChart = (BarChart) findViewById(R.id.monthlySalesChart);
      monthlySalesProgressBar = findViewById(R.id.monthlySalesProgressBar);
      totalMonthlySalesTextView = findViewById(R.id.totalMonthlySalesTextView);

      customersChart = (LineChart) findViewById(R.id.customersChart);
      customersProgressBar = findViewById(R.id.customersProgressBar);
      totalCustomersTextView = findViewById(R.id.totalCustomersTextView);

      new LoadOrdersChatDataTask().execute();
      new LoadRevenueChatDataTask().execute();
      new LoadMonthlySalesChatDataTask().execute();
      new LoadCustomersChatDataTask().execute();
    }

  }


  private class LoadOrdersChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "An error occurred while processing your request";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      ordersChart.setVisibility(View.GONE);
      totalOrdersTextView.setVisibility(View.GONE);
      ordersProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      DashboardApi apiInstance = new DashboardApi();
      try {
        return apiInstance.getOrders(LocalDate.now(), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Failed to connect to the server.";
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
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
          }
        });
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
        BarDataSet dataSet = new BarDataSet(entries, "Orders");
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

        Integer total = 0;

        for (int i : ordersData.getData()) {
          total += i;
        }

        totalOrdersTextView.setText("$" + total);
        totalOrdersTextView.setVisibility(View.VISIBLE);
      }
      ordersChart.setVisibility(View.VISIBLE);

    }

  }

  private class LoadRevenueChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "An error occurred while processing your request";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      revenueChart.setVisibility(View.GONE);
      totalRevenueTextView.setVisibility(View.GONE);
      revenueProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      DashboardApi apiInstance = new DashboardApi();
      try {
        return apiInstance.getRevenue(LocalDate.now(), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Failed to connect to the server.";
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
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
          }
        });
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
    protected void onPostExecute(ChartDataDto revenueData) {
      revenueProgressBar.setVisibility(View.GONE);

      if (revenueData != null) {
        List<Entry> entries = new ArrayList<Entry>();

        for (int i = 0; i < revenueData.getData().size(); i++) {
          entries.add(new BarEntry(i, revenueData.getData().get(i)));
        }

        // add entries to dataset
        LineDataSet dataSet = new LineDataSet(entries, "Revenue");
        dataSet.setColor(getColor(R.color.purple_bg_color));
        dataSet.setValueTextColor(getColor(R.color.purple_bg_color));

        LineData lineData = new LineData(dataSet);
        revenueChart.setData(lineData);

        // description
        revenueChart.getDescription().setEnabled(false);
        revenueChart.getLegend().setEnabled(false);

        // the labels that should be drawn on the XAxis
        final String[] dates = revenueData.getDates().toArray(new String[0]);
        ValueFormatter formatter = new ValueFormatter() {
          @Override
          public String getAxisLabel(float value, AxisBase axis) {
            return dates[(int) value];
          }
        };

        XAxis xAxis = revenueChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setXOffset(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        revenueChart.getAxisLeft().setDrawAxisLine(false);
        revenueChart.getAxisLeft().setDrawZeroLine(true);
        revenueChart.getAxisRight().setEnabled(false);

        revenueChart.animateX(2000, Easing.EaseInOutExpo);

        Integer total = 0;

        for (int i : revenueData.getData()) {
          total += i;
        }

        totalRevenueTextView.setText("$" + total);
        totalRevenueTextView.setVisibility(View.VISIBLE);
      }
      revenueChart.setVisibility(View.VISIBLE);

    }

  }

  private class LoadMonthlySalesChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "An error occurred while processing your request";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      monthlySalesChart.setVisibility(View.GONE);
      totalMonthlySalesTextView.setVisibility(View.GONE);
      monthlySalesProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      DashboardApi apiInstance = new DashboardApi();
      try {
        return apiInstance.getSales(LocalDate.now(), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Failed to connect to the server.";
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
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
          }
        });
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
        BarDataSet dataSet = new BarDataSet(entries, "Monthly Sales");
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

        Integer total = 0;

        for (int i : monthlySalesData.getData()) {
          total += i;
        }

        totalMonthlySalesTextView.setText("$" + total);
        totalMonthlySalesTextView.setVisibility(View.VISIBLE);
      }
      monthlySalesChart.setVisibility(View.VISIBLE);

    }

  }

  private class LoadCustomersChatDataTask extends AsyncTask<Void, Void, ChartDataDto> {
    String errorMessage = "An error occurred while processing your request";

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      customersChart.setVisibility(View.GONE);
      totalCustomersTextView.setVisibility(View.GONE);
      customersProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ChartDataDto doInBackground(Void... voids) {
      DashboardApi apiInstance = new DashboardApi();
      try {
        return apiInstance.getCustomers(LocalDate.now(), LocalDate.now());
      } catch (ApiException e) {
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }

          if (e.getCause() instanceof SocketTimeoutException) {
            errorMessage = "Failed to connect to the server.";
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
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
          }
        });
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
        LineDataSet dataSet = new LineDataSet(entries, "customers");
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

        Integer total = 0;

        for (int i : customersData.getData()) {
          total += i;
        }

        totalCustomersTextView.setText(total.toString());
        totalCustomersTextView.setVisibility(View.VISIBLE);
      }
      customersChart.setVisibility(View.VISIBLE);

    }

  }

}
