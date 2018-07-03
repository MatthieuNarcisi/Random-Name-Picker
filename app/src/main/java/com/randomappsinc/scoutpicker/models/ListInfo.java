package com.randomappsinc.scoutpicker.models;

import android.util.Log;

import com.randomappsinc.scoutpicker.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Represents the choosing state of a name list */
public class ListInfo {

    private Map<String, Integer> nameAmounts;
    private Map<String, Integer> nameScores;
    private List<String> names;
    private int numInstances;
    private List<String> nameHistory;

    private int totalScore;

    public ListInfo(Map<String, Integer> nameAmounts, Map<String, Integer> nameScores, List<String> names, int numInstances, List<String> history) {
        this.totalScore = -1;
        this.nameAmounts = nameAmounts;
        this.nameScores = nameScores;
        this.names = names;
        this.numInstances = numInstances;
        this.nameHistory = history;
    }

    public Map<String, Integer> getNameAmounts() {
        return nameAmounts;
    }

    public Map<String, Integer> getNameScores() { return nameScores; }

    public List<String> getNames() {
        return names;
    }

    public List<String> getNameHistory() {
        return nameHistory;
    }

    public List<String> getLongList() {
        List<String> longList = new ArrayList<>();
        for (String name : names) {
            int amount = nameAmounts.get(name);
            for (int i = 0; i < amount; i++) {
                longList.add(name);
            }
        }
        return longList;
    }

    public void addNames(String name, int amount) {
        this.totalScore = -1;
        if (nameAmounts.containsKey(name)) {
            int currentAmount = nameAmounts.get(name);
            nameAmounts.put(name, amount + currentAmount);
        } else {
            nameAmounts.put(name, amount);
            nameScores.put(name, 0);
            names.add(name);
            Collections.sort(names);
        }
        numInstances += amount;
    }

    public void removeNames(String name, int amount) {
        this.totalScore = -1;
        if (nameAmounts.containsKey(name)) {
            int currentAmount = nameAmounts.get(name);
            if (currentAmount - amount <= 0) {
                nameAmounts.remove(name);
                nameScores.remove(name);
                names.remove(name);
            } else {
                nameAmounts.put(name, currentAmount - amount);
            }
        }
        numInstances -= amount;
    }

    public void renamePeople(String oldName, String newName, int amount) {
        removeNames(oldName, amount);
        addNames(newName, amount);
    }

    public String getName(int position) {
        return names.get(position);
    }

    public String getNameText(int position) {
        this.calculateTotalScoreAndWeight();

        String name = names.get(position);
        int amount = nameAmounts.get(name);
        int score = nameScores.get(name);
        String base = name;


        if (score != 0) {
            base = score + " - " + base;
        }

        base = amount == 1 ? base : base + " (" + String.valueOf(amount) + ")";

        if (this.totalScore != 0) {
            float percent = this.getPercentageForScore(score) / (float)this.totalScore * 100;
            return base + " (" + String.format("%.3g", percent) + "%)";
        }

        return base;
    }

    public int getNumNames() {
        return names.size();
    }

    public int getNumInstances() {
        return numInstances;
    }

    public void removeAllInstancesOfName(int position) {
        this.totalScore = -1;
        String name = getName(position);
        int amount = nameAmounts.get(name);
        removeNames(name, amount);
    }

    public void addPointAtPosition(int position) {
        this.totalScore = -1;
        String name = getName(position);
        int score = nameScores.get(name);
        nameScores.put(name, score + 1);
    }

    public void removePointAtPosition(int position) {
        this.totalScore = -1;
        String name = getName(position);
        int score = nameScores.get(name);

        if (score > 0) {
            nameScores.put(name, score - 1);
        }
    }

    public String chooseNames(List<Integer> indexes, ChoosingSettings settings) {
        StringBuilder namesText = new StringBuilder();
        List<String> allNames = getLongList();
        for (int i = 0; i < indexes.size(); i++) {
            if (i != 0) {
                namesText.append("\n");
            }
            if (settings.getShowAsList()) {
                namesText.append(NameUtils.getPrefix(i));
            }

            String chosenName = allNames.get(indexes.get(i));
            namesText.append(chosenName);
            nameHistory.add(chosenName);
            if (!settings.getWithReplacement()) {
                removeNames(chosenName, 1);
            }
        }
        return namesText.toString();
    }

    public int getInstancesOfName(String name) {
        return nameAmounts.get(name);
    }

    private void calculateTotalScoreAndWeight() {
        if (totalScore == -1) {
            this.totalScore = 0;
            for (int score : nameScores.values()) {
                float percentage = getPercentageForScore(score);
                this.totalScore += (int)percentage;
            }
        }
    }

    private float getPercentageForScore(int score) {
        int maximumScore = Collections.max(nameScores.values());

        int weight = maximumScore + 2 - (score + 1);

        return (float)weight / (float)(maximumScore + 2) * 100;
    }
}
