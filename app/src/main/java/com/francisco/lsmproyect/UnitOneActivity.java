package com.francisco.lsmproyect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.francisco.lsmproyect.PROTOTYPE.IntroLessonOneActivity;
import com.francisco.lsmproyect.PROTOTYPE.IntroLessonTwoActivity;
import com.google.android.material.imageview.ShapeableImageView;

public class UnitOneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_one);

        ImageView btnBack = findViewById(R.id.btnBack);

        ShapeableImageView imageViewLessonOne = findViewById(R.id.imageViewLessonOne);
        ShapeableImageView imageViewLessonTwo = findViewById(R.id.imageViewLessonTwo);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UnitOneActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageViewLessonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UnitOneActivity.this, IntroLessonOneActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageViewLessonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UnitOneActivity.this, IntroLessonTwoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}