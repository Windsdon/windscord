package com.windsdon.windscord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.Properties;

public class Discord {
	private WebhookClient client;
	private JDA jda;

	public void reloadConfiguration() {
		if (client != null) {
			client.close();
			client = null;
		}

		String webhook = Config.get(Config.CONFIG_DISCORD_WEBHOOK);
		try {
			client = WebhookClient.withUrl(webhook);
		} catch (Exception e) {
			throw new RuntimeException("Failed to reload Discord client", e);
		}

		String token = Config.get(Config.CONFIG_DISCORD_TOKEN);
		try {
			jda = new JDABuilder(token).addEventListeners(new Listener()).build();
		} catch (LoginException e) {
			throw new RuntimeException("Failed to login to Discord", e);
		}
	}

	public void sendMessage(String message) {
		if (client == null) {
			return;
		}

		client.send(message);
	}

	public void sendMessage(UserInfo userInfo, String message) {
		WebhookMessageBuilder builder = new WebhookMessageBuilder();
		builder.setAvatarUrl(userInfo.avatarUrl).setUsername(userInfo.username).setContent(message);
		client.send(builder.build());
	}

	public static class UserInfo {
		public String avatarUrl;
		public String username;
		public String id;

		static UserInfo fromProperties(Properties properties) {
			UserInfo userInfo = new UserInfo();
			userInfo.avatarUrl = properties.getProperty("avatarUrl");
			userInfo.username = properties.getProperty("username");
			userInfo.id = properties.getProperty("id");

			return userInfo;
		}

		public Properties toProperties() {
			Properties properties = new Properties();
			properties.setProperty("avatarUrl", avatarUrl);
			properties.setProperty("username", username);
			properties.setProperty("id", id);

			return properties;
		}
	}

	private static class Listener extends ListenerAdapter {
		@Override
		public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
			WindscordMod.handleDiscordMessage(event);
		}

		@Override
		public void onReady(@Nonnull ReadyEvent event) {
			WindscordLog.LOGGER.info("Discord bot ready");
		}
	}
}
