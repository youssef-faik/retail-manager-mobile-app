package com.example.myapplication.product;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.DrawerBaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityProductsBinding;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.swagger.client.ApiException;
import io.swagger.client.api.ProduitApi;
import io.swagger.client.model.ProductRequestDto;

public class ProductsActivity extends DrawerBaseActivity {
  ActivityProductsBinding activityProductsBinding;
  private ListView productList;
  private ProductListAdapter productAdapter;
  private ProgressBar mProgressBar;
  private TextView textViewAvailable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityProductsBinding = ActivityProductsBinding.inflate(getLayoutInflater());
    setContentView(activityProductsBinding.getRoot());

    productList = findViewById(R.id.ListViewProducts);
    mProgressBar = findViewById(R.id.progressBar);
    textViewAvailable = findViewById(R.id.textViewAvailable);

    // Create an instance of the ProductListAdapter
    productAdapter = new ProductListAdapter(ProductsActivity.this, new ArrayList<>(), mProgressBar, textViewAvailable);

    // Set the productAdapter for the ListView
    productList.setAdapter(productAdapter);

    // Initialize your button and other views here
    Button addButton = findViewById(R.id.add_button_button);

    // Set an OnClickListener on the "Add Product" button
    addButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showAddProductDialog();
      }
    });

  }

  private void showAddProductDialog() {
    final Dialog dialog = new Dialog(this);
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_new_product);
    dialog.setTitle("Ajouter un nouveau produit");

    // retrieve user input
    final EditText nameEditText = dialog.findViewById(R.id.name_edit_text);
    final EditText purchasePriceEditText = dialog.findViewById(R.id.purchase_price_edit_text);
    final EditText sellingPriceEditText = dialog.findViewById(R.id.selling_price_edit_text);
    final EditText barcodeEditText = dialog.findViewById(R.id.barcode_edit_text);
    final Spinner spinnerTaxRate = dialog.findViewById(R.id.spinner_tax_rate);

    ArrayAdapter<ProductRequestDto.TaxRateEnum> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            ProductRequestDto.TaxRateEnum.values());

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerTaxRate.setAdapter(adapter);

    Button cancelButton = dialog.findViewById(R.id.button_cancel);
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    Button saveButton = dialog.findViewById(R.id.button_save);
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Disable the button until the condition is met
        saveButton.setEnabled(false);

        // Get the input values
        String name = nameEditText.getText().toString().trim();
        String barcode = barcodeEditText.getText().toString().trim();
        String purchasePriceString = purchasePriceEditText.getText().toString().trim();
        String sellingPriceString = sellingPriceEditText.getText().toString().trim();

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(name)) {
          nameEditText.setError("Le nom du produit est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(purchasePriceString)) {
          purchasePriceEditText.setError("Le prix d'achat du produit est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(sellingPriceString)) {
          sellingPriceEditText.setError("Le prix de vente du produit est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(barcode)) {
          barcodeEditText.setError("Le code-barres du produit est requis");
          isValid = false;
        }

        // If the input values are valid, save the product and dismiss the dialog
        if (isValid) {
          // create
          double purchasePrice = Double.parseDouble(purchasePriceEditText.getText().toString());
          double sellingPrice = Double.parseDouble(sellingPriceEditText.getText().toString());
          ProductRequestDto.TaxRateEnum taxRate = (ProductRequestDto.TaxRateEnum) spinnerTaxRate.getSelectedItem();

          // Create the request body
          ProductRequestDto productRequestDto = new ProductRequestDto();
          productRequestDto.barCode(barcode);
          productRequestDto.name(name);
          productRequestDto.sellingPriceExcludingTax(BigDecimal.valueOf(sellingPrice));
          productRequestDto.purchasePrice(BigDecimal.valueOf(purchasePrice));
          productRequestDto.taxRate(taxRate);

          // Perform API call to save the newly created product
          new CreateProductTask().execute(productRequestDto);
          dialog.dismiss();
        }

        // Enable the button
        saveButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private class CreateProductTask extends AsyncTask<ProductRequestDto, Void, Void> {
    @Override
    protected Void doInBackground(ProductRequestDto... productRequestDtos) {
      ProduitApi apiInstance = new ProduitApi();
      try {
        apiInstance.createProduct(productRequestDtos[0]);
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à ProductApi#createProduct");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the the products ListView
      productAdapter.refreshData();
      Toast.makeText(ProductsActivity.this, "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
    }
  }

}