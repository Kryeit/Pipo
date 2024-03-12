package org.pipeman.pipo.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.pipeman.pipo.afk.AfkPlayer;
import org.pipeman.pipo.afk.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.pipeman.pipo.Pipo.lastActiveTime;

// This class has been mostly made by afkdisplay mod
// https://github.com/beabfc/afkdisplay
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends Entity implements AfkPlayer {
    @Shadow
    @Final
    public MinecraftServer server;
    @Unique
    public ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    @Unique
    private boolean isAfk;

    public ServerPlayerMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    public boolean pipo$isAfk() {
        return this.isAfk;
    }

    @Unique
    public void pipo$enableAfk() {
        if (this.pipo$isAfk()) return;
        setAfk(true);
    }

    @Unique
    public void pipo$disableAfk() {
        if (!isAfk) return;
        lastActiveTime.put(player.getUuid(), System.currentTimeMillis());
        setAfk(false);
    }

    @Unique
    private void setAfk(boolean isAfk) {
        this.isAfk = isAfk;
    }

    @Inject(method = "updateLastActionTime", at = @At("TAIL"))
    private void onActionTimeUpdate(CallbackInfo ci) {
        this.pipo$disableAfk();
    }

    public void setPosition(double x, double y, double z) {
        if (Config.PacketOptions.resetOnMovement && (this.getX() != x || this.getY() != y || this.getZ() != z)) {
            player.updateLastActionTime();
        }
        super.setPosition(x, y, z);
    }
}
