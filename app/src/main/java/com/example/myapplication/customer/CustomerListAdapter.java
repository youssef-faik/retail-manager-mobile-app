package com.example.myapplication.customer;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.example.myapplication.R;

import java.util.List;

import io.swagger.client.ApiException;
import io.swagger.client.api.ClientApi;
import io.swagger.client.model.CustomerResponseDto;
import io.swagger.client.model.CustomerUpdateDto;

public class CustomerListAdapter extends ArrayAdapter<CustomerResponseDto> {
  private final List<CustomerResponseDto> customers;
  private final Activity activity;
  public ProgressBar mProgressBar;
  public TextView mTextViewAvailable;

  public CustomerListAdapter(Activity activity, List<CustomerResponseDto> customers, ProgressBar progressBar, TextView textViewAvailable) {
    super(activity, R.layout.list_item_customer, customers);
    this.activity = activity;
    this.customers = customers;
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
      view = inflater.inflate(R.layout.list_item_customer, null);
    }

    // Get the customer at the current position
    CustomerResponseDto customer = customers.get(position);

    // Set the customer name, phone, and email in the corresponding TextViews
    TextView nameTextView = view.findViewById(R.id.nameTextView);
    nameTextView.setText(customer.getName());

    TextView phone = view.findViewById(R.id.phoneTextView);
    phone.setText(customer.getPhone());

    TextView emailTextView = view.findViewById(R.id.iceTextView);
    emailTextView.setText(customer.getIce());

    // Set the options button click listener to show the menu
    Button optionsButton = view.findViewById(R.id.optionsButton);
    optionsButton.setOnClickListener(v -> {
      PopupMenu popupMenu = new PopupMenu(activity, v);
      popupMenu.inflate(R.menu.menu_item_options);
      popupMenu.setOnMenuItemClickListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.updateMenuItem) {
          // Handle update menu item click
          showUpdateCustomerDialog(customer);
          return true;
        } else {
          // Handle delete menu item click
          showDeleteCustomerDialog(customer);
          return itemId == R.id.deleteMenuItem;
        }
      });
      popupMenu.show();
    });

    return view;
  }

  private void showUpdateCustomerDialog(CustomerResponseDto customer) {
    final Dialog dialog = new Dialog(getContext());
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_new_customer);
    final TextView titleEditText = dialog.findViewById(R.id.dialog_title);
    titleEditText.setText("Update Customer");

    // retrieve user input
    final EditText nameEditText = dialog.findViewById(R.id.name_edit_text);
    final EditText emailEditText = dialog.findViewById(R.id.email_edit_text);
    final EditText phoneEditText = dialog.findViewById(R.id.phone_edit_text);
    final EditText addressEditText = dialog.findViewById(R.id.address_edit_text);

    // Populate the input fields with the current customer's data
    nameEditText.setText(customer.getName());
    emailEditText.setText(customer.getEmail());
    phoneEditText.setText(customer.getPhone());
    addressEditText.setText(customer.getAddress());

    final EditText iceEditText = dialog.findViewById(R.id.ice_edit_text);
    final TextView iceTextView = dialog.findViewById(R.id.ice_text_view);
    iceEditText.setVisibility(View.GONE);
    iceTextView.setVisibility(View.GONE);

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
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(name)) {
          nameEditText.setError("Customer name is required");
          isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
          phoneEditText.setError("Customer phone is required");
          isValid = false;
        } else if (phone.length() != 10) {
          phoneEditText.setError("Customer phone must be exactly 10 digits long");
          isValid = false;
        }

        if (TextUtils.isEmpty(address)) {
          addressEditText.setError("Customer address is required");
          isValid = false;
        }

        // If the input values are valid, save the customer and dismiss the dialog
        if (isValid) {
          // Create the request body
          CustomerUpdateDto customerUpdateDto = new CustomerUpdateDto();
          customerUpdateDto.name(name);
          customerUpdateDto.email(email);
          customerUpdateDto.phone(phone);
          customerUpdateDto.address(address);

          // Perform API call to update this customer
          UpdateCustomerTask updateCustomerTask = new UpdateCustomerTask();
          updateCustomerTask.execute(customerUpdateDto, customer.getIce());

          dialog.dismiss();
        }

        // Enable the button
        saveButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private void showDeleteCustomerDialog(CustomerResponseDto customer) {
    final Dialog dialog = new Dialog(getContext());
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_delete);
    TextView deleteDialogTitle = dialog.findViewById(R.id.dialog_title);
    TextView deleteDialogMessage = dialog.findViewById(R.id.dialog_message);
    deleteDialogTitle.setText("Delete Customer Confirmation");
    deleteDialogMessage.setText("Do you really want to delete this customer?");

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

        // Perform API call to delete this customer
        new DeleteCustomerTask().execute(customer.getIce());
        Toast.makeText(getContext(), "Customer deleted successfully", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

        // Enable the button
        deleteButton.setEnabled(true);

      }
    });

    dialog.show();
  }

  public void refreshData() {
    new CustomerListAdapter.GetCustomersTask().execute();
  }

  private class GetCustomersTask extends AsyncTask<Void, Void, List<CustomerResponseDto>> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mProgressBar.setVisibility(View.VISIBLE);
      mTextViewAvailable.setVisibility(View.GONE);
    }

    @Override
    protected List<CustomerResponseDto> doInBackground(Void... voids) {
      ClientApi apiInstance = new ClientApi();
      try {
        return apiInstance.getAllCustomers();
      } catch (ApiException e) {
        System.err.println("Exception when calling CustomerApi#listCustomers");
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPostExecute(List<CustomerResponseDto> customers) {
      if (customers != null) {
        clear();
        addAll(customers);
        notifyDataSetChanged();

        if (customers.isEmpty()) {
          mTextViewAvailable.setVisibility(View.VISIBLE);
        }
      }
      mProgressBar.setVisibility(View.INVISIBLE);
      if (customers == null) {
        mTextViewAvailable.setVisibility(View.VISIBLE);
      }
    }
  }

  private class UpdateCustomerTask extends AsyncTask<Object, Void, Void> {
    @Override
    protected Void doInBackground(Object... objects) {
      ClientApi apiInstance = new ClientApi();
      try {
        apiInstance.updateCustomer((CustomerUpdateDto) objects[0], (String) objects[1]);
      } catch (ApiException e) {
        System.err.println("Exception when calling CustomerApi#updateCustomer");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of customers after updating a customer
      refreshData();
      Toast.makeText(activity, "Customer updated successfully", Toast.LENGTH_SHORT);
    }
  }

  private class DeleteCustomerTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
      ClientApi apiInstance = new ClientApi();
      try {
        apiInstance.deleteCustomer(strings[0]);
      } catch (ApiException e) {
        System.err.println("Exception when calling CustomerApi#createCustomer");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of customers after deleting a customer
      refreshData();
      Toast.makeText(activity, "Customer deleted successfully", Toast.LENGTH_SHORT).show();
    }
  }

}
