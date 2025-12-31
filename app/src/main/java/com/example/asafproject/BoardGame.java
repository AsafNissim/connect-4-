package com.example.asafproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class BoardGame extends View {

    private GameMoudle gameMoudle;

    private int cellW = 0, cellH = 0;

    private final Paint boardPaint = new Paint();
    private final Paint holePaint = new Paint();

    private Bitmap bmpRed, bmpYellow;
    private final Rect dstRect = new Rect();

    public BoardGame(Context context) {
        super(context);
        init();
    }

    public BoardGame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);                 // 🔥 חשוב
        setFocusable(true);
        setFocusableInTouchMode(true);

        boardPaint.setAntiAlias(true);
        boardPaint.setStyle(Paint.Style.FILL);
        boardPaint.setARGB(255, 20, 90, 200);

        holePaint.setAntiAlias(true);

        holePaint.setStyle(Paint.Style.FILL);
        holePaint.setARGB(255, 40, 40, 40);
    }

    public void setGameMoudle(GameMoudle gm) {
        this.gameMoudle = gm;
        prepare(getWidth(), getHeight());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        prepare(w, h);
    }

    private void prepare(int w, int h) {
        if (gameMoudle == null) return;
        if (w <= 0 || h <= 0) return;

        cellW = w / gameMoudle.getCols();
        cellH = h / gameMoudle.getRows();
        if (cellW <= 0 || cellH <= 0) return;

        bmpRed = BitmapFactory.decodeResource(getResources(), R.drawable.img_3);
        bmpYellow = BitmapFactory.decodeResource(getResources(), R.drawable.img_2);

        int size = Math.min(cellW, cellH) - 14;
        if (size < 1) size = 1;

        bmpRed = Bitmap.createScaledBitmap(bmpRed, size, size, true);
        bmpYellow = Bitmap.createScaledBitmap(bmpYellow, size, size, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gameMoudle == null) return;

        if (cellW == 0 || cellH == 0) prepare(getWidth(), getHeight());

        int rows = gameMoudle.getRows();
        int cols = gameMoudle.getCols();

        float pad = 12f;
        canvas.drawRoundRect(pad, pad, getWidth() - pad, getHeight() - pad, 40f, 40f, boardPaint);

        if (cellW == 0 || cellH == 0) return;

        float holeRadius = Math.min(cellW, cellH) * 0.38f;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float cx = c * cellW + cellW / 2f;
                float cy = r * cellH + cellH / 2f;
                canvas.drawCircle(cx, cy, holeRadius, holePaint);
            }
        }

        if (bmpRed == null || bmpYellow == null) return;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Disk.Color color = gameMoudle.getCellColor(r, c);
                if (color == Disk.Color.EMPTY) continue;

                Bitmap bmp = (color == Disk.Color.RED) ? bmpRed : bmpYellow;

                int cx = c * cellW + cellW / 2;
                int cy = r * cellH + cellH / 2;

                int halfW = bmp.getWidth() / 2;
                int halfH = bmp.getHeight() / 2;

                dstRect.set(cx - halfW, cy - halfH, cx + halfW, cy + halfH);
                canvas.drawBitmap(bmp, null, dstRect, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
        if (gameMoudle == null) return true;
        if (gameMoudle.isGameOver()) return true;
        if (cellW == 0) return true;

        int col = (int) (event.getX() / cellW);
        if (col < 0 || col >= gameMoudle.getCols()) return true;

        Position placed = gameMoudle.dropDisk(col);
        if (placed == null) return true;

        invalidate();

        if (getContext() instanceof GameActivity) {
            ((GameActivity) getContext()).onDiskPlaced(placed);
        }
        return true;
    }
}
