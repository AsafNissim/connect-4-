package com.example.asafproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements GeminiModule.CellStateProvider {

    private BoardGame boardGame;
    private GameMoudle gameMoudle;
    private GeminiModule geminiModule;

    private int mode;
    private TextView tvTurn;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boardGame = findViewById(R.id.boardView);
        tvTurn = findViewById(R.id.tvTurn);
        Button btnRestart = findViewById(R.id.btnRestart);
        Button btnBack = findViewById(R.id.btnBack);

        gameMoudle = new GameMoudle();
        geminiModule = new GeminiModule();

        boardGame.setGameMoudle(gameMoudle);

        Intent intent = getIntent();
        mode = intent.getIntExtra(MainActivity.EXTRA_MODE, MainActivity.MODE_TWO_PLAYERS);

        tvTurn.setText("תור: אדום");

        btnRestart.setOnClickListener(v -> {
            gameMoudle.reset();
            gameMoudle.setGameOver(false);
            boardGame.invalidate();
            tvTurn.setText("תור: אדום");
        });

        btnBack.setOnClickListener(v -> finish());
    }

    // Gemini קורא מפה את מצב הלוח
    @Override
    public String boardToGeminiText() {
        return gameMoudle.boardToGeminiText();
    }

    /**
     * נקרא מ-BoardGame אחרי מהלך שחקן (אחרי dropDisk).
     */
    public void onDiskPlaced(Position placed) {
        if (placed == null) return;

        // ✅ בדיקת ניצחון אחרי מהלך שחקן
        int win = gameMoudle.isWin(placed);
        if (win == GameMoudle.redWin) {
            Toast.makeText(this, "🔴 אדום ניצח!", Toast.LENGTH_LONG).show();
            gameMoudle.setGameOver(true);
            return;
        }
        if (win == GameMoudle.yellowWin) {
            Toast.makeText(this, "🟡 צהוב ניצח!", Toast.LENGTH_LONG).show();
            gameMoudle.setGameOver(true);
            return;
        }

        // ✅ תור (אחרי dropDisk התור כבר התחלף)
        tvTurn.setText(gameMoudle.getCurrentPlayer() == Disk.Color.RED ? "תור: אדום" : "תור: צהוב");

        // ✅ אם זה שני שחקנים על אותו מכשיר
        if (mode == MainActivity.MODE_TWO_PLAYERS) return;

        // ✅ אם זה נגד מחשב: המחשב תמיד צהוב
        if (gameMoudle.getCurrentPlayer() != Disk.Color.YELLOW) return;

        // ✅ מחשב קל / קשה
        if (mode == MainActivity.MODE_EASY) {
            playEasyWithSmallDelay();
        } else if (mode == MainActivity.MODE_HARD) {
            playGeminiWithToastAndDelay();
        }
    }

    // -------- EASY --------
    private void playEasyWithSmallDelay() {
        if (gameMoudle.isGameOver()) return;

        // (לא חובה) תוכל להשאיר בלי טוסט, אבל זה יותר "חי"
        Toast.makeText(this, "מחשב חושב...", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            Position aiPlaced = gameMoudle.aiMoveRandom(); // מפיל בתוך המודל
            if (aiPlaced != null) {
                boardGame.invalidate();
                afterAiMove(aiPlaced);
            }
        }, 800);
    }

    // -------- HARD (GEMINI) --------
    private void playGeminiWithToastAndDelay() {
        if (gameMoudle.isGameOver()) return;

        // ✅ כמו פעם: טוסט חשיבה
        Toast.makeText(this, "Gemini חושב...", Toast.LENGTH_SHORT).show();

        geminiModule.requestHardMove(this, new GeminiModule.MoveCallback() {
            @Override
            public void onMove(int col) {
                // ✅ כמו שהוספנו: מחכה 3 שניות ואז עושה מהלך
                handler.postDelayed(() -> {

                    Position aiPlaced = gameMoudle.dropDisk(col);

                    // fallback אם Gemini טעה (עמודה מלאה וכו')
                    if (aiPlaced == null) {
                        aiPlaced = gameMoudle.aiMoveHardLocal();
                    }

                    if (aiPlaced != null) {
                        boardGame.invalidate();
                        afterAiMove(aiPlaced);
                    }

                }, 3000);
            }

            @Override
            public void onError(String msg) {
                // גם בשגיאה מחכים 3 שניות ואז fallback
                handler.postDelayed(() -> {

                    Position aiPlaced = gameMoudle.aiMoveHardLocal();
                    if (aiPlaced != null) {
                        boardGame.invalidate();
                        afterAiMove(aiPlaced);
                    }

                }, 3000);
            }
        });
    }

    // -------- אחרי מהלך מחשב --------
    private void afterAiMove(Position aiPlaced) {
        int win = gameMoudle.isWin(aiPlaced);

        if (win == GameMoudle.redWin) {
            Toast.makeText(this, "🔴 אדום ניצח!", Toast.LENGTH_LONG).show();
            gameMoudle.setGameOver(true);
            return;
        }
        if (win == GameMoudle.yellowWin) {
            Toast.makeText(this, "🟡 צהוב ניצח!", Toast.LENGTH_LONG).show();
            gameMoudle.setGameOver(true);
            return;
        }

        tvTurn.setText(gameMoudle.getCurrentPlayer() == Disk.Color.RED ? "תור: אדום" : "תור: צהוב");
    }
}

