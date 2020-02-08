package com.windsdon.windscord.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.windsdon.windscord.UserRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Link {
	public static LiteralArgumentBuilder<ServerCommandSource> getCommand() {
		return CommandManager.literal("link").executes(Link::execute);
	}

	private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayer();

		String code = UserRegistry.createVerificationRequest(player);

		String commandPrefix = "windscord verify ";
		final String linkCommand = commandPrefix + code;
		Text obfuscatedLinkCode = new LiteralText(code).formatted(Formatting.OBFUSCATED);
		Text linkText = new LiteralText(commandPrefix)
				.append(obfuscatedLinkCode)
				.formatted(Formatting.BLUE)
				.styled(style -> style
						.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, linkCommand))
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy!"))));
		Text text = new LiteralText("Send ").append(linkText).append(" on Discord to link your account.");

		player.sendMessage(text);

		return 0;
	}
}
