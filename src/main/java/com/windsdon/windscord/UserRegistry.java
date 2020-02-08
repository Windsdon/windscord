package com.windsdon.windscord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserRegistry {
	private static final HashMap<String, ServerPlayerEntity> ephemeralVerificationRequests = new HashMap<>();
	private static final Pattern FILE_PATTERN = Pattern.compile("^([a-z0-9\\-]+)\\.properties$");
	private static final Pattern MESSAGE_VERIFY_PATTERN = Pattern.compile("^windscord verify (\\w+)$");
	private static final HashMap<String, Properties> REGISTRY = new HashMap<>();

	public static String createVerificationRequest(ServerPlayerEntity player) {
		String code = RandomStringUtils.randomAlphabetic(12).toLowerCase();
		ephemeralVerificationRequests.put(code, player);
		return code;
	}

	public static boolean verify(String code, Discord.UserInfo info) {
		if (!ephemeralVerificationRequests.containsKey(code)) {
			return false;
		}

		String uuid = ephemeralVerificationRequests.get(code).getUuidAsString();

		try {
			saveUser(uuid, info);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return true;
	}

	private static void saveUser(String uuid, Discord.UserInfo info) throws IOException {
		Properties properties = info.toProperties();
		Path userPath = resolvePath(uuid);

		properties.store(new FileOutputStream(userPath.toFile()), "uuid: " + uuid);
	}

	private static Path resolvePath(String uuid) {
		return Config.PATH.resolve("users/" + uuid + ".properties");
	}

	public static void reload() throws IOException {
		REGISTRY.clear();
		Path userDirectory = Config.PATH.resolve("users");

		File directory = userDirectory.toFile();

		try {
			//noinspection ResultOfMethodCallIgnored
			directory.mkdirs();
		} catch (Exception ignore) {
		}

		File[] files = directory.listFiles();

		for (File file : files) {
			String uuid = parseUuid(file.getName());
			WindscordLog.LOGGER.info("Loading registry for user {}", uuid);
			Properties properties;

			if (REGISTRY.containsKey(uuid)) {
				properties = REGISTRY.get(uuid);
			} else {
				properties = new Properties();
			}

			properties.load(new FileInputStream(file));

			REGISTRY.put(uuid, properties);
		}
	}

	private static String parseUuid(String name) {
		Matcher matcher = FILE_PATTERN.matcher(name);
		if (!matcher.find()) {
			return null;
		}

		return matcher.group(1);
	}

	public static Discord.UserInfo getUser(String uuid) {
		if (!REGISTRY.containsKey(uuid)) {
			return null;
		}

		return Discord.UserInfo.fromProperties(REGISTRY.get(uuid));
	}

	public static String uuidForDiscordUser(String userId) {
		for (Map.Entry<String, Properties> entry : REGISTRY.entrySet()) {
			if (entry.getValue().getProperty("id").equals(userId)) {
				return entry.getKey();
			}
		}

		return null;
	}

	public static void attemptVerification(MessageReceivedEvent event) {
		String message = event.getMessage().getContentStripped();
		Matcher matcher = MESSAGE_VERIFY_PATTERN.matcher(message);

		if (!matcher.find()) {
			event.getChannel().sendMessage("I don't understand this message").queue();
			return;
		}

		String code = matcher.group(1);
		ServerPlayerEntity entity = ephemeralVerificationRequests.get(code);

		if (entity == null) {
			event.getChannel().sendMessage("That code is invalid").queue();
			return;
		}

		Discord.UserInfo info = new Discord.UserInfo();

		info.username = event.getAuthor().getName();
		info.avatarUrl = event.getAuthor().getEffectiveAvatarUrl();
		info.id = event.getAuthor().getId();

		try {
			saveUser(entity.getUuidAsString(), info);
		} catch (IOException e) {
			event.getChannel().sendMessage("Failed to save user information: " + e.getMessage()).queue();
			return;
		}

		event
				.getChannel()
				.sendMessage("You have successfully linked your accounts. You Minecraft UUID is " + entity.getUuid())
				.queue();

		WindscordMod.handleUserAccountLinked(info, entity);
	}
}
