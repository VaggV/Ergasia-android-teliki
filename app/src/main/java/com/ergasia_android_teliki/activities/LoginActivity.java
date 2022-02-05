package com.ergasia_android_teliki.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ergasia_android_teliki.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private ProgressBar progressBar;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SharedPreferences initialization
        sp = getSharedPreferences("Cart", Context.MODE_PRIVATE);
        editor = sp.edit();

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
            final Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login(){
        String email, password;
        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();

        // If email or password inputs are empty then the app doesn't try to login
        if (TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_password), Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        // Try to login, if login successful then open main activity
        // if unsuccessful then show login failed message
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(getApplicationContext(), getString(R.string.login_successful), Toast.LENGTH_LONG).show();

                // Start main activity after login
                final Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                // Initialize cart clears the cart when a new login has been made
                editor.clear().apply();
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
                    Toast.makeText(getApplicationContext(), getString(R.string.please_enter_password), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                }
            }

            progressBar.setVisibility(View.GONE);
        });
    }
}