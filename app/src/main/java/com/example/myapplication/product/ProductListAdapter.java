package com.example.myapplication.product;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.example.myapplication.R;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import io.swagger.client.ApiException;
import io.swagger.client.api.ProduitApi;
import io.swagger.client.model.ProductRequestDto;
import io.swagger.client.model.ProductResponseDto;

public class ProductListAdapter extends ArrayAdapter<ProductResponseDto> {
  private final List<ProductResponseDto> products;
  private final Activity activity;
  public ProgressBar mProgressBar;
  public TextView mTextViewAvailable;

  public ProductListAdapter(Activity activity, List<ProductResponseDto> products, ProgressBar progressBar, TextView textViewAvailable) {
    super(activity, R.layout.list_item_product, products);
    this.activity = activity;
    this.products = products;
    mProgressBar = progressBar;
    mTextViewAvailable = textViewAvailable;
    refreshData();
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      LayoutInflater inflater = activity.getLayoutInflater();
      view = inflater.inflate(R.layout.list_item_product, null);
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
      popupMenu.inflate(R.menu.menu_item_options);
      popupMenu.setOnMenuItemClickListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.updateMenuItem) {
          // Handle update menu item click
          showUpdateProductDialog(product);
          return true;

        } else {
          // Handle delete menu item click
          showDeleteProductDialog(product);
          return itemId == R.id.deleteMenuItem;
        }
      });
      popupMenu.show();
    });

    return view;
  }

  private void showUpdateProductDialog(ProductResponseDto product) {
    final Dialog dialog = new Dialog(getContext());
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_new_product);
    final TextView titleEditText = dialog.findViewById(R.id.dialog_title);
    titleEditText.setText("Modifier le produit");

    // retrieve user input
    final EditText nameEditText = dialog.findViewById(R.id.name_edit_text);
    final EditText purchasePriceEditText = dialog.findViewById(R.id.purchase_price_edit_text);
    final EditText sellingPriceEditText = dialog.findViewById(R.id.selling_price_edit_text);
    final EditText barcodeEditText = dialog.findViewById(R.id.barcode_edit_text);
    final Spinner spinnerTaxRate = dialog.findViewById(R.id.spinner_tax_rate);

    ProductRequestDto.TaxRateEnum[] taxRateEnums = ProductRequestDto.TaxRateEnum.values();
    ArrayAdapter<ProductRequestDto.TaxRateEnum> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            taxRateEnums);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerTaxRate.setAdapter(adapter);

    // Populate the input fields with the current user's data
    nameEditText.setText(product.getName());
    purchasePriceEditText.setText(product.getPurchasePrice().toString());
    sellingPriceEditText.setText(product.getSellingPriceExcludingTax().toString());
    barcodeEditText.setText(product.getBarCode());

    int index = IntStream.range(0, taxRateEnums.length)
            .filter(i -> product.getTaxRate().getValue().equals(taxRateEnums[i].getValue()))
            .findFirst()
            .orElse(-1);

    spinnerTaxRate.setSelection(index);

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

          // Perform API call to update this product
          UpdateProductTask updateProductTask = new UpdateProductTask();
          updateProductTask.execute(productRequestDto, product.getId());

          dialog.dismiss();
        }

        // Enable the button
        saveButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  public void refreshData() {
    new GetProductsTask().execute();
  }

  private void showDeleteProductDialog(ProductResponseDto product) {
    final Dialog dialog = new Dialog(getContext());
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_delete);
    TextView deleteDialogTitle = dialog.findViewById(R.id.dialog_title);
    TextView deleteDialogMessage = dialog.findViewById(R.id.dialog_message);
    deleteDialogTitle.setText("Confirmation de suppression du produit");
    deleteDialogMessage.setText("Voulez-vous vraiment supprimer ce produit ?");

    Button cancelButton = dialog.findViewById(R.id.button_cancel);
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    Button deleteButton = dialog.findViewById(R.id.button_delete);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Disable the button
        deleteButton.setEnabled(false);

        // Perform API call to delete this product
        new DeleteProductTask().execute(product.getId());
        dialog.dismiss();

        // Enable the button
        deleteButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private class GetProductsTask extends AsyncTask<Void, Void, List<ProductResponseDto>> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mProgressBar.setVisibility(View.VISIBLE);
      mTextViewAvailable.setVisibility(View.GONE);
    }

    @Override
    protected List<ProductResponseDto> doInBackground(Void... voids) {
      ProduitApi apiInstance = new ProduitApi();
      try {
        return apiInstance.getAllProducts();
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à ProductApi#listProducts");
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

        if (products.isEmpty()) {
          mTextViewAvailable.setVisibility(View.VISIBLE);
        }
      }
      mProgressBar.setVisibility(View.INVISIBLE);
      if (products == null) {
        mTextViewAvailable.setVisibility(View.VISIBLE);
      }
    }
  }

  public class UpdateProductTask extends AsyncTask<Object, Void, Void> {
    @Override
    protected Void doInBackground(Object... objects) {
      ProduitApi apiInstance = new ProduitApi();
      try {
        apiInstance.updateProduct((ProductRequestDto) objects[0], (Integer) objects[1]);
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à ProductApi#createProduct");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of products
      refreshData();
      Toast.makeText(activity, "Produit mis à jour avec succès", Toast.LENGTH_SHORT).show();
    }
  }

  private class DeleteProductTask extends AsyncTask<Integer, Void, Void> {
    @Override
    protected Void doInBackground(Integer... integers) {
      ProduitApi apiInstance = new ProduitApi();
      try {
        apiInstance.deleteProduct(integers[0]);
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à ProductApi#createProduct");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of products after deleting a product
      refreshData();
      Toast.makeText(activity, "Produit supprimé avec succès", Toast.LENGTH_SHORT).show();
    }
  }

}

