package com.ergasia_android_teliki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText emailInput, passwordInput;
    Button loginBtn, registerBtn;
    ProgressBar progressBar;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase auth initialization
        mAuth = FirebaseAuth.getInstance();

        // Get controls from layout
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        // Set on click listener methods to buttons
        loginBtn.setOnClickListener(view -> login());
        registerBtn.setOnClickListener(view -> {
            // Open register activity on click
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login(){
        progressBar.setVisibility(View.VISIBLE);
        String email, password;
        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();

        // If email or password inputs are empty then the app doesn't try to login
        if (TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(), "Please enter your email.", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_LONG).show();
            return;
        }

        // Try to login, if login successful then open main activity
        // if unsuccessful then show login failed message
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();

                // Start main activity after login
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                // Different catch clauses for each error case
                // (e.g. No user with that email or password is wrong)
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException e) {
                    emailInput.setError(getString(R.string.user_not_found));
                    emailInput.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e){
                    passwordInput.setError(getString(R.string.error_invalid_password));
                    passwordInput.requestFocus();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Login failed with unknown error", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                }
            }

            progressBar.setVisibility(View.GONE);
        });
    }
}