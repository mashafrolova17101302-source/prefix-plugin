package ru.example.auth;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {

    private final AuthPlugin plugin;
    private final boolean register;

    public AuthCommand(AuthPlugin plugin, boolean register) {
        this.plugin = plugin;
        this.register = register;
    }

    private void msg(CommandSender to, String text) {
        to.sendMessage(Component.text(text.replace('&', '§')));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            msg(sender, "&cТолько для игроков.");
            return true;
        }
        AuthManager am = plugin.getAuthManager();

        if (register) {
            if (am.isRegistered(p)) { msg(p, "&cТы уже зарегистрирован. Войди: /login <пароль>"); return true; }
            if (args.length < 2) { msg(p, "&e/reg <пароль> <повтор>"); return true; }
            if (!args[0].equals(args[1])) { msg(p, "&cПароли не совпадают."); return true; }
            if (args[0].length() < 4) { msg(p, "&cПароль минимум 4 символа."); return true; }
            am.register(p, args[0]);
            am.setLoggedIn(p, true);
            msg(p, "&aУчётная запись создана! Ты вошёл.");
        } else {
            if (!am.isRegistered(p)) { msg(p, "&cСначала зарегистрируйся: /reg <пароль> <повтор>"); return true; }
            if (am.isLoggedIn(p)) { msg(p, "&eТы уже вошёл."); return true; }
            if (args.length < 1) { msg(p, "&e/login <пароль>"); return true; }
            if (am.check(p, args[0])) {
                am.setLoggedIn(p, true);
                msg(p, "&aВход выполнен. Добро пожаловать!");
            } else {
                msg(p, "&cНеверный пароль.");
            }
        }
        return true;
    }
}
