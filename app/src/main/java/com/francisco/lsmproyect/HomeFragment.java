package com.francisco.lsmproyect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    CardView cardViewUnitOne;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        } else {
            checkIfUserVerified(user);
        }

        cardViewUnitOne = view.findViewById(R.id.cardViewUnitOne);

        cardViewUnitOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UnitOneActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    private void checkIfUserVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        } else {
            // Toast.makeText(getActivity().getApplicationContext(), "User's email is verified", Toast.LENGTH_SHORT).show();
            Log.d("HomeFragment", "User's email is verified");
        }
    }

    private void showAlertDialog() {
        // Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Email Not Verify");
        builder.setMessage("Please verify your email now. You can not login without email verification next time");

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
}