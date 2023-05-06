package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.example.myapplication.databinding.ActivityProductsBinding;

import java.util.List;

import io.swagger.client.ApiException;
import io.swagger.client.api.ProductApi;
import io.swagger.client.model.ProductResponseDto;

public class ProductsActivity extends DrawerBaseActivity {
  ActivityProductsBinding activityProductsBinding;
  private ListView productList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityProductsBinding = ActivityProductsBinding.inflate(getLayoutInflater());
    setContentView(activityProductsBinding.getRoot());

    productList = findViewById(R.id.ListViewProducts);
    new GetProductsTask().execute();
  }

  private class GetProductsTask extends AsyncTask<Void, Void, List<ProductResponseDto>> {

    @Override
    protected List<ProductResponseDto> doInBackground(Void... voids) {
      ProductApi apiInstance = new ProductApi();
      try {
        return apiInstance.getAllProducts();
      } catch (ApiException e) {
        System.err.println("Exception when calling ProductApi#getAllCustomers");
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPostExecute(List<ProductResponseDto> products) {
      if (products != null) {
        // Create an instance of the ProductListAdapter
        ProductListAdapter productAdapter = new ProductListAdapter(ProductsActivity.this, products);

        // Set the productAdapter for the ListView
        productList.setAdapter(productAdapter);
      }
    }
  }

}