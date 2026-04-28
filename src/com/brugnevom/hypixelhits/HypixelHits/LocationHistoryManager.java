package com.brugnevom.hypixelhits.HypixelHits;

import org.bukkit.Location;
import java.util.*;

public class LocationHistoryManager {
    private final Map<UUID, Deque<Location>> history = new HashMap<>();
    private final int MAX_TICKS = 20;

    public void record(UUID uuid, Location loc) {
        history.computeIfAbsent(uuid, k -> new LinkedList<>()).addFirst(loc.clone());
        if (history.get(uuid).size() > MAX_TICKS) {
            history.get(uuid).removeLast();
        }
    }

    public Location getPastLocation(UUID uuid, int ticksAgo) {
        Deque<Location> locs = history.get(uuid);
        if (locs == null || locs.isEmpty()) return null;
        
        List<Location> list = new ArrayList<>(locs);
        int index = Math.min(ticksAgo, list.size() - 1);
        return list.get(index);
    }
}