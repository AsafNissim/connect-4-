package com.example.asafproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "EXTRA_MODE";

    public static final int MODE_TWO_PLAYERS = 0;
    public static final int MODE_EASY = 1;
    public static final int MODE_HARD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // חובה קודם XML
        setContentView(R.layout.activity_main);

        // כפתורים
        Button btnTwoPlayers = findViewById(R.id.btnTwoPlayers);
        Button btnEasy = findViewById(R.id.btnComputerEasy);
        Button btnHard = findViewById(R.id.btnComputerHard);
        Button btnInstructions = findViewById(R.id.btnInstructions);

        // שני שחקנים
        btnTwoPlayers.setOnClickListener(v ->
                startGame(MODE_TWO_PLAYERS)
        );

        // מחשב קל
        btnEasy.setOnClickListener(v ->
                startGame(MODE_EASY)
        );

        // מחשב קשה (Gemini)
        btnHard.setOnClickListener(v ->
                startGame(MODE_HARD)
        );

        // הוראות
        btnInstructions.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, InstructionActivity.class);
            startActivity(i);
        });
    }

    private void startGame(int mode) {
        Intent i = new Intent(MainActivity.this, GameActivity.class);
        i.putExtra(EXTRA_MODE, mode);
        startActivity(i);
    }
}
