package me.arthed.walljump;

import me.arthed.walljump.api.WallJumpAPI;
import me.arthed.walljump.command.WallJumpCommand;
import me.arthed.walljump.config.WallJumpConfiguration;
import me.arthed.walljump.handlers.OtherPluginsHandler;
import me.arthed.walljump.handlers.WorldGuardHandler;
import me.arthed.walljump.listeners.*;
import me.arthed.walljump.player.PlayerManager;
import me.arthed.walljump.player.WPlayer;
import me.arthed.walljump.utils.AntiCheatUtils;
import me.arthed.walljump.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;


public final class WallJump extends JavaPlugin {

    private static WallJump plugin;
    public static WallJump getInstance() {
        return plugin;
    }

    private WallJumpAPI api;
    public WallJumpAPI getAPI() {
        return api;
    }

    private PlayerManager playerManager;
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    private WallJumpConfiguration config;
    public WallJumpConfiguration getWallJumpConfig() {
        return config;
    }

    private WallJumpConfiguration dataConfig;
    public WallJumpConfiguration getDataConfig() {
        return dataConfig;
    }

    private WorldGuardHandler worldGuard;
    public WorldGuardHandler getWorldGuardHandler() {
        return worldGuard;
    }

    @Override
    public void onEnable() {
        playerManager = new PlayerManager();

        registerEvents(
                new PlayerJoinListener(),
                new PlayerQuitListener(),
                new PlayerToggleSneakListener(),
                new PlayerDamageListener(),
                new OtherPluginsHandler()
        );

        this.getCommand("walljump").setExecutor(new WallJumpCommand());

        //in case the plugin has been loaded while the server is running using plugman or any other similar methods, register all the online players
        for(Player player : Bukkit.getOnlinePlayers()) {
            playerManager.registerPlayer(player);
        }

        new Metrics(this, 10126);

        UpdateChecker updateChecker = new UpdateChecker(this);
        if(!config.getBoolean("ignoreUpdates"))
            updateChecker.checkUpdates();

        new AntiCheatUtils();

        api = new WallJumpAPI();
    }

    @Override
    public void onLoad() {
        plugin = this;
        config = new WallJumpConfiguration("config.yml");
        dataConfig = new WallJumpConfiguration("data.yml");

        if(getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                worldGuard = new WorldGuardHandler();
            } catch(LinkageError e) {
                getLogger().log(Level.WARNING, "Failed to hook into WorldGuard, the wall-jump flag will not work", e);
            }
        }
    }

    @Override
    public void onDisable() {
        if(config.getBoolean("toggleCommand")) {
            for (WPlayer wplayer : playerManager.getWPlayers()) {
                dataConfig.set(wplayer.getPlayer().getUniqueId().toString(), wplayer.enabled);
            }
            dataConfig.save();
        }
    }

    private void registerEvents(Listener... listeners) {
        for(Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
