package com.example.myapplication.scanner;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import com.example.myapplication.R;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import io.swagger.client.ApiException;
import io.swagger.client.api.ElementPanierApi;
import io.swagger.client.model.CartItemRequestDto;

public class ScanItemsActivity extends Activity {
  private DecoratedBarcodeView barcodeView;
  private BeepManager beepManager;
  private String cartId;

  private final BarcodeCallback callback = new BarcodeCallback() {
    private boolean isProcessing = false;

    @Override
    public void barcodeResult(BarcodeResult result) {
      if (result.getText() != null && !isProcessing) {
        isProcessing = true;

        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto();
        cartItemRequestDto.setCartId(cartId);
        cartItemRequestDto.setBarcode(result.getText());

        new SaveCartItemTask().execute(cartItemRequestDto);

        beepManager.playBeepSoundAndVibrate();

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            isProcessing = false;
          }
        }, 1500);
      }
    }

    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan_items);

    cartId = getIntent().getStringExtra("cartId");

    barcodeView = findViewById(R.id.barcode_scanner);
    barcodeView.initializeFromIntent(getIntent());
    barcodeView.decodeContinuous(callback);

    beepManager = new BeepManager(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    barcodeView.resume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    barcodeView.pause();
  }

  public void pause(View view) {
    barcodeView.pause();
  }

  public void resume(View view) {
    barcodeView.resume();
  }

  public void finish(View view) {
    this.finish();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
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
