package com.randomappsinc.scoutpicker.utils;

import com.randomappsinc.scoutpicker.models.ChoosingSettings;
import com.randomappsinc.scoutpicker.models.ListInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {

    public static final String NAMES_KEY = "names";
    public static final String ALREADY_CHOSEN_NAMES_KEY = "alreadyChosenNames";
    public static final String SETTINGS_KEY = "settings";
    public static final String PRESENTATION_MODE_KEY = "presentationMode";
    public static final String WITH_REPLACEMENT_KEY = "withReplacement";
    public static final String AUTOMATIC_TTS_KEY = "automaticTts";
    public static final String SHOW_AS_LIST_KEY = "showAsList";
    public static final String NUM_NAMES_TO_CHOOSE_KEY = "numNamesToChoose";

    public static final String PERSON_NAME_KEY = "name";
    public static final String PERSON_SCORE_KEY = "score";

    // Given a list of names, converts it to a JSON and stringifies it
    public static String serializeChoosingState(ListInfo currentState, ChoosingSettings choosingSettings) {
        try {
            JSONObject nameListJson = new JSONObject();

            // Store current names in list
            JSONArray namesArray = new JSONArray();
            Map<String, Integer> nameAmounts = currentState.getNameAmounts();
            Map<String, Integer> nameScores = currentState.getNameScores();
            for (String name : currentState.getNames()) {
                int amount = nameAmounts.get(name);
                int score = nameScores.get(name);

                JSONObject person = new JSONObject();
                person.put(PERSON_NAME_KEY, name);
                person.put(PERSON_SCORE_KEY, score);

                for (int i = 0; i < amount; i++) {
                    namesArray.put(person);
                }
            }
            nameListJson.put(NAMES_KEY, namesArray);

            // Store already chosen names
            JSONArray alreadyChosenNamesArray = new JSONArray();
            for (String alreadyChosenName : currentState.getNameHistory()) {
                alreadyChosenNamesArray.put(alreadyChosenName);
            }
            nameListJson.put(ALREADY_CHOSEN_NAMES_KEY, alreadyChosenNamesArray);

            JSONObject settings = new JSONObject();
            settings.put(PRESENTATION_MODE_KEY, choosingSettings.getPresentationMode());
            settings.put(WITH_REPLACEMENT_KEY, choosingSettings.getWithReplacement());
            settings.put(AUTOMATIC_TTS_KEY, choosingSettings.getAutomaticTts());
            settings.put(SHOW_AS_LIST_KEY, choosingSettings.getShowAsList());
            settings.put(NUM_NAMES_TO_CHOOSE_KEY, choosingSettings.getNumNamesToChoose());
            nameListJson.put(SETTINGS_KEY, settings);

            return nameListJson.toString();
        }
        catch (JSONException e) {
            return "";
        }
    }

    // Given a serialized JSON "cache" string of a name list, extracts the list state
    public static ListInfo extractNames(String cachedList) {
        if (cachedList.isEmpty()) {
            return null;
        }

        Map<String, Integer> nameAmounts = new HashMap<>();
        Map<String, Integer> nameScores = new HashMap<>();
        List<String> names = new ArrayList<>();
        int numNames = 0;
        List<String> alreadyChosenNames = new ArrayList<>();
        try {
            JSONObject nameListJson = new JSONObject(cachedList);
            JSONArray namesArray = nameListJson.getJSONArray(NAMES_KEY);
            for (int i = 0; i < namesArray.length(); i++) {
                JSONObject person = namesArray.getJSONObject(i);
                String name = person.getString(PERSON_NAME_KEY);
                if (nameAmounts.containsKey(name)) {
                    int currentAmount = nameAmounts.get(name);
                    nameAmounts.put(name, currentAmount + 1);
                } else {
                    nameAmounts.put(name, 1);
                    names.add(name);
                }

                if (!nameScores.containsKey(name)) {
                    Integer score = person.getInt(PERSON_SCORE_KEY);
                    nameScores.put(name, score);
                }
                numNames++;
            }
            Collections.sort(names);

            JSONArray alreadyChosenNamesArray = nameListJson.getJSONArray(ALREADY_CHOSEN_NAMES_KEY);
            for (int i = 0; i < alreadyChosenNamesArray.length(); i++) {
                alreadyChosenNames.add(alreadyChosenNamesArray.getString(i));
            }
        }
        catch (JSONException ignored) {}
        return new ListInfo(nameAmounts, nameScores, names, numNames, alreadyChosenNames);
    }

    // Given a serialized JSON "cache" string of a name list, extracts the chosen names history
    public static List<String> extractAlreadyChosenNames(String cachedList) {
        List<String> alreadyChosenNames = new ArrayList<>();
        try {
            JSONObject nameListJson = new JSONObject(cachedList);
            JSONArray alreadyChosenNamesArray = nameListJson.getJSONArray(ALREADY_CHOSEN_NAMES_KEY);
            for (int i = 0; i < alreadyChosenNamesArray.length(); i++) {
                alreadyChosenNames.add(alreadyChosenNamesArray.getString(i));
            }
        }
        catch (JSONException ignored) {}
        return alreadyChosenNames;
    }

    // Given a serialized JSON "cache" string of a name list, extracts the settings
    public static ChoosingSettings extractChoosingSettings(String cachedList) {
        ChoosingSettings settings = new ChoosingSettings();
        try {
            JSONObject nameListJson = new JSONObject(cachedList);
            JSONObject settingsJson = nameListJson.getJSONObject(SETTINGS_KEY);
            settings.setWithReplacement(settingsJson.getBoolean(WITH_REPLACEMENT_KEY));
            settings.setNumNamesToChoose(settingsJson.getInt(NUM_NAMES_TO_CHOOSE_KEY));
            settings.setPresentationMode(settingsJson.getBoolean(PRESENTATION_MODE_KEY));
            settings.setAutomaticTts(settingsJson.getBoolean(AUTOMATIC_TTS_KEY));
            settings.setShowAsList(settingsJson.getBoolean(SHOW_AS_LIST_KEY));
        }
        catch (JSONException ignored) {}
        return settings;
    }
}
