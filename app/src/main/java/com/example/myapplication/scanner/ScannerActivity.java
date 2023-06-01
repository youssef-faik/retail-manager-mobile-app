package com.example.myapplication.scanner;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import com.example.myapplication.DrawerBaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityScannerBinding;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Collections;

import io.swagger.client.ApiException;
import io.swagger.client.api.ElementPanierApi;
import io.swagger.client.model.CartItemRequestDto;

public class ScannerActivity extends DrawerBaseActivity {
  ActivityScannerBinding activityScannerBinding;

  TextView cartIdTextView;
  TextView orLineTextView;
  Button connectToCartBtn;
  Button scanItemsBtn;
  String cartId = "";

  ActivityResultLauncher<ScanOptions> cartIdLauncher = registerForActivityResult(
          new ScanContract(),
          result -> {
            if (result.getContents() != null) {
              cartId = result.getContents();

              CartItemRequestDto cartItemRequestDto = new CartItemRequestDto();
              cartItemRequestDto.setCartId(cartId);
              cartItemRequestDto.setBarcode("101");

              new SaveCartItemTask().execute(cartItemRequestDto);

              scanItemsBtn.setVisibility(View.VISIBLE);
              orLineTextView.setVisibility(View.VISIBLE);
              connectToCartBtn.setText("Utilize a different cart option.");
              cartIdTextView.setText("Status : Connected to cart id " + cartId);
              Drawable drawable = getResources().getDrawable(R.drawable.ic_circle);
              // Set the tint color
              int tintColor = getResources().getColor(R.color.connected);
              drawable.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));

              // Set the tinted drawable to the TextView
              cartIdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
            }
          }
  );


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityScannerBinding = ActivityScannerBinding.inflate(getLayoutInflater());
    setContentView(activityScannerBinding.getRoot());

    cartIdTextView = findViewById(R.id.cartIdTextView);
    orLineTextView = findViewById(R.id.orLineTexView);
    connectToCartBtn = findViewById(R.id.connectToCartBtn);
    scanItemsBtn = findViewById(R.id.scanItemsBtn);

    connectToCartBtn.setOnClickListener(view -> {
      ScanOptions options = new ScanOptions();

      options.setBeepEnabled(true);
      options.setOrientationLocked(true);
      options.setCaptureActivity(ScanCartActivity.class);
      options.setDesiredBarcodeFormats(Collections.singletonList(Intents.Scan.QR_CODE_MODE));

      cartIdLauncher.launch(options);
    });

    scanItemsBtn.setOnClickListener(view -> {
      Intent intent = new Intent(this, ScanItemsActivity.class);
      intent.putExtra("cartId", cartId);
      startActivity(intent);
    });

  }

  private class SaveCartItemTask extends AsyncTask<CartItemRequestDto, Void, Void> {
    @Override
    protected Void doInBackground(CartItemRequestDto... cartItemRequestDtos) {
      ElementPanierApi apiInstance = new ElementPanierApi();
      try {
        apiInstance.saveCartItem(cartItemRequestDtos[0]);
      } catch (ApiException e) {
        System.err.println("Exception when calling ProductApi#createProduct");
        e.printStackTrace();
      }
      return null;
    }

  }


}