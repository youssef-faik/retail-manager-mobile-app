package com.example.myapplication.user;

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
import com.example.myapplication.databinding.ActivityUsersBinding;

import java.util.ArrayList;

import io.swagger.client.ApiException;
import io.swagger.client.api.UtilisateurApi;
import io.swagger.client.model.UserCreateDto;

public class UsersActivity extends DrawerBaseActivity {
  ActivityUsersBinding activityUsersBinding;
  private ListView userList;
  private UserListAdapter userAdapter;
  private ProgressBar mProgressBar;
  private TextView textViewAvailable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityUsersBinding = ActivityUsersBinding.inflate(getLayoutInflater());
    setContentView(activityUsersBinding.getRoot());

    userList = findViewById(R.id.ListViewUsers);
    mProgressBar = findViewById(R.id.progressBar);
    textViewAvailable = findViewById(R.id.textViewAvailable);

    // Create an instance of the UserListAdapter
    userAdapter = new UserListAdapter(UsersActivity.this, new ArrayList<>(), mProgressBar, textViewAvailable);

    // Set the userAdapter for the ListView
    userList.setAdapter(userAdapter);

    // Initialize your button and other views here
    Button addButton = findViewById(R.id.add_button_button);

    // Set an OnClickListener on the "Add User" button
    addButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showAddUserDialog();
      }
    });

  }

  private void showAddUserDialog() {
    final Dialog dialog = new Dialog(this);
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_new_user);

    // retrieve user input
    final EditText firstNameEditText = dialog.findViewById(R.id.first_name_edit_text);
    final EditText lastNameEditText = dialog.findViewById(R.id.last_name_edit_text);
    final EditText emailEditText = dialog.findViewById(R.id.email_edit_text);
    final EditText passwordEditText = dialog.findViewById(R.id.password_edit_text);
    final Spinner spinnerRole = dialog.findViewById(R.id.spinner_user_role);

    ArrayAdapter<UserCreateDto.RoleEnum> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            UserCreateDto.RoleEnum.values());

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerRole.setAdapter(adapter);

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
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(firstName)) {
          firstNameEditText.setError("Le prénom de l'utilisateur est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(lastName)) {
          lastNameEditText.setError("Le nom de famille de l'utilisateur est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
          emailEditText.setError("L'adresse e-mail de l'utilisateur est requise");
          isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
          passwordEditText.setError("Le mot de passe de l'utilisateur est requis");
          isValid = false;
        }

        // If the input values are valid, save the user and dismiss the dialog
        if (isValid) {
          // create
          UserCreateDto.RoleEnum role = (UserCreateDto.RoleEnum) spinnerRole.getSelectedItem();

          // Create the request body
          UserCreateDto userCreateDto = new UserCreateDto();
          userCreateDto.firstName(firstName);
          userCreateDto.lastName(lastName);
          userCreateDto.email(email);
          userCreateDto.password(password);
          userCreateDto.role(role);

          // Perform API call to save the newly created user
          new CreateUserTask().execute(userCreateDto);
          dialog.dismiss();
        }

        // Enable the button
        saveButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private class CreateUserTask extends AsyncTask<UserCreateDto, Void, Void> {
    @Override
    protected Void doInBackground(UserCreateDto... userCreateDtos) {
      UtilisateurApi apiInstance = new UtilisateurApi();
      try {
        apiInstance.createUser(userCreateDtos[0]);
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à UserApi#createUser");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the the users ListView
      userAdapter.refreshData();
      Toast.makeText(UsersActivity.this, "Utilisateur ajouté avec succès", Toast.LENGTH_SHORT).show();
    }
  }

}