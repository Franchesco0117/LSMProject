package com.francisco.lsmproyect.PROTOTYPE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.francisco.lsmproyect.R;
import com.francisco.lsmproyect.UnitOneActivity;

public class IntroLessonOneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_lesson_one);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnContinue = findViewById(R.id.btnContinue);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroLessonOneActivity.this, UnitOneActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroLessonOneActivity.this, PartOneLessonOneActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}