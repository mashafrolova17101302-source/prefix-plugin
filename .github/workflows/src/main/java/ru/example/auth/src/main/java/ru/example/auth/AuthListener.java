package ru.example.auth;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import java.time.Duration;
import java.time.LocalTime;

public class AuthListener implements Listener {

    private final AuthPlugin plugin;

    public AuthListener(AuthPlugin plugin) {
        this.plugin = plugin;
    }

    private void msg(Player p, String text) {
        p.sendMessage(Component.text(text.replace('&', '§')));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        AuthManager am = plugin.getAuthManager();

        if (!am.isRegistered(p)) {
            Title title = Title.title(
                    Component.text("Привет. Зарегистрируйся.").color(net.kyori.adventure.text.format.NamedTextColor.RED),
                    Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            p.showTitle(title);
            msg(p, "&cСоздать учётную запись: /reg <свой пароль> <повтор пароля>");
        } else if (!am.isLoggedIn(p)) {
            String greeting = greetingByTime();
            Title title = Title.title(
                    Component.text(greeting).color(net.kyori.adventure.text.format.NamedTextColor.GOLD),
                    Component.text("Войди в аккаунт: /login <пароль>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            p.showTitle(title);
            msg(p, "&e" + greeting + ", войди в аккаунт: &f/login <пароль>");
        }

        // Кик, если не вошёл за 60 сек
        if (!am.isLoggedIn(p)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline() && !plugin.getAuthManager().isLoggedIn(p)) {
                    p.kick(Component.text("Ты не вошёл вовремя."));
                }
            }, 20L * 60);
        }
    }

    private String greetingByTime() {
        int hour = LocalTime.now().getHour();
        if (hour >= 5 && hour < 12) return "Доброе утро";
        if (hour >= 12 && hour < 18) return "Добрый день";
        if (hour >= 18 && hour < 23) return "Добрый вечер";
        return "Доброй ночи";
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getAuthManager().setLoggedIn(e.getPlayer(), false);
    }

    // --- блокировки до входа ---

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        if (!plugin.getAuthManager().isLoggedIn(e.getPlayer())) {
            e.setCancelled(true);
            msg(e.getPlayer(), "&cСначала войди: /login <пароль>");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (plugin.getAuthManager().isLoggedIn(e.getPlayer())) return;
        String cmd = e.getMessage().toLowerCase();
        if (cmd.startsWith("/reg") || cmd.startsWith("/login")) return;
        e.setCancelled(true);
        msg(e.getPlayer(), "&cСначала войди: /login <пароль>");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e) {
        if (plugin.getAuthManager().isLoggedIn(e.getPlayer())) return;
        if (e.getFrom().getBlockX() != e.getTo().getBlockX()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()
                || e.getFrom().getBlockY() != e.getTo().getBlockY()) {
            e.setTo(e.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        if (!plugin.getAuthManager().isLoggedIn(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        if (!plugin.getAuthManager().isLoggedIn(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!plugin.getAuthManager().isLoggedIn(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && !plugin.getAuthManager().isLoggedIn(p)) {
            e.setCancelled(true);
        }
    }
}
