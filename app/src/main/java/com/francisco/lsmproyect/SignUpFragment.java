package com.francisco.lsmproyect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class SignUpFragment extends Fragment {

    private EditText etRegisterName, etRegisterUsername, etRegisterDoB, etRegisterPhone, etRegisterEmail,
            etRegisterPassword, etRegisterPasswordConfirm;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        return view;
    }
}