package com.example.myapplication.user;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

import java.util.List;
import java.util.stream.IntStream;

import io.swagger.client.ApiException;
import io.swagger.client.api.UtilisateurApi;
import io.swagger.client.model.UserDto;
import io.swagger.client.model.UserUpdateDto;

public class UserListAdapter extends ArrayAdapter<UserDto> {
  private final List<UserDto> users;
  private final Activity activity;
  public ProgressBar mProgressBar;
  public TextView mTextViewAvailable;

  public UserListAdapter(Activity activity, List<UserDto> users, ProgressBar progressBar, TextView textViewAvailable) {
    super(activity, R.layout.list_item_user, users);
    this.activity = activity;
    this.users = users;
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
      view = inflater.inflate(R.layout.list_item_user, null);
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
      popupMenu.inflate(R.menu.menu_item_options);
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
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_update_user);

    // retrieve user input
    final EditText firstNameEditText = dialog.findViewById(R.id.first_name_edit_text);
    final EditText lastNameEditText = dialog.findViewById(R.id.last_name_edit_text);
    final EditText emailEditText = dialog.findViewById(R.id.email_edit_text);
    final Spinner spinnerRole = dialog.findViewById(R.id.spinner_user_role);

    // Get a reference to the SharedPreferences object
    SharedPreferences prefs = activity.getSharedPreferences("myPrefs", MODE_PRIVATE);
    String currentUserEmail = prefs.getString("email", "");

    if (currentUserEmail.equalsIgnoreCase(user.getEmail())) {
      final TextView updateEmailWarningTextView = dialog.findViewById(R.id.updateEmailWarningTextView);
      updateEmailWarningTextView.setVisibility(View.VISIBLE);
    }

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
          firstNameEditText.setError("Le prénom de l'utilisateur est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(lastNameString)) {
          lastNameEditText.setError("Le nom de famille de l'utilisateur est requis");
          isValid = false;
        }

        if (TextUtils.isEmpty(emailString)) {
          emailEditText.setError("L'e-mail de l'utilisateur est requis");
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

          if (currentUserEmail.equalsIgnoreCase(user.getEmail()) && !userUpdateDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("token", "");
            editor.putString("role", "");
            editor.apply();

            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            activity.finish();
          }

          dialog.dismiss();
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
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_listview_background);
    dialog.setContentView(R.layout.dialog_delete);
    TextView deleteDialogTitle = dialog.findViewById(R.id.dialog_title);
    TextView deleteDialogMessage = dialog.findViewById(R.id.dialog_message);
    deleteDialogTitle.setText("Confirmation de suppression de l'utilisateur");
    deleteDialogMessage.setText("Voulez-vous vraiment supprimer cet utilisateur ?");

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
        dialog.dismiss();

        // Enable the button
        deleteButton.setEnabled(true);
      }
    });

    dialog.show();
  }

  private class GetUsersTask extends AsyncTask<Void, Void, List<UserDto>> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mProgressBar.setVisibility(View.VISIBLE);
      mTextViewAvailable.setVisibility(View.GONE);
    }

    @Override
    protected List<UserDto> doInBackground(Void... voids) {
      UtilisateurApi apiInstance = new UtilisateurApi();
      try {
        return apiInstance.getAllUsers();
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à UserApi#listUsers");
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

        if (users.isEmpty()) {
          mTextViewAvailable.setVisibility(View.VISIBLE);
        }
      }
      mProgressBar.setVisibility(View.INVISIBLE);
      if (users == null) {
        mTextViewAvailable.setVisibility(View.VISIBLE);
      }
    }
  }

  public class UpdateUserTask extends AsyncTask<Object, Void, Void> {
    @Override
    protected Void doInBackground(Object... objects) {
      UtilisateurApi apiInstance = new UtilisateurApi();
      try {
        apiInstance.updateUser((UserUpdateDto) objects[0], (Integer) objects[1]);
      } catch (ApiException e) {
        System.err.println("Exception lors de l'appel à UserApi#updateUser");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of products after updating a user
      refreshData();
      Toast.makeText(activity, "Utilisateur mis à jour avec succès", Toast.LENGTH_SHORT).show();
    }
  }

  private class DeleteUserTask extends AsyncTask<Integer, Void, Void> {
    boolean errorOccurred = false;

    @Override
    protected Void doInBackground(Integer... integers) {
      UtilisateurApi apiInstance = new UtilisateurApi();
      try {
        apiInstance.deleteUser(integers[0]);
      } catch (ApiException e) {
        errorOccurred = true;
        System.err.println("Exception lors de l'appel à UserApi#deleteUser");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // Refresh the list of users after deleting a user
      if (!errorOccurred) {
        refreshData();
        Toast.makeText(activity, "Utilisateur supprimé avec succès", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(activity, "Nous avons rencontré une erreur lors du traitement de votre demande.", Toast.LENGTH_SHORT).show();
      }
    }
  }

}

