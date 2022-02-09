package com.ergasia_android_teliki.activities;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private ProgressBar progressBar;
    private SharedPreferences sp, roleSp;
    private SharedPreferences.Editor editor;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SensorManager sensorManager;
    private Sensor light;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SharedPreferences initialization
        sp = getSharedPreferences("Cart", Context.MODE_PRIVATE);
        roleSp = getSharedPreferences("Role", Context.MODE_PRIVATE);
        editor = sp.edit();

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get controls from layout
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();


        // Set on click listener methods to buttons
        loginBtn.setOnClickListener(view -> {
            login();
        });

        registerBtn.setOnClickListener(view -> {
            // Open register activity on click
            final Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
                    if (sensorEvent.values[0] > 15) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, light, SensorManager.SENSOR_DELAY_NORMAL);

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

                // Get user role from firestore
                // and save the role to shared preferences to use in the app
                db.collection("users").document(email).get()
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful() && task2.getResult() != null){
                                String role = (String) task2.getResult().get("role");
                                roleSp.edit().putString("role", role).apply();
                            }
                        });

                // Initialize cart clears the cart when a new login has been made
                editor.clear().apply();
                getSharedPreferences("Role", MODE_PRIVATE).edit().clear().apply();

                biometricPrompt.authenticate(promptInfo);

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