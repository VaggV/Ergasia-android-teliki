package com.ergasia_android_teliki;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText emailInput, passwordInput;
    ProgressBar progressBar;
    Button registerBtn, backBtn;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase auth initialization
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
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerNewUser(){
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

        // Register user to firebase, if successful then go back to login activity
        // else show an error message
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(getApplicationContext(), "Registration successfull!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                // Different catch clause for each register error
                try {
                    throw task.getException();
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
                    Toast.makeText(getApplicationContext(), "Registration failed with unknown error", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }
}