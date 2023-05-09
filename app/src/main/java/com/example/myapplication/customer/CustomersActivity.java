package com.example.myapplication.customer;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.DrawerBaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCustomersBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.swagger.client.ApiException;
import io.swagger.client.api.CustomerApi;
import io.swagger.client.model.CustomerRequestDto;

public class CustomersActivity extends DrawerBaseActivity {
  ActivityCustomersBinding activityCustomersBinding;
  private ListView customerList;
  private CustomerListAdapter customerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityCustomersBinding = ActivityCustomersBinding.inflate(getLayoutInflater());
    setContentView(activityCustomersBinding.getRoot());

    customerList = findViewById(R.id.ListViewCustomers);

    // Create an instance of the CustomerListAdapter
    customerAdapter = new CustomerListAdapter(CustomersActivity.this, new ArrayList<>());

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
        String addressString = addressEditText.getText().toString().trim();
        String emailString = emailEditText.getText().toString().trim();
        String phoneString = phoneEditText.getText().toString().trim();

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(nameString)) {
          nameEditText.setError("Customer name is required");
          isValid = false;
        }

        if (TextUtils.isEmpty(emailString)) {
          emailEditText.setError("Customer email is required");
          isValid = false;
        }

        if (TextUtils.isEmpty(phoneString)) {
          phoneEditText.setError("Customer phone is required");
          isValid = false;
        } else if (phoneString.length() != 10) {
          phoneEditText.setError("Customer phone must be exactly 10 digits long");
          isValid = false;
        }

        if (TextUtils.isEmpty(addressString)) {
          addressEditText.setError("Customer address is required");
          isValid = false;
        }

        // If the input values are valid, save the Customer and dismiss the dialog
        if (isValid) {
          // Create the request body
          CustomerRequestDto customerRequestDto = new CustomerRequestDto();
          customerRequestDto.name(nameString);
          customerRequestDto.email(emailString);
          customerRequestDto.phone(phoneString);
          customerRequestDto.address(addressString);

          // Perform API call to save the newly created customer
          CreateCustomerTask createCustomerTask = new CreateCustomerTask(getApplicationContext());
          createCustomerTask.execute(customerRequestDto);

          dialog.dismiss();

          // Refresh the the customers ListView
          customerAdapter.refreshData();

          Toast.makeText(CustomersActivity.this, "Customer added successfully", Toast.LENGTH_SHORT).show();
        }

        // Enable the button
        saveButton.setEnabled(true);

      }
    });

    dialog.show();
  }

  private class CreateCustomerTask extends AsyncTask<CustomerRequestDto, Void, Void> {
    private final Context mContext;
    String errorMessage = "An error occurred while processing your request";

    public CreateCustomerTask(Context context) {
      mContext = context;
    }

    @Override
    protected Void doInBackground(CustomerRequestDto... customerRequestDtos) {
      CustomerApi apiInstance = new CustomerApi();
      try {
        apiInstance.createCustomer(customerRequestDtos[0]);
      } catch (ApiException e) {
        // Handle the error here
        // Retrieve the error message
        try {
          if (e.getResponseBody() != null) {
            JSONObject json = new JSONObject(e.getResponseBody());
            errorMessage = "Error : " + json.getString("message");
          }
        } catch (JSONException ex) {
          throw new RuntimeException(ex);
        }

        // Log the error details
        System.err.println("Exception when calling CustomerApi#createCustomer");
        System.out.println("ResponseBody : " + errorMessage);
        e.printStackTrace();

        // display toast with the error message
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
          }
        });


      }
      return null;
    }
  }


}