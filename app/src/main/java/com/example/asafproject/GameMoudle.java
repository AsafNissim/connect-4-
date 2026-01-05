package com.example.asafproject; // חבילת הפרויקט

public class GameMoudle { // מודל המשחק

    public static final int redWin = 0; // קוד אדום
    public static final int yellowWin = 1; // קוד צהוב
    public static final int noWin = 2; // קוד אין

    private static final int ROWS = 6; // מספר שורות
    private static final int COLS = 7; // מספר עמודות

    private final Disk.Color[][] board = new Disk.Color[ROWS][COLS]; // מערך לוח
    private Disk.Color currentPlayer = Disk.Color.RED; // תור נוכחי

    public GameMoudle() { // בנאי מחלקה
        reset(); // איפוס משחק
    }

    public void reset() { // מאפס מצב
        for (int r = 0; r < ROWS; r++) { // לולאת שורות
            for (int c = 0; c < COLS; c++) { // לולאת עמודות
                board[r][c] = Disk.Color.EMPTY; // קובע ריק
            } // סוף עמודות
        } // סוף שורות
        currentPlayer = Disk.Color.RED; // מתחיל אדום
    }

    public int getRows() { return ROWS; } // מחזיר שורות
    public int getCols() { return COLS; } // מחזיר עמודות

    public Disk.Color getCurrentPlayer() { // מחזיר תור
        return currentPlayer; // ערך תור
    }

    public Disk.Color getCellColor(int row, int col) { // צבע תא
        return board[row][col]; // קורא מהלוח
    }

    // מפיל דיסק בעמודה ומחזיר Position של הנחיתה. אם העמודה מלאה מחזיר null. // הערת תיאור
    public Position dropDisk(int col) { // מפיל דיסק
        if (col < 0 || col >= COLS) return null; // טווח עמודה

        for (int row = ROWS - 1; row >= 0; row--) { // סריקה מלמטה
            if (board[row][col] == Disk.Color.EMPTY) { // אם תא ריק
                board[row][col] = currentPlayer; // מניח דיסק

                Position placed = new Position(row, col); // שומר מיקום

                // החלפת תור // תיאור החלפה
                currentPlayer = (currentPlayer == Disk.Color.RED) ? Disk.Color.YELLOW : Disk.Color.RED; // מחליף שחקן

                return placed; // מחזיר מיקום
            } // סוף אם
        } // סוף לולאה
        return null; // עמודה מלאה
    }

    // מחזיר redWin / yellowWin / noWin // הערת תיאור
    public int isWin(Position lastMove) { // בודק ניצחון
        if (lastMove == null) return noWin; // אין מהלך

        int r = lastMove.getRow(); // שורת מהלך
        int c = lastMove.getCol(); // עמודת מהלך
        Disk.Color color = board[r][c]; // צבע אחרון
        if (color == Disk.Color.EMPTY) return noWin; // הגנה ריק

        boolean win = // תוצאת בדיקה
                count(r, c, 0, 1, color) + count(r, c, 0, -1, color) - 1 >= 4 ||   // אופקי בדיקה
                        count(r, c, 1, 0, color) + count(r, c, -1, 0, color) - 1 >= 4 ||   // אנכי בדיקה
                        count(r, c, 1, 1, color) + count(r, c, -1, -1, color) - 1 >= 4 ||  // אלכסון \
                        count(r, c, 1, -1, color) + count(r, c, -1, 1, color) - 1 >= 4;    // אלכסון /

        if (!win) return noWin; // אין ניצחון
        return (color == Disk.Color.RED) ? redWin : yellowWin; // מחזיר מנצח
    }

