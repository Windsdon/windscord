package com.windsdon.windscord;

import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class Config {
	public static final String CONFIG_DISCORD_WEBHOOK = "webhook";
	public static final String CONFIG_DISCORD_TOKEN = "token";
	public static final String CONFIG_DISCORD_CHANNEL = "channel";
	public static final Path PATH = Paths.get("./mods/windscord").toAbsolutePath().normalize();
	private static Properties config = new Properties();
	private static String configFile;

	public static synchronized void loadConfiguration() throws IOException {
		initializeDefaultConfiguration();

		Path configPath = Paths.get("./mods/windscord/config.properties").toAbsolutePath().normalize();
		File configDirectory = new File(PATH.toString());

		if (!configDirectory.exists() && (!configDirectory.mkdirs())) {
			throw new IOException("Cannot access config directory: " + configPath.toString());
		}

		configFile = PATH.resolve("config.properties").toString();
		WindscordLog.LOGGER.log(Level.INFO, "Configuration file: " + configFile);

		try {
			FileInputStream inputStream = new FileInputStream(configFile);
			config.load(inputStream);
		} catch (Exception e) {
			WindscordLog.LOGGER.log(Level.WARN, "Could not load configuration file, initializing with default values");
		}

		saveConfiguration();
	}

	private static synchronized void initializeDefaultConfiguration() {
		config.setProperty(CONFIG_DISCORD_WEBHOOK, "");
		config.setProperty(CONFIG_DISCORD_TOKEN, "");
		config.setProperty(CONFIG_DISCORD_CHANNEL, "");
	}

	public static synchronized String get(String key) {
		return config.getProperty(key);
	}

	public static synchronized void set(String key, String value) {
		config.setProperty(key, value);
		saveConfiguration();
	}

	private static synchronized void saveConfiguration() {
		try {
			FileOutputStream outputStream = new FileOutputStream(configFile);
			config.store(outputStream, "Windscord configuration file");
			outputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to save configuration file: " + e.getMessage());
		}
	}
}
