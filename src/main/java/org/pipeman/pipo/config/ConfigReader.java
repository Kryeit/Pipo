package org.pipeman.pipo.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);

    // Initialize with nulls to make errors more obvious
    public static String DB_URL = null;
    public static String DB_USER = null;
    public static String DB_PASSWORD = null;

    public static void readFile(Path path) throws IOException {
        try {
            String config = readOrCopyFile(path.resolve("config.json"), "/config.json");
            JsonObject configObject = JsonParser.parseString(config).getAsJsonObject();

            // Safely check each field exists before getting it
            if (configObject.has("db-url")) {
                DB_URL = configObject.get("db-url").getAsString();
            } else {
                LOGGER.warn("db-url missing in config file");
            }

            if (configObject.has("db-user")) {
                DB_USER = configObject.get("db-user").getAsString();
            } else {
                LOGGER.warn("db-user missing in config file");
            }

            if (configObject.has("db-password")) {
                DB_PASSWORD = configObject.get("db-password").getAsString();
            } else {
                LOGGER.warn("db-password missing in config file");
            }

        } catch (Exception e) {
            LOGGER.error("Error reading config file", e);
            // Let Config.java handle the defaults - don't set values here
        }
    }

    public static String readOrCopyFile(Path path, String exampleFile) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            LOGGER.info("File does not exist, attempting to copy from resources: " + exampleFile);
            InputStream stream = ConfigReader.class.getResourceAsStream(exampleFile);
            if (stream == null) {
                LOGGER.error("Cannot load example file: " + exampleFile);
                throw new NullPointerException("Cannot load example file");
            }

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            Files.copy(stream, path);
            LOGGER.info("File copied to: " + path.toString());
        } else {
            LOGGER.info("File already exists: " + path.toString());
        }
        return Files.readString(path);
    }
}