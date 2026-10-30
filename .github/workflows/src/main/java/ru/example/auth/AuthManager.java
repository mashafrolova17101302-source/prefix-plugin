package ru.example.auth;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class AuthManager {

    private final JavaPlugin plugin;
    private final Map<UUID, String> hashes = new HashMap<>(); // uuid -> "salt:hash"
    private final Set<UUID> loggedIn = new HashSet<>();

    private File file;
    private FileConfiguration cfg;

    public AuthManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "users.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не создать users.yml: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        hashes.clear();
        for (String key : cfg.getKeys(false)) {
            hashes.put(UUID.fromString(key), cfg.getString(key));
        }
    }

    public void save() {
        if (cfg == null || file == null) return;
        for (String key : new ArrayList<>(cfg.getKeys(false))) cfg.set(key, null);
        hashes.forEach((uuid, h) -> cfg.set(uuid.toString(), h));
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Не сохранить users.yml: " + e.getMessage());
        }
    }

    public boolean isRegistered(Player p) {
        return hashes.containsKey(p.getUniqueId());
    }

    public boolean isLoggedIn(Player p) {
        return loggedIn.contains(p.getUniqueId());
    }

    public void setLoggedIn(Player p, boolean v) {
        if (v) loggedIn.add(p.getUniqueId());
        else loggedIn.remove(p.getUniqueId());
    }

    public void register(Player p, String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String saltHex = toHex(salt);
        String hash = hash(password, salt);
        hashes.put(p.getUniqueId(), saltHex + ":" + hash);
        save();
    }

    public boolean check(Player p, String password) {
        String stored = hashes.get(p.getUniqueId());
        if (stored == null) return false;
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;
        String saltHex = parts[0];
        String expected = parts[1];
        String actual = hash(password, fromHex(saltHex));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private String hash(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }
}
