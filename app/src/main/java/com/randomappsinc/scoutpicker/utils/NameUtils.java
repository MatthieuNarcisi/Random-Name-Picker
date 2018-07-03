package com.randomappsinc.scoutpicker.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;

import com.randomappsinc.scoutpicker.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NameUtils {

    // For the choose multiple names at once case. We're just generating indices
    public static List<Integer> getRandomNumsInRange(int numNumbers, int capIndex, Map<String, Integer> nameScores) {
        // Part where we choose the result
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= capIndex; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        if (numNumbers > capIndex) {
            return list;
        }

        // Calculation of the weight
        int totalScore = calculateTotalScoreAndWeight(nameScores);

        Random random = new Random();
        List<Integer> chosenNumbers = new ArrayList<>();

        while (chosenNumbers.size() < numNumbers) {
            int randomWeight = random.nextInt(totalScore) + 1;

            Object[] scores = nameScores.values().toArray();
            for (int i = 0; i < scores.length && randomWeight > 0; i++) {
                int weight = (int)getPercentageForScore(nameScores, (int)scores[i]);
                randomWeight -= weight;

                if (randomWeight <= 0 && !chosenNumbers.contains(i)) {
                    chosenNumbers.add(i);
                }
            }
        }

//        List<Integer> chosenNumbers = new ArrayList<>();
//        for (int i = 0; i < numNumbers; i++) {
//            chosenNumbers.add(list.get(i));
//        }
        return chosenNumbers;
    }

    private static int calculateTotalScoreAndWeight(Map<String, Integer> nameScores) {
        int totalScore = 0;
        for (int score : nameScores.values()) {
            float percentage = getPercentageForScore(nameScores, score);
            totalScore += (int)percentage;
        }

        return totalScore;
    }

    private static float getPercentageForScore(Map<String, Integer> nameScores, int score) {
        int maximumScore = Collections.max(nameScores.values());

        int weight = maximumScore + 2 - (score + 1);

        return (float)weight / (float)(maximumScore + 2) * 100;
    }

    public static String getFileName(String filePath) {
        String[] pieces = filePath.split("/");
        String fileName = pieces[pieces.length - 1];
        return fileName.replace(".txt", "");
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getNamesFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        String contents = convertStreamToString(fileInputStream);
        String[] allNames = contents.split("\\r?\\n");
        StringBuilder namesString = new StringBuilder();
        for (int i = 0; i < allNames.length; i++) {
            if (i != 0) {
                namesString.append("\n");
            }
            namesString.append(allNames[i]);
        }
        fileInputStream.close();
        return namesString.toString();
    }

    public static void copyNamesToClipboard(String names, View parent, int numNames, boolean historyMode) {
        Context context = MyApplication.getAppContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        ClipData clip = ClipData.newPlainText(context.getString(R.string.chosen_names), names);
        clipboard.setPrimaryClip(clip);

        int messageId;
        if (historyMode) {
            messageId = R.string.name_history_copied;
        } else {
            if (numNames > 1) {
                messageId = R.string.copy_confirmation_plural;
            } else {
                messageId = R.string.copy_confirmation_singular;
            }
        }

        if (parent == null) {
            UIUtils.showLongToast(messageId);
        } else {
            UIUtils.showSnackbar(parent, context.getString(messageId));
        }
    }

    public static String[] getNameOptions(String name) {
        Context context = MyApplication.getAppContext();
        String[] options = new String[3];
        options[0] = String.format(context.getString(R.string.rename_person), name);
        options[1] = String.format(context.getString(R.string.delete_name), name);
        options[2] = String.format(context.getString(R.string.duplicate), name);
        return options;
    }

    // Given 0 (1st element in array), returns "1. ", scaling linearly with the input
    public static String getPrefix(int index) {
        return String.valueOf(index + 1) + ". ";
    }
}