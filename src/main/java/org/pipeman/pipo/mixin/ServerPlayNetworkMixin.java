package org.pipeman.pipo.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.pipeman.pipo.afk.AfkPlayer;
import org.pipeman.pipo.afk.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.pipeman.pipo.Pipo.lastActiveTime;

// This class has been mostly made by afkdisplay mod
// https://github.com/beabfc/afkdisplay
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateAfkStatus(CallbackInfo ci) {
        AfkPlayer afkPlayer = (AfkPlayer) player;
        int timeoutSeconds = Config.PacketOptions.timeoutSeconds;
        if (afkPlayer.pipo$isAfk() || timeoutSeconds <= 0) return;
        if (!lastActiveTime.containsKey(player.getUuid())) return;
        long afkDuration = System.currentTimeMillis() - lastActiveTime.get(player.getUuid());
        if (afkDuration > timeoutSeconds * 1000L) {
            afkPlayer.pipo$enableAfk();
        }
    }

    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void checkPlayerLook(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (Config.PacketOptions.resetOnLook && packet.changesLook()) {
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            if (pitch != packet.getPitch(pitch) || yaw != packet.getYaw(yaw)) lastActiveTime.put(player.getUuid(), System.currentTimeMillis());
        }
    }
}
