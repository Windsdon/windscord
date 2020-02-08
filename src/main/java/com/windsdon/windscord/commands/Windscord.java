package com.windsdon.windscord.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class Windscord {
	public static LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return CommandManager.literal("windscord")
							 .then(Link.getCommand());
	}
}
