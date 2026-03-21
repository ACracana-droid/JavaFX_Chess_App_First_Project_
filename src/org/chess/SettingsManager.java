package org.chess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SettingsManager {
    static List<String> parsedFile;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Path SETTINGS_PATH =
            Paths.get(System.getProperty("user.home"), ".mychess", "settings.json");

    public static Settings load() {
        try {
            // Create folder if it doesn't exist
            Files.createDirectories(SETTINGS_PATH.getParent());

            if (!Files.exists(SETTINGS_PATH)) {
                Settings defaultSettings = new Settings();
                save(defaultSettings);
                return defaultSettings;
            }

            Reader reader = Files.newBufferedReader(SETTINGS_PATH);
            return gson.fromJson(reader, Settings.class);

        } catch (IOException e) {
            e.printStackTrace();
            return new Settings(); // fallback
        }
    }

    public static void save(Settings settings) {
        try {
            Files.createDirectories(SETTINGS_PATH.getParent());
            Writer writer = Files.newBufferedWriter(SETTINGS_PATH);
            gson.toJson(settings, writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum Style {
        BLUEISH("/blue-tiles.css"),
        WOOD("/wood-tiles.css");
        final String fileName;

        Style(String fileName) {
            this.fileName = fileName;
        }
    }

    private static void setSettings(String stylesheet, boolean rotateBoard) {
        Settings that = new Settings();
        that.style = stylesheet;
        that.rotateBoard = rotateBoard;
        save(that);
    }
}
