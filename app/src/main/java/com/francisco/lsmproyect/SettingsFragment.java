package com.francisco.lsmproyect;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FirebaseAuth authProfile;
    CardView cardViewSignOut;
    private TextView textViewDeleteAccount, textViewEditProfile, textViewUpdateEmail, textViewFaq
            , textViewAppOverview;
    private SwitchMaterial switchNotifications, switchTheme;
    boolean isNightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Spinner spinnerLanguage;
    public static final String[] languages = {"Select Language", "English", "Spanish", "French"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        authProfile = FirebaseAuth.getInstance();

        cardViewSignOut = view.findViewById(R.id.cardSignOut);
        textViewDeleteAccount = view.findViewById(R.id.tvDeleteAccount);
        textViewEditProfile = view.findViewById(R.id.tvEditProfile);
        textViewUpdateEmail = view.findViewById(R.id.tvUpdateEmail);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchTheme = view.findViewById(R.id.switchTheme);
        textViewFaq = view.findViewById(R.id.tvFaq);
        textViewAppOverview = view.findViewById(R.id.tvOverview);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), // Pass the Context object
                android.R.layout.simple_spinner_dropdown_item,
                languages
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setSelection(0);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();

                if (selectedItem.equals("English")) {
                    setLocal(SettingsFragment.this, "en");
                    getActivity().recreate();
                    getActivity().finish();
                } else if (selectedItem.equals("Spanish")) {
                    setLocal(SettingsFragment.this, "es-rMX");
                    getActivity().recreate();
                    getActivity().finish();
                } else if (selectedItem.equals("French")) {
                    setLocal(SettingsFragment.this, "fr");
                    getActivity().finish();
                    getActivity().recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isNightMode = sharedPreferences.getBoolean("isNightMode", false);

        cardViewSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authProfile.signOut();
                Toast.makeText(getActivity().getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MainActivity.class);

                // Clear stack to prevent user coming back
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        textViewEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UpdateProfileActivity.class);
                startActivity(intent);
                getActivity();
            }
        });

        textViewUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UpdateEmailActivity.class);
                startActivity(intent);
                getActivity();
            }
        });

        switchNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchNotifications.isChecked()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isNightMode) {
            switchTheme.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            switchTheme.setChecked(false);
        }

        switchTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTheme();
            }
        });

        textViewFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FaqActivity.class);
                startActivity(intent);
                getActivity();
            }
        });

        textViewAppOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WelcomeActivity.class);
                startActivity(intent);
                getActivity();
            }
        });

        return view;
    }

    private void setTheme() {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("isNightMode", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean("isNightMode", true);
        }
        editor.apply();
    }

    public void setLocal(Fragment fragment, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = fragment.getResources();
        Configuration configuration = resources.getConfiguration();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}