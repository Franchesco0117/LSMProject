package com.francisco.lsmproyect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class ResetPasswordActivity extends NightModeThemes {
    private Button btnPasswordReset;
    private TextInputLayout tILPasswordReset;
    private TextInputEditText etPasswordReset;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ResetPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        tILPasswordReset = findViewById(R.id.tlResetPassword);
        etPasswordReset = findViewById(R.id.etResetPassword);
        btnPasswordReset = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.ProgressBarResetPassword);

        authProfile = FirebaseAuth.getInstance();

        btnPasswordReset.setOnClickListener(view -> {
            String email = etPasswordReset.getText().toString();

            if (TextUtils.isEmpty(email)) {
                tILPasswordReset.setError("Email is required");
                tILPasswordReset.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tILPasswordReset.setError("Valid email is required");
                tILPasswordReset.requestFocus();
            } else {
                tILPasswordReset.setError(null);
                progressBar.setVisibility(View.VISIBLE);
                resetPassword(email);
            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    tILPasswordReset.setError(null);
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ResetPasswordActivity.this);
                    builder.setTitle("Recovery Email Sent");
                    builder.setMessage("An email with the reset password link has been sent to your email address. Please check your inbox and follow the instructions to reset your password.");

                    builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                            // Clear stack to prevent user coming back
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // Disable animation
                            finish();
                        }
                    });

                    builder.show();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Log.e(TAG, "onComplete: ", e);
                        Toast.makeText(getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthException e) {
                        Log.e(TAG, "onComplete: ", e);
                        Toast.makeText(getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: ", e);
                        Toast.makeText(getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                        tILPasswordReset.setError("User does not exist. Please check your email and try again.");
                        tILPasswordReset.requestFocus();
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}