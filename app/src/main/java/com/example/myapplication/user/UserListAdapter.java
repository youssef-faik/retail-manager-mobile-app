package com.example.myapplication.user;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.example.myapplication.R;

import java.util.List;
import java.util.stream.IntStream;

import io.swagger.client.ApiException;
import io.swagger.client.api.UserApi;
import io.swagger.client.model.UserDto;
import io.swagger.client.model.UserUpdateDto;

public class UserListAdapter extends ArrayAdapter<UserDto> {
  private final List<UserDto> users;
  private final Activity activity;

  public UserListAdapter(Activity activity, List<UserDto> users) {
    super(activity, R.layout.user_list_item, users);
    this.activity = activity;
    this.users = users;
    refreshData();
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      LayoutInflater inflater = activity.getLayoutInflater();
      view = inflater.inflate(R.layout.user_list_item, null);
    }

    // Get the user at the current position
    UserDto user = users.get(position);

    // Set the user full name, email, and role in the corresponding TextViews
    TextView nameTextView = view.findViewById(R.id.nameTextView);
    nameTextView.setText(String.format("%s %s", user.getFirstName(), user.getLastName().toUpperCase()));

    TextView emailTextView = view.findViewById(R.id.emailTextView);
    emailTextView.setText(user.getEmail());

    TextView roleTextView = view.findViewById(R.id.roleTextView);
    roleTextView.setText(user.getRole().toString());

    // Set the options button click listener to show the menu
    Button optionsButton = view.findViewById(R.id.optionsButton);
    optionsButton.setOnClickListener(v -> {
      PopupMenu popupMenu = new PopupMenu(activity, v);
      popupMenu.inflate(R.menu.item_options_menu);
      popupMenu.setOnMenuItemClickListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.updateMenuItem) {
          // Handle update menu item click
          showUpdateUserDialog(user);
          return true;

        } else {
          // Handle delete menu item click
          showDeleteUserDialog(user);
          return itemId == R.id.deleteMenuItem;
        }
      });
      popupMenu.show();
    });

    return view;
  }

  private void showUpdateUserDialog(UserDto user) {
    final Dialog dialog = new Dialog(getContext());
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.listview_background);
    dialog.setContentView(R.layout.update_user_dialog);

    // retrieve user input
    final EditText firstNameEditText = dialog.findViewById(R.id.first_name_edit_text);
    final EditText lastNameEditText = dialog.findViewById(R.id.last_name_edit_text);
    final EditText emailEditText = dialog.findViewById(R.id.email_edit_text);
    final Spinner spinnerRole = dialog.findViewById(R.id.spinner_user_role);

    UserDto.RoleEnum[] roleEnums = UserDto.RoleEnum.values();
    ArrayAdapter<UserDto.RoleEnum> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            roleEnums);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerRole.setAdapter(adapter);

    // Populate the input fields with the current user's data
    firstNameEditText.setText(user.getFirstName());
    lastNameEditText.setText(user.getLastName());
    emailEditText.setText(user.getEmail());

    int index = IntStream.range(0, roleEnums.length)
            .filter(i -> user.getRole().getValue().equals(roleEnums[i].getValue()))
            .findFirst()
            .orElse(-1);

    spinnerRole.setSelection(index);

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
        String firstNameString = firstNameEditText.getText().toString().trim();
        String lastNameString = lastNameEditText.getText().toString().trim();
        String emailString = emailEditText.getText().toString().trim();

        // Validate the input values
        boolean isValid = true;
        if (TextUtils.isEmpty(firstNameString)) {
          firstNameEditText.setError("User First Name is required");
          isValid = false;
        }

        if (TextUtils.isEmpty(lastNameString)) {
          lastNameEditText.setError("User Last Name is required");
          isValid = false;
        }

        if (TextUtils.isEmpty(emailString)) {
          emailEditText.setError("User Email is required");
          isValid = false;
        }

        // If the input values are valid, save the user and dismiss the dialog
        if (isValid) {
          // create
          UserUpdateDto.RoleEnum role = UserUpdateDto.RoleEnum.fromValue(spinnerRole.getSelectedItem().toString());

          // Create the request body
          UserUpdateDto userUpdateDto = new UserUpdateDto();
          userUpdateDto.firstName(firstNameString);
          userUpdateDto.lastName(lastNameString);
          userUpdateDto.email(emailString);
          userUpdateDto.role(role);

          // Perform API call to update this user
          UpdateUserTask updateUserTask = new UpdateUserTask();
          updateUserTask.execute(userUpdateDto, user.getId());

          dialog.dismiss();

          // Refresh the the products ListView
          refreshData();

          Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
        }

        // Enable the button
        saveButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  public void refreshData() {
    new GetUsersTask().execute();
  }

  private void showDeleteUserDialog(UserDto user) {
    final Dialog dialog = new Dialog(getContext());
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.listview_background);
    dialog.setContentView(R.layout.delete_product);
    TextView deleteDialogTitle = dialog.findViewById(R.id.dialog_title);
    TextView deleteDialogMessage = dialog.findViewById(R.id.dialog_message);
    deleteDialogTitle.setText("Delete user confirmation");
    deleteDialogMessage.setText("Do you really want to delete this user?");

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

        // Perform API call to delete this user
        new DeleteUserTask().execute(user.getId());
        Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

        // Enable the button
        deleteButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private class GetUsersTask extends AsyncTask<Void, Void, List<UserDto>> {
    @Override
    protected List<UserDto> doInBackground(Void... voids) {
      UserApi apiInstance = new UserApi();
      try {
        return apiInstance.getAllUsers();
      } catch (ApiException e) {
        System.err.println("Exception when calling UserApi#listUsers");
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPostExecute(List<UserDto> users) {
      if (users != null) {
        clear();
        addAll(users);
        notifyDataSetChanged();
      }
    }
  }

  public class UpdateUserTask extends AsyncTask<Object, Void, Void> {
    @Override
    protected Void doInBackground(Object... objects) {
      UserApi apiInstance = new UserApi();
      try {
        apiInstance.updateUser((UserUpdateDto) objects[0], (Integer) objects[1]);
      } catch (ApiException e) {
        System.err.println("Exception when calling UserApi#updateUser");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of products after updating a user
      refreshData();
    }
  }

  private class DeleteUserTask extends AsyncTask<Integer, Void, Void> {
    @Override
    protected Void doInBackground(Integer... integers) {
      UserApi apiInstance = new UserApi();
      try {
        apiInstance.deleteUser(integers[0]);
      } catch (ApiException e) {
        System.err.println("Exception when calling UserApi#deleteUser");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of users after deleting a user
      refreshData();
    }
  }

}
