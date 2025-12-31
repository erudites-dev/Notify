package kr.pyke.notify.client.cache;

import net.minecraft.network.chat.Component;

import java.util.*;

public class NameClientCache {
    private static final Map<UUID, Component> CACHE = new HashMap<>();
    private static final List<Runnable> LISTENERS = new ArrayList<>();

    public static void put(UUID uuid, Component name) { CACHE.put(uuid, name); }
    public static void putAll(HashMap<UUID, Component> m) { CACHE.putAll(m); }
    public static Component getOrDefault(UUID uuid, Component def) { return CACHE.getOrDefault(uuid, def); }

    public static void addListener(Runnable r) { if (null != r) {  LISTENERS.add(r); } }
    public static void removeListener(Runnable r) { LISTENERS.remove(r); }
    private static void notifyListeners() {
        for (var r : LISTENERS) {
            try { r.run(); }
            catch (Throwable ignored) { }
        }
    }

    public static Set<UUID> missing(Iterable<UUID> need) {
        var miss = new HashSet<UUID>();
        for (UUID uuid : need) { if (!CACHE.containsKey(uuid)) { miss.add(uuid); } }
        return miss;
    }
}
