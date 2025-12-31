package com.example.asafproject;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiModule {

    // ✅ הכנס את ה-API KEY שלך כאן
    private static final String API_KEY = "AIzaSyAOQ39Zgi1GhfIIK1AyHV87cKEFa_bBaeA";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client = new OkHttpClient.Builder();



    public interface MoveCallback {
        void onMove(int col);      // 0-6
        void onError(String msg);
    }

    public interface CellStateProvider {
        String boardToGeminiText();
    }

    // בקשה למהלך "קשה"
    public void requestHardMove(CellStateProvider provider, MoveCallback callback) {
        try {
            String boardText = provider.boardToGeminiText();

            JSONObject input = new JSONObject();

            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject text = new JSONObject();

            // ✅ פרומפט חזק: ניצחון -> חסימה -> בניית יתרון + הימנעות ממתנת ניצחון לאדום
            text.put("text",
                    "You are playing Connect 4 as YELLOW.\n" +
                            boardText + "\n" +
                            "Rules:\n" +
                            "- Columns are 0..6.\n" +
                            "- A move is choosing a column; the disc falls to the lowest empty cell.\n\n" +
                            "PRIORITY:\n" +
                            "1) If you have a winning move now, play it.\n" +
                            "2) Else if RED has a winning move next turn, block it.\n" +
                            "3) Else prefer center columns, create threats (3-in-a-row with an open end), and avoid moves that allow RED to win immediately.\n\n" +
                            "Return ONLY this format, nothing else:\n" +
                            "col=<0-6>"
            );

            parts.put(text);
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            input.put("contents", contents);

            RequestBody body = RequestBody.create(
                    input.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    postError(callback, "Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        postError(callback, "Gemini error " + response.code());
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(resp);
                        JSONArray candidates = json.getJSONArray("candidates");
                        if (candidates.length() == 0) {
                            postError(callback, "No candidates");
                            return;
                        }

                        JSONObject first = candidates.getJSONObject(0);
                        JSONObject content = first.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() == 0) {
                            postError(callback, "No parts");
                            return;
                        }

                        String text = parts.getJSONObject(0).getString("text");
                        int col = parseCol(text);

                        if (col < 0 || col > 6) {
                            postError(callback, "Bad move text");
                            return;
                        }

                        postMove(callback, col);

                    } catch (Exception ex) {
                        postError(callback, "Parse error");
                    }
                }
            });

        } catch (Exception e) {
            postError(callback, "Build request error");
        }
    }

    private int parseCol(String text) {
        if (text == null) return -1;
        String t = text.toLowerCase().replaceAll("[^a-z0-9=]", " ");
        String[] tokens = t.split("\\s+");
        for (String token : tokens) {
            if (token.startsWith("col=")) {
                String num = token.substring(4).replaceAll("[^0-9]", "");
                if (!num.isEmpty()) return Integer.parseInt(num);
            }
        }
        return -1;
    }

    private void postMove(MoveCallback cb, int col) {
        new Handler(Looper.getMainLooper()).post(() -> cb.onMove(col));
    }

    private void postError(MoveCallback cb, String msg) {
        new Handler(Looper.getMainLooper()).post(() -> cb.onError(msg));
    }
}