    private int count(int r, int c, int dr, int dc, Disk.Color color) { // סופר רצף
        int cnt = 0; // מונה רצף
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == color) { // כל עוד חוקי
            cnt++; // מגדיל מונה
            r += dr; // מתקדם שורה
            c += dc; // מתקדם עמודה
        } // סוף while
        return cnt; // מחזיר כמות
    }

    private boolean gameOver = false; // דגל סיום

    public boolean isGameOver() { // בודק סיום
        return gameOver; // מחזיר דגל
    }

    public void setGameOver(boolean gameOver) { // קובע סיום
        this.gameOver = gameOver; // מעדכן דגל
    }

    // מחשב קל: בוחר עמודה אקראית חוקית ומפיל דיסק (צהוב) // הערת תיאור
    public Position aiMoveRandom() { // מהלך רנדום
        if (gameOver) return null; // אם נגמר
        if (getCurrentPlayer() != Disk.Color.YELLOW) return null; // לא תור צהוב

        java.util.ArrayList<Integer> validCols = new java.util.ArrayList<>(); // רשימת חוקיות
        for (int c = 0; c < getCols(); c++) { // עובר עמודות
            if (getCellColor(0, c) == Disk.Color.EMPTY) { // אם לא מלא
                validCols.add(c); // מוסיף עמודה
            } // סוף אם
        } // סוף לולאה

        if (validCols.isEmpty()) return null; // אין מהלך

        int col = validCols.get(new java.util.Random().nextInt(validCols.size())); // בוחר אקראי
        return dropDisk(col); // מפיל דיסק
    }

    // טקסט ל-Gemini: '.' ריק, 'R' אדום, 'Y' צהוב (שורה 0 למעלה) // הערת תיאור
    public String boardToGeminiText() { // ממיר לטקסט
        StringBuilder sb = new StringBuilder(); // בונה מחרוזת
        sb.append("Board 6x7 (row 0 is top):\n"); // כותרת לוח
        for (int r = 0; r < getRows(); r++) { // לולאת שורות
            for (int c = 0; c < getCols(); c++) { // לולאת עמודות
                Disk.Color col = getCellColor(r, c); // קורא צבע
                if (col == Disk.Color.RED) sb.append("R "); // מוסיף R
                else if (col == Disk.Color.YELLOW) sb.append("Y "); // מוסיף Y
                else sb.append(". "); // מוסיף נקודה
            } // סוף עמודות
            sb.append("\n"); // ירידת שורה
        } // סוף שורות
        sb.append("Columns are 0..6.\n"); // טווח עמודות
        return sb.toString(); // מחזיר טקסט
    }

    // ---------- HARD AI (LOCAL) ---------- // כותרת חלק
    // צהוב = AI, אדום = שחקן // מי נגד מי
    public Position aiMoveHardLocal() { // מהלך חכם
        if (gameOver) return null; // אם נגמר
        if (getCurrentPlayer() != Disk.Color.YELLOW) return null; // לא תור צהוב

        Disk.Color[][] b = snapshotBoard(); // צילום לוח

        int winCol = findWinningColumn(b, Disk.Color.YELLOW); // מחפש ניצחון
        if (winCol != -1) return dropDisk(winCol); // מנצח עכשיו

        int blockCol = findWinningColumn(b, Disk.Color.RED); // מחפש איום
        if (blockCol != -1) return dropDisk(blockCol); // חוסם אדום

        int bestCol = pickBestHeuristicColumn(b); // בוחר הכי טוב
        if (bestCol != -1) return dropDisk(bestCol); // מפיל לשם

        return null; // אין מהלך
    }

    // ---- Helpers ---- // כותרת עזר
    private Disk.Color[][] snapshotBoard() { // יוצר צילום
        Disk.Color[][] b = new Disk.Color[getRows()][getCols()]; // לוח חדש
        for (int r = 0; r < getRows(); r++) { // עובר שורות
            for (int c = 0; c < getCols(); c++) { // עובר עמודות
                b[r][c] = getCellColor(r, c); // מעתיק תא
            } // סוף עמודות
        } // סוף שורות
        return b; // מחזיר צילום
    }

    private int findWinningColumn(Disk.Color[][] b, Disk.Color who) { // מוצא עמודה מנצחת
        for (int c = 0; c < getCols(); c++) { // עובר עמודות
            int r = nextEmptyRow(b, c); // מוצא שורה פנויה
            if (r == -1) continue; // עמודה מלאה

            Disk.Color[][] tmp = copyBoard(b); // מעתיק לוח
            tmp[r][c] = who; // מדמה מהלך

            if (isWinOnBoard(tmp, who)) return c; // אם ניצחון
        } // סוף לולאה
        return -1; // לא נמצא
    }

    private int pickBestHeuristicColumn(Disk.Color[][] b) { // בוחר לפי ניקוד
        int bestCol = -1; // עמודה טובה
        int bestScore = Integer.MIN_VALUE; // ניקוד מינימלי

        for (int c = 0; c < getCols(); c++) { // עובר עמודות
            int r = nextEmptyRow(b, c); // שורה פנויה
            if (r == -1) continue; // מלאה

            Disk.Color[][] tmp = copyBoard(b); // לוח זמני
            tmp[r][c] = Disk.Color.YELLOW; // מדמה צהוב

            int score = scoreBoard(tmp); // מחשב ניקוד

            if (score > bestScore) { // אם יותר טוב
                bestScore = score; // שומר ניקוד
                bestCol = c; // שומר עמודה
            } // סוף אם
        } // סוף לולאה
        return bestCol; // מחזיר בחירה
    }

    private int nextEmptyRow(Disk.Color[][] b, int col) { // מחפש שורה פנויה
        for (int r = getRows() - 1; r >= 0; r--) { // מלמטה למעלה
            if (b[r][col] == Disk.Color.EMPTY) return r; // מצא ריק
        } // סוף לולאה
        return -1; // אין מקום
    }

    private Disk.Color[][] copyBoard(Disk.Color[][] b) { // מעתיק לוח
        Disk.Color[][] cp = new Disk.Color[getRows()][getCols()]; // לוח חדש
        for (int r = 0; r < getRows(); r++) { // עובר שורות
            System.arraycopy(b[r], 0, cp[r], 0, getCols()); // מעתיק שורה
        } // סוף לולאה
        return cp; // מחזיר העתק
    }

    private boolean isWinOnBoard(Disk.Color[][] b, Disk.Color who) { // בדיקת ניצחון
        int R = getRows(); // שורות מקומיות
        int C = getCols(); // עמודות מקומיות

        // אופקי // כיוון אופקי
        for (int r = 0; r < R; r++) { // עובר שורות
            for (int c = 0; c <= C - 4; c++) { // חלון ארבעה
                if (b[r][c] == who && b[r][c+1] == who && b[r][c+2] == who && b[r][c+3] == who) return true; // 4 ברצף
            } // סוף עמודות
        } // סוף שורות

        // אנכי // כיוון אנכי
        for (int c = 0; c < C; c++) { // עובר עמודות
            for (int r = 0; r <= R - 4; r++) { // חלון ארבעה
                if (b[r][c] == who && b[r+1][c] == who && b[r+2][c] == who && b[r+3][c] == who) return true; // 4 ברצף
            } // סוף שורות
        } // סוף עמודות

        // אלכסון יורד (\) // אלכסון מטה
        for (int r = 0; r <= R - 4; r++) { // שורות התחלה
            for (int c = 0; c <= C - 4; c++) { // עמודות התחלה
                if (b[r][c] == who && b[r+1][c+1] == who && b[r+2][c+2] == who && b[r+3][c+3] == who) return true; // 4 ברצף
            } // סוף עמודות
        } // סוף שורות

        // אלכסון עולה (/) // אלכסון מעלה
        for (int r = 3; r < R; r++) { // שורות התחלה
            for (int c = 0; c <= C - 4; c++) { // עמודות התחלה
                if (b[r][c] == who && b[r-1][c+1] == who && b[r-2][c+2] == who && b[r-3][c+3] == who) return true; // 4 ברצף
            } // סוף עמודות
        } // סוף שורות

        return false; // אין ניצחון
    }

    // ניקוד פשוט אבל חזק: // כותרת ניקוד
    // - מעדיף מרכז // העדפת מרכז
    // - מתגמל "שלשות" ו"זוגות" של צהוב // מתגמל רצפים
    // - מעניש "שלשות" של אדום (איום) // מעניש איומים
    private int scoreBoard(Disk.Color[][] b) { // מחשב ניקוד
        int score = 0; // מתחיל אפס

        int centerCol = getCols() / 2; // עמודת אמצע
        for (int r = 0; r < getRows(); r++) { // עובר שורות
            if (b[r][centerCol] == Disk.Color.YELLOW) score += 6; // צהוב במרכז
            if (b[r][centerCol] == Disk.Color.RED) score -= 6; // אדום במרכז
        } // סוף לולאה

        // כל חלון של 4 תאים // מתחיל חלונות
        // אופקי // חלונות אופקיים
        for (int r = 0; r < getRows(); r++) { // עובר שורות
            for (int c = 0; c <= getCols() - 4; c++) { // חלון ארבעה
                score += scoreWindow(b[r][c], b[r][c+1], b[r][c+2], b[r][c+3]); // מוסיף ניקוד
            } // סוף עמודות
        } // סוף שורות

        // אנכי // חלונות אנכיים
        for (int c = 0; c < getCols(); c++) { // עובר עמודות
            for (int r = 0; r <= getRows() - 4; r++) { // חלון ארבעה
                score += scoreWindow(b[r][c], b[r+1][c], b[r+2][c], b[r+3][c]); // מוסיף ניקוד
            } // סוף שורות
        } // סוף עמודות

        // אלכסון \ // חלונות אלכסון \
        for (int r = 0; r <= getRows() - 4; r++) { // שורות התחלה
            for (int c = 0; c <= getCols() - 4; c++) { // עמודות התחלה
                score += scoreWindow(b[r][c], b[r+1][c+1], b[r+2][c+2], b[r+3][c+3]); // מוסיף ניקוד
            } // סוף עמודות
        } // סוף שורות

        // אלכסון / // חלונות אלכסון /
        for (int r = 3; r < getRows(); r++) { // שורות התחלה
            for (int c = 0; c <= getCols() - 4; c++) { // עמודות התחלה
                score += scoreWindow(b[r][c], b[r-1][c+1], b[r-2][c+2], b[r-3][c+3]); // מוסיף ניקוד
            } // סוף עמודות
        } // סוף שורות

        return score; // מחזיר ניקוד
    }

    private int scoreWindow(Disk.Color a, Disk.Color b, Disk.Color c, Disk.Color d) { // ניקוד חלון
        int y = 0, r = 0, e = 0; // מונים צבעים
        Disk.Color[] w = new Disk.Color[]{a,b,c,d}; // יוצר מערך 4
        for (Disk.Color x : w) { // עובר תאים
            if (x == Disk.Color.YELLOW) y++; // סופר צהוב
            else if (x == Disk.Color.RED) r++; // סופר אדום
            else e++; // סופר ריק
        } // סוף לולאה

        if (y > 0 && r > 0) return 0; // חלון מעורב

        if (y == 4) return 100000; // צהוב ניצח
        if (y == 3 && e == 1) return 120; // שלשה צהוב
        if (y == 2 && e == 2) return 15; // זוג צהוב

        if (r == 4) return -100000; // אדום ניצח
        if (r == 3 && e == 1) return -140; // שלשה אדום
        if (r == 2 && e == 2) return -18; // זוג אדום

        return 0; // אחרת אפס
    }

} // סוף מחלקה
