package com.francisco.lsmproyect;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends NightModeThemes {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView tvAuthEmail;
    private TextInputEditText etAuthEmail, etAuthPassword, etNewEmail;
    private TextInputLayout tilAuthEmail, tilAuthPassword, tilNewEmail;
    private String userOldEmail, userPassword, userNewEmail;
    private Button btnUpdateEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        progressBar = findViewById(R.id.UpdateEmailProgressBar);

        // Text Inputs Edit texts
        tvAuthEmail = findViewById(R.id.tvConfirmAuthenticationHeader);
        etAuthEmail = findViewById(R.id.etAuthenticationEmail);
        etAuthPassword = findViewById(R.id.etAuthenticationPassword);
        etNewEmail = findViewById(R.id.etUpdateEmail);

        // Text Inputs Layouts
        tilAuthEmail = findViewById(R.id.tlAuthenticationEmail);
        tilAuthPassword = findViewById(R.id.tlAuthenticationPassword);
        tilNewEmail = findViewById(R.id.tlUpdateEmail);

        // Buttons
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnUpdateEmail.setEnabled(false);
        tilNewEmail.setEnabled(false);

        CheckBox cbHidePassword = findViewById(R.id.cbHideEmailAuthPassword);

        cbHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // Muestra la contraseña
                    etAuthPassword.setTransformationMethod(null);
                } else {
                    // Oculta la contraseña
                    etAuthPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Set old email ID on TextView
        userOldEmail = firebaseUser.getEmail();
        etAuthEmail.setText(userOldEmail);

        if (firebaseUser.equals("")) {
            Toast.makeText(getApplicationContext(), "Something went wrong, user's details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate();
        }
    }

    private void reAuthenticate() {
        Button btnVerifyEmail = findViewById(R.id.btnAuthenticateEmail);
        btnVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPassword = etAuthPassword.getText().toString();
                if (TextUtils.isEmpty(userPassword)) {
                    tilAuthPassword.setError("Password is required");
                    tilAuthPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    tilAuthPassword.setError(null);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Authentication successful", Toast.LENGTH_SHORT).show();

                                // Set TextView to show that user is authenticated
                                tvAuthEmail.setText("User is authenticated");

                                // Enable text inputs
                                etNewEmail.setEnabled(true);
                                tilNewEmail.setEnabled(true);
                                btnUpdateEmail.setEnabled(true);

                                // Disable Password
                                etAuthEmail.setEnabled(false);
                                tilAuthPassword.setEnabled(false);
                                btnVerifyEmail.setEnabled(false);

                                btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail = etNewEmail.getText().toString();

                                        if (TextUtils.isEmpty(userNewEmail)) {
                                            tilNewEmail.setError("New email is required");
                                            tilNewEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            tilNewEmail.setError("New email is invalid");
                                            tilNewEmail.requestFocus();
                                        } else if (userNewEmail.matches(userOldEmail)) {
                                            tilNewEmail.setError("New email cannot be same as old email");
                                            tilNewEmail.requestFocus();
                                        } else {
                                            tilNewEmail.setError(null);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Verify Email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(getApplicationContext(), "Verification email sent", Toast.LENGTH_SHORT).show();

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main, new ProfileFragment());
                    fragmentTransaction.commit();
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UpdateEmailActivity", e.getMessage());
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}