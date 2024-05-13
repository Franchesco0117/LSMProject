package com.francisco.lsmproyect.PROTOTYPE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.francisco.lsmproyect.R;
import com.francisco.lsmproyect.UnitOneActivity;

public class PartTwoLessonOneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_two_lesson_one);

        ImageView btnBack = findViewById(R.id.btnBack);

        Button btnOption1 = findViewById(R.id.btnOption5);
        Button btnOption2 = findViewById(R.id.btnOption6);
        Button btnOption3 = findViewById(R.id.btnOption7);
        Button btnOption4 = findViewById(R.id.btnOption8);

        Button btnContinue = findViewById(R.id.btnContinue);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PartTwoLessonOneActivity.this, UnitOneActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnOption1.setBackgroundColor(Color.GREEN);
                btnOption1.setTextColor(Color.WHITE);

                btnOption2.setEnabled(false);
                btnOption3.setEnabled(false);
                btnOption4.setEnabled(false);

                btnContinue.setEnabled(true);

                Toast.makeText(PartTwoLessonOneActivity.this, "Correct answer", Toast.LENGTH_SHORT).show();
            }
        });

        btnOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnOption2.setBackgroundColor(Color.RED);
                btnOption2.setTextColor(Color.WHITE);

                btnOption1.setEnabled(false);
                btnOption3.setEnabled(false);
                btnOption4.setEnabled(false);

                btnContinue.setEnabled(true);

                Toast.makeText(PartTwoLessonOneActivity.this, "Incorrect answer", Toast.LENGTH_SHORT).show();
            }
        });

        btnOption3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnOption3.setBackgroundColor(Color.RED);
                btnOption3.setTextColor(Color.WHITE);

                btnOption1.setEnabled(false);
                btnOption2.setEnabled(false);
                btnOption4.setEnabled(false);

                btnContinue.setEnabled(true);

                Toast.makeText(PartTwoLessonOneActivity.this, "Incorrect answer", Toast.LENGTH_SHORT).show();
            }
        });

        // Correct answer
        btnOption4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnOption4.setBackgroundColor(Color.RED);
                btnOption4.setTextColor(Color.WHITE);

                btnOption1.setEnabled(false);
                btnOption2.setEnabled(false);
                btnOption3.setEnabled(false);

                btnContinue.setEnabled(true);

                Toast.makeText(PartTwoLessonOneActivity.this, "Incorrect answer", Toast.LENGTH_SHORT).show();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PartTwoLessonOneActivity.this, PartThreeLessonOneActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}