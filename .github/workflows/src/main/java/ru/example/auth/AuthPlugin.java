package ru.example.auth;

import org.bukkit.plugin.java.JavaPlugin;

public final class AuthPlugin extends JavaPlugin {

    private AuthManager authManager;

    @Override
    public void onEnable() {
        this.authManager = new AuthManager(this);
        this.authManager.load();

        getCommand("reg").setExecutor(new AuthCommand(this, true));
        getCommand("login").setExecutor(new AuthCommand(this, false));

        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        getLogger().info("AuthPlugin включён.");
    }

    @Override
    public void onDisable() {
        if (authManager != null) authManager.save();
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}
