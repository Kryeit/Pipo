package org.pipeman.pipo;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.pipeman.pipo.offline.Offlines;

import java.util.TimerTask;
import java.util.UUID;

public class Autorole extends TimerTask {
    public final static String KRYEITOR = "1009788977894666240";
    public final static String COLLABORATOR = "1041751895062089779";
    public final static String BOOSTER = "1019980287867887720";

    private final Guild guild;
    private final Role role;

    public Autorole(Role role) {
        this.guild = Pipo.JDA.getGuildById(Pipo.KRYEIT_GUILD);
        this.role = role;
    }

    @Override
    public void run() {
        guild.loadMembers(member -> {
            UUID id = Utils.getMinecraftId(member);
            if (id == null) return;
            if (member.getRoles().contains(role)) {
                Utils.isPlayerOnGroup(id, role.getName().toLowerCase()).thenAcceptAsync(result -> {
                    if (!result)
                        Utils.addGroup(id, role.getName().toLowerCase());
                });
            }
        });

        Offlines.getPlayerNames().stream().iterator().forEachRemaining(name -> {
            UUID id = Offlines.getUUIDbyName(name).orElse(null);
            if (id == null) return;
            if (!Pipo.getInstance().discordRegistry.hasPlayer(id)) {
                Utils.isPlayerOnGroup(id, role.getName().toLowerCase()).thenAcceptAsync(result -> {
                    if (result)
                        Utils.removeGroup(id, role.getName().toLowerCase());
                });
            }
        });
    }
}
