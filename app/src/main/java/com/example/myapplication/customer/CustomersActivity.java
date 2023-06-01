package com.example.myapplication.customer;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.DrawerBaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCustomersBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.swagger.client.ApiException;
import io.swagger.client.api.ClientApi;
import io.swagger.client.model.CustomerCreateDto;

public class CustomersActivity extends DrawerBaseActivity {
  ActivityCustomersBinding activityCustomersBinding;
  private ListView customerList;
  private CustomerListAdapter customerAdapter;
  private ProgressBar mProgressBar;
  private TextView textViewAvailable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityCustomersBinding = ActivityCustomersBinding.inflate(getLayoutInflater());
    setContentView(activityCustomersBinding.getRoot());

    customerList = findViewById(R.id.ListViewCustomers);
    mProgressBar = findViewById(R.id.progressBar);
    textViewAvailable = findViewById(R.id.textViewAvailable);

    // Create an instance of the CustomerListAdapter
    customerAdapter = new CustomerListAdapter(CustomersActivity.this, new ArrayList<>(), mProgressBar, textViewAvailable);

    customerList.setAdapter(customerAdapter);

    // Initialize your button and other views here
    Button addButton = findViewById(R.id.add_customer_button);

    // Set an OnClickListener on the "Add Customer" button
    addButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showAddCustomerDialog();
      }
    });
  }

  private void showAddCustomerDialog() {
    final Dialog dialog = new Dialog(this);
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_new_customer);

    // retrieve user input
    final EditText nameEditText = dialog.findViewById(R.id.name_edit_text);
    final EditText iceEditText = dialog.findViewById(R.id.ice_edit_text);
    final EditText emailEditText = dialog.findViewById(R.id.email_edit_text);
    final EditText phoneEditText = dialog.findViewById(R.id.phone_edit_text);
    final EditText addressEditText = dialog.findViewById(R.id.address_edit_text);


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
        String nameString = nameEditText.getText().toString().trim();
        String iceString = iceEditText.getText().toString().trim();
        String addressString = addressEditText.getText().toString().trim();
        String emailString = emailEditText.getText().toString().trim();
        String phoneString = phoneEditText.getText().toString().trim();

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(nameString)) {
          nameEditText.setError("Le nom du client est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(iceString)) {
          iceEditText.setError("Le ICE est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(phoneString)) {
          phoneEditText.setError("Le téléphone du client est requis");
          isValid = false;
        } else if (phoneString.length() != 10) {
          phoneEditText.setError("Le téléphone du client doit comporter exactement 10 chiffres");
          isValid = false;
        }

        if (TextUtils.isEmpty(addressString)) {
          addressEditText.setError("L'adresse du client est requise");
          isValid = false;
        }

        // If the input values are valid, save the Customer and dismiss the dialog
        if (isValid) {
          // Create the request body
          CustomerCreateDto customerCreateDto = new CustomerCreateDto();
          customerCreateDto.setIce(iceString);
          customerCreateDto.setName(nameString);
          customerCreateDto.setEmail(emailString);
          customerCreateDto.setPhone(phoneString);
          customerCreateDto.setAddress(addressString);

          // Perform API call to save the newly created customer
          new CreateCustomerTask().execute(customerCreateDto);

          dialog.dismiss();
        }

        // Enable the button
        saveButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private class CreateCustomerTask extends AsyncTask<CustomerCreateDto, Void, Void> {
    String errorMessage = "Une erreur s'est produite lors du traitement de votre demande";

    @Override
    protected Void doInBackground(CustomerCreateDto... customerRequestDtos) {
      ClientApi apiInstance = new ClientApi();
      try {
        apiInstance.createCustomer(customerRequestDtos[0]);
      } catch (ApiException e) {
        // Handle the error here
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Erreur : " + json.getString("message");
          }
        } catch (JSONException ex) {
          throw new RuntimeException(ex);
        }

        // Log the error details
        System.err.println("Exception lors de l'appel à CustomerApi#createCustomer");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

        // display toast with the error message
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
          }
        });

      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the the customers ListView
      customerAdapter.refreshData();
      Toast.makeText(CustomersActivity.this, "Client ajouté avec succès", Toast.LENGTH_SHORT).show();
    }
  }

}