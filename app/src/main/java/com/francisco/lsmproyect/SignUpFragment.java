package com.francisco.lsmproyect;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {

    private TextInputEditText etRegisterName, etRegisterUsername, etRegisterDoB, etRegisterPhone, etRegisterEmail,
            etRegisterPassword, etRegisterPasswordConfirm, etLocation, etBio;

    private TextInputLayout tILRegisterName, tILRegisterUsername, tILRegisterDoB, tILRegisterPhone, tILRegisterEmail,
            tILRegisterPassword, tILRegisterPasswordConfirm, tILocation, tILBio;
    private ProgressBar progressBar;

    private static final String TAG = "SignUpFragment";

    private DatePickerDialog picker;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        etRegisterName = view.findViewById(R.id.etName);
        etRegisterUsername = view.findViewById(R.id.etUsername);
        etRegisterDoB = view.findViewById(R.id.etDateOfBirth);
        etRegisterPhone = view.findViewById(R.id.etTelephoneNumber);
        etRegisterEmail = view.findViewById(R.id.etSignUpEmail);
        etRegisterPassword = view.findViewById(R.id.etSignUpPassword);
        etRegisterPasswordConfirm = view.findViewById(R.id.etSignUpPasswordConfirm);
        progressBar = view.findViewById(R.id.progressBar);


        etLocation = view.findViewById(R.id.etSignUpLocation);
        etBio = view.findViewById(R.id.etSignUpBio);

        // DatePicker on TextInputEditText
        etRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // DatePicker Dialog
                picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        etRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);

                picker.show();
            }
        });

        tILRegisterName = view.findViewById(R.id.tlName);
        tILRegisterUsername = view.findViewById(R.id.tlUsername);
        tILRegisterDoB = view.findViewById(R.id.tlDateOfBirth);
        tILRegisterPhone = view.findViewById(R.id.tlTelephoneNumber);
        tILRegisterEmail = view.findViewById(R.id.tlSignUpEmail);
        tILRegisterPassword = view.findViewById(R.id.tlSignUpPassword);
        tILRegisterPasswordConfirm = view.findViewById(R.id.tlSignUpPasswordConfirm);

        tILocation = view.findViewById(R.id.tlLocation);
        tILBio = view.findViewById(R.id.tlSignUpBio);

        CheckBox cbHidePassword = view.findViewById(R.id.cbHidePassword);
        CheckBox cbHidePasswordConfirmation = view.findViewById(R.id.cbHidePasswordConfirmation);

        Button btnRegister = view.findViewById(R.id.btnSignUp);
        
        cbHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Muestra la contraseña
                    etRegisterPassword.setTransformationMethod(null);
                } else {
                    // Oculta la contraseña
                    etRegisterPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        cbHidePasswordConfirmation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    etRegisterPasswordConfirm.setTransformationMethod(null);
                } else {
                    etRegisterPasswordConfirm.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtener los datos ingresados
                String textName = etRegisterName.getText().toString();
                String textUsername = etRegisterUsername.getText().toString();
                String textDoB = etRegisterDoB.getText().toString();
                String textPhone = etRegisterPhone.getText().toString();
                String textEmail = etRegisterEmail.getText().toString();
                String textPassword = etRegisterPassword.getText().toString();
                String textPasswordConfirm = etRegisterPasswordConfirm.getText().toString();

                String textLocation = etLocation.getText().toString();
                String textBio = etBio.getText().toString();

                // Validar numero de celular usando Matcher and Pattern (Regular Expression)
                String mobileRegex = "^(\\+?52)?(1)?([5-7][0-9]{9})$";
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textPhone);

                int currentLevel = 0, nextLevel = 1, coursesCompleted = 0, streak = 0, totalExperience = 0, levelProgress = 0;

                if (checkRegistrationConditions(textName, textUsername, textDoB, textPhone, textEmail, textPassword, textPasswordConfirm,
                        mobileMatcher, textLocation, textBio)) {
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textName, textUsername, textDoB, textPhone, textEmail, textPassword,
                            textLocation, textBio, currentLevel, nextLevel, coursesCompleted, streak, totalExperience, levelProgress);
                }
            }
        });

        return view;
    }

    private boolean checkRegistrationConditions(String textName, String textUsername, String textDoB,
                                                String textPhone, String textEmail, String textPassword, String textPasswordConfirm,
                                                Matcher mobileMatcher, String textLocation, String textBio) {

        boolean isValid = true;

        if (TextUtils.isEmpty(textName)) {
            tILRegisterName.setError("Name is required");
            tILRegisterName.requestFocus();
            isValid = false;
        } else {
            tILRegisterName.setError(null);
        }

        if (TextUtils.isEmpty(textUsername)) {
            tILRegisterUsername.setError("Username is required");
            tILRegisterUsername.requestFocus();
            isValid = false;
        } else {
            tILRegisterUsername.setError(null);
        }

        if (TextUtils.isEmpty(textDoB)) {
            tILRegisterDoB.setError("Date of Birth is required");
            tILRegisterDoB.requestFocus();
            isValid = false;
        } else {
            tILRegisterDoB.setError(null);
        }

        if (TextUtils.isEmpty(textPhone)) {
            tILRegisterPhone.setError("Phone number is required");
            tILRegisterPhone.requestFocus();
            isValid = false;
        } else {
            tILRegisterPhone.setError(null);
        }

        if (!mobileMatcher.find()) {
            tILRegisterPhone.setError("Phone number is not valid");
            tILRegisterPhone.requestFocus();
            isValid = false;
        } else {
            tILRegisterPhone.setError(null);
        }

        if (TextUtils.isEmpty(textEmail)) {
            tILRegisterEmail.setError("Email is required");
            tILRegisterEmail.requestFocus();
            isValid = false;
        } else {
            tILRegisterEmail.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
            tILRegisterEmail.setError("Valid email is required");
            tILRegisterEmail.requestFocus();
            isValid = false;
        } else {
            tILRegisterEmail.setError(null);
        }

        if (TextUtils.isEmpty(textPassword)) {
            tILRegisterPassword.setError("Password is required");
            tILRegisterPassword.requestFocus();
            isValid = false;
        } else {
            tILRegisterPassword.setError(null);
        }

        if (textPassword.length() < 8) {
            tILRegisterPassword.setError("Password too weak");
            tILRegisterPassword.requestFocus();
            isValid = false;
        } else {
            tILRegisterPassword.setError(null);
        }

        if (TextUtils.isEmpty(textPasswordConfirm) || !textPassword.equals(textPasswordConfirm)) {
            tILRegisterPasswordConfirm.setError("Confirm your password and make sure it matches your password");
            tILRegisterPasswordConfirm.requestFocus();
            isValid = false;
        } else {
            tILRegisterPasswordConfirm.setError(null);
        }

        if (TextUtils.isEmpty(textLocation)) {
            tILocation.setError("Location is required");
            tILocation.requestFocus();
            isValid = false;
        } else {
            tILocation.setError(null);
        }

        if (TextUtils.isEmpty(textBio)) {
            tILBio.setError("Bio is required");
            tILBio.requestFocus();
            isValid = false;
        } else {
            tILBio.setError(null);
        }

        return isValid;
    }

    // Regsitrar usuario con las credenciales dadas
    private void registerUser(String textName, String textUsername, String textDoB, String textPhone,
                              String textEmail, String textPassword, String textLocation, String textBio, int currentLevel,
                              int nextLevel, int coursesCompleted, int streak, int totalExperience, int levelProgress) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Crear usuario
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // Usuario creado
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // Update Display name of user
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    // Ingresar los datos del usuario al Firebase Realtime Database
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textUsername, textDoB, textPhone, textLocation, textBio,
                            currentLevel, nextLevel, coursesCompleted, streak, totalExperience, levelProgress);

                    // Extracción de referencia de usuario de la base de datos para "Usuarios registrados"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                // Enviar verificacion al correo electronico
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(getActivity().getApplicationContext(), "User registered successfully. Please verify your email", Toast.LENGTH_SHORT).show();

                                // Open OnBoarding Tutorial Screen
                                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                getActivity().finish();


                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "User registered failed. Please try again", Toast.LENGTH_SHORT).show();
                            }

                            progressBar.setVisibility(View.GONE);

                        }
                    });

                    // Aquí puedes realizar acciones adicionales después de crear el usuario
                } else {
                    // Hubo un error al crear el usuario
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        tILRegisterPassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and especial characters");
                        tILRegisterPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        tILRegisterEmail.setError("Your email is invalid or already in use. Kindly re-enter.");
                        tILRegisterEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        tILRegisterEmail.setError("User is already registered with this email. Use another email.");
                        tILRegisterEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}