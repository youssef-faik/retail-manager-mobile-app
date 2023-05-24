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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
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
  private TextView totalRevenueTextView;
  private ProgressBar revenueProgressBar;

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

      revenueChart = (LineChart) findViewById(R.id.revenueCart);
      revenueProgressBar = findViewById(R.id.revenueProgressBar);
      totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
      new LoadRevenueChatDataTask().execute();
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
        Description revenueChartDescription = revenueChart.getDescription();
        revenueChartDescription.setText("Monthly sales revenue.");

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

        revenueChart.animateX(2000, Easing.EaseInOutExpo);
        revenueChart.setDrawBorders(true);
        revenueChart.setBorderColor(getColor(R.color.light_gray));

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

}
