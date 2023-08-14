package tech.yfshadaow.spectator;

import java.util.Objects;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import sun.security.krb5.Config;

public class Spectator
        extends JavaPlugin
        implements Listener {

    public void onEnable() {
        this.getLogger().info("观察者已加载");
        Objects.requireNonNull(this.getCommand("pg")).setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
    }

    @EventHandler
    public void cancelCommands(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            if (e.getPlayer().hasPermission("pg.bypass")) {
                return;
            }
            for (String cmd : this.getConfig().getStringList("Commands")) {
                if (!StringUtils.containsIgnoreCase(e.getMessage(), cmd)) continue;
                e.getPlayer().sendMessage("§c你不能在旁观模式下使用这个指令！");
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void preventDamage(EntityDamageEvent e) {
        if (
                e.getCause().equals(EntityDamageEvent.DamageCause.VOID)
                        && e.getEntity() instanceof Player
                        && ((Player) e.getEntity()).getGameMode().equals(GameMode.SPECTATOR)
        ) {
            e.setCancelled(true);
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        FileConfiguration Config = this.getConfig();
        if (cmd.getName().equalsIgnoreCase("pg")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    if (sender.hasPermission("pg.use")) {
                        String uuid = ((Player) sender).getUniqueId().toString();
                        if (!((Player) sender).getGameMode().equals(GameMode.SPECTATOR)) {
                            Config.set("PlayerData." + uuid + ".world", ((Player) sender).getWorld().getName());
                            Config.set("PlayerData." + uuid + ".x", ((Player) sender).getLocation().getX());
                            Config.set("PlayerData." + uuid + ".y", ((Player) sender).getLocation().getY());
                            Config.set("PlayerData." + uuid + ".z", ((Player) sender).getLocation().getZ());
                            Config.set("PlayerData." + uuid + ".yaw", ((Player) sender).getLocation().getYaw());
                            Config.set("PlayerData." + uuid + ".pitch", ((Player) sender).getLocation().getPitch());
                            Config.set("PlayerData." + uuid + ".gamemode", ((Player) sender).getGameMode().toString());
                            this.saveConfig();
                            ((Player) sender).setGameMode(GameMode.SPECTATOR);
                            for (Entity e : ((Player) sender).getPassengers()) {
                                ((Player) sender).removePassenger(e);
                            }
                            sender.sendMessage(ChatColor.YELLOW + "你已进入旁观模式！");
                            return true;
                        }
                        ((Player) sender).teleport(
                                new Location(
                                        this.getServer().getWorld(
                                                Objects.requireNonNull(
                                                        Config.getString("PlayerData." + uuid + ".world")
                                                )
                                        ),
                                        Config.getDouble("PlayerData." + uuid + ".x"),
                                        Config.getDouble("PlayerData." + uuid + ".y"),
                                        this.getConfig().getDouble("PlayerData." + uuid + ".z"),
                                        Float.parseFloat(
                                                Objects.requireNonNull(
                                                        Config.getString("PlayerData." + uuid + ".yaw")
                                                )
                                        ),
                                        Float.parseFloat(
                                                Objects.requireNonNull(
                                                        Config.getString("PlayerData." + uuid + ".pitch")
                                                )
                                        )
                                )
                        );
                        ((Player) sender).setGameMode(
                                GameMode.valueOf(this.getConfig().getString("PlayerData." + uuid + ".gamemode"))
                        );
                        sender.sendMessage(ChatColor.YELLOW + "你已退出旁观模式！");
                        return true;
                    }
                    sender.sendMessage(ChatColor.RED + "你没有权限使用旁观模式！");
                    return false;
                }
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("pg.reload")) {
                        this.reloadConfig();
                        sender.sendMessage(ChatColor.YELLOW + "旁观者配置已重新载入！");
                        return true;
                    }
                    sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "指令输入有误！");
                return false;
            }
        }
        return false;
    }
}
