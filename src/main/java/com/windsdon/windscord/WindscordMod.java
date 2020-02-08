package com.windsdon.windscord;

import com.mojang.authlib.GameProfile;
import com.windsdon.windscord.commands.Windscord;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.UUID;

public class WindscordMod implements ModInitializer {
	public static final Discord DISCORD = new Discord();
	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		try {
			Config.loadConfiguration();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load configuration, caused by " + e.getMessage());
		}

		DISCORD.reloadConfiguration();

		registerCommands();
		registerEventListener();

		try {
			UserRegistry.reload();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		MessageQueue.begin();
	}

	private void registerEventListener() {
		ServerStartCallback.EVENT.register(minecraftServer -> WindscordMod.server = minecraftServer);
	}

	private void registerCommands() {
		CommandRegistry.INSTANCE.register(false, dispatcher -> {
			dispatcher.register(Windscord.getCommand());
		});
	}

	public static void handleChatMessage(String uuid, String message) {
		if (message.startsWith("/")) {
			return;
		}

		MessageQueue.enqueue(uuid, message);
	}

	public static void handleBroadcastMessage(Text text) {
		// ignore self messages
		if (text instanceof WindscordText) {
			return;
		}

		MessageQueue.enqueue(text);
	}

	public static void handleDiscordMessage(MessageReceivedEvent event) {
		// ignore self messages
		if (event.getJDA().getSelfUser().getId().equals(event.getAuthor().getId())) {
			return;
		}

		if (event.isWebhookMessage()) {
			// no webhooks here
			return;
		}

		if (event.isFromType(ChannelType.PRIVATE)) {
			UserRegistry.attemptVerification(event);
			return;
		}

		if (!event.getMessage().getChannel().getId().equals(Config.get(Config.CONFIG_DISCORD_CHANNEL))) {
			return;
		}

		String userId = event.getAuthor().getId();
		String uuid = UserRegistry.uuidForDiscordUser(userId);
		String message = event.getMessage().getContentDisplay();

		String username = event.getAuthor().getName();

		if (uuid != null) {
			GameProfile profile = server.getUserCache().getByUuid(UUID.fromString(uuid));
			if (profile != null) {
				username = profile.getName();
			}
		}

		server
				.getPlayerManager()
				.sendToAll(new WindscordText("")
						.append(new LiteralText("[D] ").formatted(Formatting.AQUA))
						.append(new LiteralText("<" + username + "> " + message).formatted(Formatting.RESET)));
	}

	public static void handleUserAccountLinked(Discord.UserInfo info, ServerPlayerEntity entity) {
		server
				.getPlayerManager()
				.sendToAll(new WindscordText("")
						.append(entity.getDisplayName())
						.append(" is ")
						.append(new LiteralText(info.username).formatted(Formatting.RED))
						.append(" on Discord"));
	}
}
