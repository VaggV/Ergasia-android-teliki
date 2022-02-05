package com.ergasia_android_teliki.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private ProgressBar progressBar;
    private Button registerBtn, backBtn;
    private FirebaseFirestore db;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase initialization
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get controls from layout
        emailInput = findViewById(R.id.email2);
        passwordInput = findViewById(R.id.passwd);
        registerBtn = findViewById(R.id.btnregister);
        progressBar = findViewById(R.id.progressBar2);
        backBtn = findViewById(R.id.backtologin);

        // Set on click listener methods to register and back buttons
        registerBtn.setOnClickListener(view -> registerNewUser());
        backBtn.setOnClickListener(view -> {
            // Go back to login activity
            final Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerNewUser(){
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
        // Register user to firebase, if successful then go back to login activity
        // else show an error message
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(getApplicationContext(), getString(R.string.registration_successful), Toast.LENGTH_LONG).show();
                final Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                // When a new user has been registered we also add him in firestore
                // so we can specify a role (consumer is the default, and from
                // firebase console we can change it to "store")
                Map<String, Object> user = new HashMap<>();
                user.put("role", "consumer");
                db.collection("users").document(email).set(user).addOnSuccessListener(unused -> {
                    Log.d(TAG, "User added to firestore with id: " + email);
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding new user to firestore.", e);
                });

                startActivity(intent);
            } else {
                // Different catch clause for each register error
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    passwordInput.setError(getString(R.string.error_weak_password));
                    passwordInput.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    emailInput.setError(getString(R.string.error_invalid_email));
                    emailInput.requestFocus();
                } catch (FirebaseAuthUserCollisionException e){
                    emailInput.setError(getString(R.string.error_user_exists));
                    emailInput.requestFocus();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), getString(R.string.register_failed_unknown), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }
}