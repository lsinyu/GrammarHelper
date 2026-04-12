package com.example.grammarhelper.ai;

import android.util.Log;
import com.example.grammarhelper.util.Config;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiApiClient {
    private static final String TAG = "GeminiApiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void generateContent(String prompt, final ApiCallback callback) {
        try {
            String apiUrl = Config.GEMINI_API_URL;
            Log.d(TAG, "API URL: " + apiUrl);
            Log.d(TAG, "Sending prompt (length=" + prompt.length() + "): " + prompt.substring(0, Math.min(100, prompt.length())) + "...");

            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();

            partObject.put("text", prompt);
            partsArray.put(partObject);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            jsonBody.put("contents", contentsArray);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Request failed: " + e.getMessage());
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "Unsuccessful response code=" + response.code() + ": " + errorBody);
                        callback.onError("Server error (" + response.code() + "): " + errorBody);
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        Log.d(TAG, "Raw API response: " + responseData.substring(0, Math.min(500, responseData.length())));
                        JSONObject responseObject = new JSONObject(responseData);

                        // Check if there's an error in the response
                        if (responseObject.has("error")) {
                            JSONObject error = responseObject.getJSONObject("error");
                            String errorMsg = error.optString("message", "Unknown API error");
                            Log.e(TAG, "API returned error: " + errorMsg);
                            callback.onError(errorMsg);
                            return;
                        }

                        if (!responseObject.has("candidates")) {
                            Log.e(TAG, "No candidates in response: " + responseData);
                            callback.onError("No response generated. Please try again.");
                            return;
                        }

                        JSONArray candidates = responseObject.getJSONArray("candidates");
                        if (candidates.length() == 0) {
                            callback.onError("Empty response from AI. Please try again.");
                            return;
                        }

                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        String text = parts.getJSONObject(0).getString("text");

                        Log.d(TAG, "Parsed AI response (length=" + text.length() + ")");
                        callback.onSuccess(text);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        callback.onError("Parsing error");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Input processing error");
        }
    }
}
