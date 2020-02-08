package com.windsdon.windscord.mixins;

import com.windsdon.windscord.Utils;
import com.windsdon.windscord.WindscordLog;
import com.windsdon.windscord.WindscordMod;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ChatMessageMixin {
	@Inject(at = @At("HEAD"), method = "onChatMessage")
	private void handleChatMessage(ChatMessageC2SPacket packet, CallbackInfo info) {
		ServerPlayNetworkHandler serverPlayNetworkHandler = Utils.interpretAs(ServerPlayNetworkHandler.class, this);
		String uuid = serverPlayNetworkHandler.player.getUuidAsString();
		if (!Thread.currentThread().getName().contains("Server thread")) {
			return;
		}
		WindscordMod.handleChatMessage(uuid, packet.getChatMessage());
	}
}
