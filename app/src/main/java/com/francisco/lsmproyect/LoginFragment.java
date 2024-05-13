package com.francisco.lsmproyect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LoginFragment extends Fragment {

    private TextInputEditText etLoginEmail, etLoginPassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private TextInputLayout tILoginEmail, tILonginPassword;
    private static final String TAG = "LoginFragment";
    TextView tvForgotPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        progressBar = view.findViewById(R.id.progressLoginBar);

        authProfile = FirebaseAuth.getInstance();

        Button btnLogin = view.findViewById(R.id.btnLogin);

        tILoginEmail = view.findViewById(R.id.tlLoginEmail);
        tILonginPassword = view.findViewById(R.id.tlLoginPassword);

        CheckBox cbHideLoginPassword = view.findViewById(R.id.cbHidePassword);

        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);

        cbHideLoginPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Muestra la contraseña
                    etLoginPassword.setTransformationMethod(null);
                } else {
                    // Oculta la contraseña
                    etLoginPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = etLoginEmail.getText().toString();
                String textPassword = etLoginPassword.getText().toString();

                if (checkLoginConditions(textEmail, textPassword)) {
                    progressBar.setVisibility(View.VISIBLE);
                    LoginUser(textEmail, textPassword);
                }
            }
        });

        // Reset password
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ResetPasswordActivity.class));
            }
        });

        return view;
    }

    private boolean checkLoginConditions(String textEmail, String textPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(textEmail)) {
            tILoginEmail.setError("Email is required");
            tILoginEmail.requestFocus();
            isValid = false;
        } else {
            tILoginEmail.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
            tILoginEmail.setError("Valid email is required");
            tILoginEmail.requestFocus();
            isValid = false;
        } else {
            tILoginEmail.setError(null);
        }

        if (TextUtils.isEmpty(textPassword)) {
            tILonginPassword.setError("Password is required");
            tILonginPassword.requestFocus();
            isValid = false;
        } else {
            tILonginPassword.setError(null);
        }

        return isValid;
    }

    private void LoginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    // Get instance of the current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    // Check if the user is verified before access
                    if (firebaseUser.isEmailVerified()) {
                        Log.d(TAG, "User is verified, you are logged in now");
                        // Toast.makeText(getActivity().getApplicationContext(), "You are logged in now", Toast.LENGTH_SHORT).show();

                        // Open main app
                        startActivity(new Intent(getActivity(), MenuActivity.class));
                        getActivity().finish();

                    } else {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        tILoginEmail.setError("User does not exits or is no longer valid.");
                        tILoginEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        tILoginEmail.setError("Invalid credentials. Kindly, check and re-enter");
                        tILoginEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        tILoginEmail.setError("User does not exits or Invalid credentials. Kindly, check and re-enter");
                        tILoginEmail.requestFocus();
                    }

                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        // Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Email Not Verify");
        builder.setMessage("Please verify your email now. You can not login without email verification");

        // Open email app if user clicks Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To open email app in a new window
                startActivity(intent);
            }
        });

        // Create alertDialog
        AlertDialog alertDialog = builder.create();

        // Show the alertDialog
        alertDialog.show();
    }

    // Check if user is already logged in
    @Override
    public void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null) {
            // Toast.makeText(getActivity().getApplicationContext(), "Already logged in", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onStart: Already logged in");

            startActivity(new Intent(getActivity(), MenuActivity.class));
            getActivity().finish();

        } else {
            Log.d(TAG, "onStart: Not logged in");
            // Toast.makeText(getActivity(), "Not logged in", Toast.LENGTH_SHORT).show();
        }

    }
}