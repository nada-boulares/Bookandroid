package com.example.booknest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.booknest.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    // View binding to access UI elements easily
    private ActivityLoginBinding binding;

    // Firebase authentication object
    private FirebaseAuth firebaseAuth;

    // ProgressBar to indicate loading state
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize and set up the ProgressBar
        progressBar = binding.progressBar;  // Assuming progressBar is defined in the layout file
        progressBar.setVisibility(View.GONE); // Hide initially

        // Redirect user to the registration screen when they click on "noAccountTv"
        binding.noAccountTv.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Handle login button click to start the login validation
        binding.loginBtn.setOnClickListener(v -> validateData());
    }

    // Strings to hold email and password values
    private String email = "", password = "";

    // Function to validate input data before proceeding with login
    private void validateData() {
        // Get the entered email and password
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        // Validate the email pattern
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email pattern...!", Toast.LENGTH_SHORT).show();
        }
        // Check if the password is empty
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password...!", Toast.LENGTH_SHORT).show();
        } else {
            // If data is validated, proceed to log the user in
            loginUser();
        }
    }

    // Function to perform the login operation using Firebase authentication
    private void loginUser() {
        // Show the ProgressBar while logging in
        progressBar.setVisibility(View.VISIBLE);

        // Attempt to sign in the user with the provided email and password
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // If login is successful, check the type of user
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If login fails, hide the ProgressBar and show the error message
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Function to check if the authenticated user is an admin or a regular user
    private void checkUser() {
        // Show ProgressBar while checking user type
        progressBar.setVisibility(View.VISIBLE);

        // Get the currently authenticated user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Check the user type in the Realtime Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Hide the ProgressBar after fetching data
                        progressBar.setVisibility(View.GONE);

                        // Retrieve the user type from the database
                        String userType = "" + snapshot.child("userType").getValue();

                        // Check the user type and navigate to the appropriate dashboard
                        if (userType.equals("user")) {
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        } else if (userType.equals("admin")) {
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors when database reading is cancelled
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
