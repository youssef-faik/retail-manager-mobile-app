package com.example.myapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import java.util.List;

import io.swagger.client.ApiException;
import io.swagger.client.api.ProductApi;
import io.swagger.client.model.ProductResponseDto;

public class ProductListAdapter extends ArrayAdapter<ProductResponseDto> {
  private final List<ProductResponseDto> products;
  private final Activity activity;

  public ProductListAdapter(Activity activity, List<ProductResponseDto> products) {
    super(activity, R.layout.product_list_item, products);
    this.activity = activity;
    this.products = products;
    refreshData();
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      LayoutInflater inflater = activity.getLayoutInflater();
      view = inflater.inflate(R.layout.product_list_item, null);
    }

    // Get the product at the current position
    ProductResponseDto product = products.get(position);

    // Set the product name, price, and barcode in the corresponding TextViews
    TextView nameTextView = view.findViewById(R.id.nameTextView);
    nameTextView.setText(product.getName());

    TextView priceTextView = view.findViewById(R.id.priceTextView);
    priceTextView.setText(String.format("$%.2f", product.getSellingPriceExcludingTax()));

    TextView barcodeTextView = view.findViewById(R.id.barcodeTextView);
    barcodeTextView.setText(product.getBarCode());

    // Set the options button click listener to show the menu
    Button optionsButton = view.findViewById(R.id.optionsButton);
    optionsButton.setOnClickListener(v -> {
      PopupMenu popupMenu = new PopupMenu(activity, v);
      popupMenu.inflate(R.menu.product_options_menu);
      popupMenu.setOnMenuItemClickListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.updateProductMenuItem) {
          // Handle update menu item click

          Toast.makeText(this.getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
          return true;

        } else {
          // Handle delete menu item click
          new DeleteProductTask().execute(product.getId());
          Toast.makeText(this.getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
          return itemId == R.id.deleteProductMenuItem;
        }
      });
      popupMenu.show();
    });

    return view;
  }

  public void refreshData() {
    new GetProductsTask().execute();
  }

  private class GetProductsTask extends AsyncTask<Void, Void, List<ProductResponseDto>> {
    @Override
    protected List<ProductResponseDto> doInBackground(Void... voids) {
      ProductApi apiInstance = new ProductApi();
      try {
        return apiInstance.getAllProducts();
      } catch (ApiException e) {
        System.err.println("Exception when calling ProductApi#listProducts");
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPostExecute(List<ProductResponseDto> products) {
      if (products != null) {
        clear();
        addAll(products);
        notifyDataSetChanged();
      }
    }
  }

  private class DeleteProductTask extends AsyncTask<Integer, Void, Void> {
    @Override
    protected Void doInBackground(Integer... integers) {
      ProductApi apiInstance = new ProductApi();
      try {
        apiInstance.deleteProduct(integers[0]);
      } catch (ApiException e) {
        System.err.println("Exception when calling ProductApi#createProduct");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of products after deleting a product
      refreshData();
    }
  }

}

