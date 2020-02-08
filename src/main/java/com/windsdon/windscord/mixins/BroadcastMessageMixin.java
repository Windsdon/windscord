package com.windsdon.windscord.mixins;

import com.windsdon.windscord.WindscordMod;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class BroadcastMessageMixin {
	@Inject(at = @At("HEAD"), method = "sendToAll(Lnet/minecraft/text/Text;)V")
	public void windscordHandleSendToAll(Text text, CallbackInfo info) {
		WindscordMod.handleBroadcastMessage(text);
	}
}
