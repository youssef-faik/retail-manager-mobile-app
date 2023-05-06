package com.example.myapplication;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import java.util.List;

import io.swagger.client.model.ProductResponseDto;

public class ProductListAdapter extends ArrayAdapter<ProductResponseDto> {
  private final List<ProductResponseDto> products;
  private final Activity activity;

  public ProductListAdapter(Activity activity, List<ProductResponseDto> products) {
    super(activity, R.layout.product_list_item, products);
    this.activity = activity;
    this.products = products;
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
        // Handle delete menu item click
        if (itemId == R.id.updateProductMenuItem) {// Handle update menu item click
          return true;
        } else return itemId == R.id.deleteProductMenuItem;
      });
      popupMenu.show();
    });

    return view;
  }
}

