package com.example.asafproject;

public class GameMoudle {

    public static final int redWin = 0;
    public static final int yellowWin = 1;
    public static final int noWin = 2;

    private static final int ROWS = 6;
    private static final int COLS = 7;

    private final Disk.Color[][] board = new Disk.Color[ROWS][COLS];
    private Disk.Color currentPlayer = Disk.Color.RED;

    public GameMoudle() {
        reset();
    }

    public void reset() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = Disk.Color.EMPTY;
            }
        }
        currentPlayer = Disk.Color.RED;
    }

    public int getRows() { return ROWS; }
    public int getCols() { return COLS; }

    public Disk.Color getCurrentPlayer() {
        return currentPlayer;
    }

    public Disk.Color getCellColor(int row, int col) {
        return board[row][col];
    }

    // מפיל דיסק בעמודה ומחזיר Position של הנחיתה. אם העמודה מלאה מחזיר null.
    public Position dropDisk(int col) {
        if (col < 0 || col >= COLS) return null;

        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == Disk.Color.EMPTY) {
                board[row][col] = currentPlayer;

                Position placed = new Position(row, col);

                // החלפת תור
                currentPlayer = (currentPlayer == Disk.Color.RED) ? Disk.Color.YELLOW : Disk.Color.RED;

                return placed;
            }
        }
        return null; // עמודה מלאה
    }

    // מחזיר redWin / yellowWin / noWin
    public int isWin(Position lastMove) {
        if (lastMove == null) return noWin;

        int r = lastMove.getRow();
        int c = lastMove.getCol();
        Disk.Color color = board[r][c];
        if (color == Disk.Color.EMPTY) return noWin;

        boolean win =
                count(r, c, 0, 1, color) + count(r, c, 0, -1, color) - 1 >= 4 ||   // אופקי
                        count(r, c, 1, 0, color) + count(r, c, -1, 0, color) - 1 >= 4 ||   // אנכי
                        count(r, c, 1, 1, color) + count(r, c, -1, -1, color) - 1 >= 4 ||  // אלכסון יורד
                        count(r, c, 1, -1, color) + count(r, c, -1, 1, color) - 1 >= 4;    // אלכסון עולה

        if (!win) return noWin;
        return (color == Disk.Color.RED) ? redWin : yellowWin;
    }

    private int count(int r, int c, int dr, int dc, Disk.Color color) {
        int cnt = 0;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == color) {
            cnt++;
            r += dr;
            c += dc;
        }
        return cnt;
    }
    private boolean gameOver = false;

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    // מחשב קל: בוחר עמודה אקראית חוקית ומפיל דיסק (צהוב)
    public Position aiMoveRandom() {
        if (gameOver) return null;

        // אם זה לא התור של הצהוב - לא עושים כלום
        if (getCurrentPlayer() != Disk.Color.YELLOW) return null;

        java.util.ArrayList<Integer> validCols = new java.util.ArrayList<>();
        for (int c = 0; c < getCols(); c++) {
            // אם השורה העליונה לא EMPTY אז העמודה מלאה
            if (getCellColor(0, c) == Disk.Color.EMPTY) {
                validCols.add(c);
            }
        }

        if (validCols.isEmpty()) return null;

        int col = validCols.get(new java.util.Random().nextInt(validCols.size()));
        return dropDisk(col); // משתמש באותה לוגיקה שלך
    }
    // טקסט ל-Gemini: '.' ריק, 'R' אדום, 'Y' צהוב (שורה 0 למעלה)
    public String boardToGeminiText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board 6x7 (row 0 is top):\n");
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                Disk.Color col = getCellColor(r, c);
                if (col == Disk.Color.RED) sb.append("R ");
                else if (col == Disk.Color.YELLOW) sb.append("Y ");
                else sb.append(". ");
            }
            sb.append("\n");
        }
        sb.append("Columns are 0..6.\n");
        return sb.toString();
    }

    // ---------- HARD AI (LOCAL) ----------
