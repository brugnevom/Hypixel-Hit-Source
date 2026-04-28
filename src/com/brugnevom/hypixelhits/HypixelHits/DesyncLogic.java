package com.brugnevom.hypixelhits.HypixelHits;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class DesyncLogic extends PacketAdapter {
    private final LocationHistoryManager history;
    private final UUID attackerUUID;
    private final UUID victimUUID;

    public DesyncLogic(Plugin plugin, LocationHistoryManager history, Player attacker, Player victim) {
        super(plugin, ListenerPriority.HIGHEST, 
              PacketType.Play.Server.ENTITY_TELEPORT, 
              PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
              PacketType.Play.Server.REL_ENTITY_MOVE);
        this.history = history;
        this.attackerUUID = attacker.getUniqueId();
        this.victimUUID = victim.getUniqueId();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        int entityId = packet.getIntegers().read(0);
        Player receiver = event.getPlayer();

        // 1. BACKTRACK: Attacker sees Victim in the past
        if (receiver.getUniqueId().equals(attackerUUID) && isEntity(entityId, victimUUID)) {
            org.bukkit.Location pastLoc = history.getPastLocation(victimUUID, 20);
            if (pastLoc != null) {
                modifyLocationPacket(packet, pastLoc);
            }
        }

        if (receiver.getUniqueId().equals(victimUUID) && isEntity(entityId, attackerUUID)) {
            double x = packet.getDoubles().read(0);
            double z = packet.getDoubles().read(2);
            
            Vector push = receiver.getLocation().getDirection().multiply(-2.0); 
            packet.getDoubles().write(0, x + push.getX());
            packet.getDoubles().write(2, z + push.getZ());
        }
    }

    private boolean isEntity(int id, UUID uuid) {
        Player p = org.bukkit.Bukkit.getPlayer(uuid);
        return p != null && p.getEntityId() == id;
    }

    private void modifyLocationPacket(PacketContainer packet, org.bukkit.Location loc) {
        packet.getDoubles().write(0, loc.getX());
        packet.getDoubles().write(1, loc.getY());
        packet.getDoubles().write(2, loc.getZ());
    }
}