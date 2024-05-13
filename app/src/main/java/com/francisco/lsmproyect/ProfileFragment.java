package com.francisco.lsmproyect;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private TextView tvNameProfile, tvUsernameProfile, tvCurrentLevel, tvNextLevel, tvCoursesCompleted,
            tvStreak, tvTotalExperience, tvUserLocation, tvUserDayOfBirth, tvUserBio;
    private ProgressBar progressBar;
    private String name, username, userLocation, userDayOfBirth, userBio;
    private int currentLevel, nextLevel, coursesCompleted, streak, totalExperience;
    private FirebaseAuth authProfile;
    private ImageButton btnEditProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShapeableImageView imgProfile;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvNameProfile = view.findViewById(R.id.tvNameProfile);
        tvUsernameProfile = view.findViewById(R.id.tvUsernameProfile);
        tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel);
        tvNextLevel = view.findViewById(R.id.tvNextLevel);
        tvCoursesCompleted = view.findViewById(R.id.tvCoursesCompleted);
        tvStreak = view.findViewById(R.id.tvStreak);
        tvTotalExperience = view.findViewById(R.id.tvTotalExperience);
        tvUserLocation = view.findViewById(R.id.tvUserLocation);
        tvUserDayOfBirth = view.findViewById(R.id.tvUserDayOfBirth);
        tvUserBio = view.findViewById(R.id.tvUserBio);
        progressBar = view.findViewById(R.id.progressBar);

        btnEditProfile = view.findViewById(R.id.imgBtnEditProfile);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        imgProfile = view.findViewById(R.id.imageViewProfile);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Something went wrong! User's details are not available", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(view.VISIBLE);
            showUserProfile(firebaseUser);
        }

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showUserProfile(firebaseUser);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String uerId = firebaseUser.getUid();

        // Extracting user's details from database
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(uerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    name = firebaseUser.getDisplayName();
                    username = readUserDetails.username;
                    currentLevel = readUserDetails.currentLevel;
                    nextLevel = readUserDetails.nextLevel;
                    coursesCompleted = readUserDetails.coursesCompleted;
                    streak = readUserDetails.streak;
                    totalExperience = readUserDetails.totalExperience;
                    userLocation = readUserDetails.location;
                    userDayOfBirth = readUserDetails.doB;
                    userBio = readUserDetails.bio;

                    tvNameProfile.setText(name);
                    tvUsernameProfile.setText(username);
                    tvCurrentLevel.setText(String.valueOf(currentLevel));
                    tvNextLevel.setText(String.valueOf(nextLevel));
                    tvCoursesCompleted.setText(String.valueOf(coursesCompleted));
                    tvStreak.setText(String.valueOf(streak));
                    tvTotalExperience.setText(String.valueOf(totalExperience));
                    tvUserLocation.setText(userLocation);
                    tvUserDayOfBirth.setText(userDayOfBirth);
                    tvUserBio.setText(userBio);

                    // Set User DP (After user has uploaded their profile picture)
                    Uri uri = firebaseUser.getPhotoUrl();

                    // ImageView setImageURI should not be used with regular URIs. So we are using Picasso library
                    Picasso.with(getActivity()).load(uri).into(imgProfile);

                    // Toast.makeText(getActivity().getApplicationContext(), "PROFILE DETAILS FETCHED", Toast.LENGTH_SHORT).show();
                    Log.d("ProfileFragment", "PROFILE DETAILS FETCHED");

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}