// צהוב = AI, אדום = שחקן
    public Position aiMoveHardLocal() {
        if (gameOver) return null;
        if (getCurrentPlayer() != Disk.Color.YELLOW) return null;

        Disk.Color[][] b = snapshotBoard();

        // 1) לנצח עכשיו אם אפשר
        int winCol = findWinningColumn(b, Disk.Color.YELLOW);
        if (winCol != -1) return dropDisk(winCol);

        // 2) לחסום ניצחון של אדום אם צריך
        int blockCol = findWinningColumn(b, Disk.Color.RED);
        if (blockCol != -1) return dropDisk(blockCol);

        // 3) אחרת לבחור עמודה עם ניקוד הכי טוב
        int bestCol = pickBestHeuristicColumn(b);
        if (bestCol != -1) return dropDisk(bestCol);

        // אם הכל מלא - אין מהלך
        return null;
    }

    // ---- Helpers ----
    private Disk.Color[][] snapshotBoard() {
        Disk.Color[][] b = new Disk.Color[getRows()][getCols()];
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                b[r][c] = getCellColor(r, c);
            }
        }
        return b;
    }

    private int findWinningColumn(Disk.Color[][] b, Disk.Color who) {
        for (int c = 0; c < getCols(); c++) {
            int r = nextEmptyRow(b, c);
            if (r == -1) continue;

            Disk.Color[][] tmp = copyBoard(b);
            tmp[r][c] = who;

            if (isWinOnBoard(tmp, who)) return c;
        }
        return -1;
    }

    private int pickBestHeuristicColumn(Disk.Color[][] b) {
        int bestCol = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int c = 0; c < getCols(); c++) {
            int r = nextEmptyRow(b, c);
            if (r == -1) continue;

            Disk.Color[][] tmp = copyBoard(b);
            tmp[r][c] = Disk.Color.YELLOW;

            int score = scoreBoard(tmp);

            if (score > bestScore) {
                bestScore = score;
                bestCol = c;
            }
        }
        return bestCol;
    }

    private int nextEmptyRow(Disk.Color[][] b, int col) {
        for (int r = getRows() - 1; r >= 0; r--) {
            if (b[r][col] == Disk.Color.EMPTY) return r;
        }
        return -1;
    }

    private Disk.Color[][] copyBoard(Disk.Color[][] b) {
        Disk.Color[][] cp = new Disk.Color[getRows()][getCols()];
        for (int r = 0; r < getRows(); r++) {
            System.arraycopy(b[r], 0, cp[r], 0, getCols());
        }
        return cp;
    }

    private boolean isWinOnBoard(Disk.Color[][] b, Disk.Color who) {
        int R = getRows();
        int C = getCols();

        // אופקי
        for (int r = 0; r < R; r++) {
            for (int c = 0; c <= C - 4; c++) {
                if (b[r][c] == who && b[r][c+1] == who && b[r][c+2] == who && b[r][c+3] == who) return true;
            }
        }
        // אנכי
        for (int c = 0; c < C; c++) {
            for (int r = 0; r <= R - 4; r++) {
                if (b[r][c] == who && b[r+1][c] == who && b[r+2][c] == who && b[r+3][c] == who) return true;
            }
        }
        // אלכסון יורד (\)
        for (int r = 0; r <= R - 4; r++) {
            for (int c = 0; c <= C - 4; c++) {
                if (b[r][c] == who && b[r+1][c+1] == who && b[r+2][c+2] == who && b[r+3][c+3] == who) return true;
            }
        }
        // אלכסון עולה (/)
        for (int r = 3; r < R; r++) {
            for (int c = 0; c <= C - 4; c++) {
                if (b[r][c] == who && b[r-1][c+1] == who && b[r-2][c+2] == who && b[r-3][c+3] == who) return true;
            }
        }
        return false;
    }

    // ניקוד פשוט אבל חזק:
// - מעדיף מרכז
// - מתגמל "שלשות" ו"זוגות" של צהוב
// - מעניש "שלשות" של אדום (איום)
    private int scoreBoard(Disk.Color[][] b) {
        int score = 0;

        // מרכז חזק
        int centerCol = getCols() / 2;
        for (int r = 0; r < getRows(); r++) {
            if (b[r][centerCol] == Disk.Color.YELLOW) score += 6;
            if (b[r][centerCol] == Disk.Color.RED) score -= 6;
        }

        // כל חלון של 4 תאים
        // אופקי
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c <= getCols() - 4; c++) {
                score += scoreWindow(b[r][c], b[r][c+1], b[r][c+2], b[r][c+3]);
            }
        }
        // אנכי
        for (int c = 0; c < getCols(); c++) {
            for (int r = 0; r <= getRows() - 4; r++) {
                score += scoreWindow(b[r][c], b[r+1][c], b[r+2][c], b[r+3][c]);
            }
        }
        // אלכסון \
        for (int r = 0; r <= getRows() - 4; r++) {
            for (int c = 0; c <= getCols() - 4; c++) {
                score += scoreWindow(b[r][c], b[r+1][c+1], b[r+2][c+2], b[r+3][c+3]);
            }
        }
        // אלכסון /
        for (int r = 3; r < getRows(); r++) {
            for (int c = 0; c <= getCols() - 4; c++) {
                score += scoreWindow(b[r][c], b[r-1][c+1], b[r-2][c+2], b[r-3][c+3]);
            }
        }

        return score;
    }

    private int scoreWindow(Disk.Color a, Disk.Color b, Disk.Color c, Disk.Color d) {
        int y = 0, r = 0, e = 0;
        Disk.Color[] w = new Disk.Color[]{a,b,c,d};
        for (Disk.Color x : w) {
            if (x == Disk.Color.YELLOW) y++;
            else if (x == Disk.Color.RED) r++;
            else e++;
        }

        // אם יש גם אדום וגם צהוב בחלון - זה לא "פוטנציאל" לשום צד
        if (y > 0 && r > 0) return 0;

        // צהוב (AI)
        if (y == 4) return 100000;
        if (y == 3 && e == 1) return 120;
        if (y == 2 && e == 2) return 15;

        // אדום (שחקן) - איום, מענישים
        if (r == 4) return -100000;
        if (r == 3 && e == 1) return -140;
        if (r == 2 && e == 2) return -18;

        return 0;
    }

}

