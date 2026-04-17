package com.example.grammarhelper.ai;

import com.example.grammarhelper.model.GrammarError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GrammarResponseParser {
    private final Gson gson = new Gson();

    public List<GrammarError> parseGrammarErrors(String jsonResponse) {
        try {
            // Find start and end of JSON array in case AI included extra text
            int startOfArray = jsonResponse.indexOf("[");
            int endOfArray = jsonResponse.lastIndexOf("]") + 1;
            if (startOfArray == -1 || endOfArray == -1) return new ArrayList<>();
            String jsonArray = jsonResponse.substring(startOfArray, endOfArray);

            Type listType = new TypeToken<ArrayList<GrammarError>>() {}.getType();
            return gson.fromJson(jsonArray, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
