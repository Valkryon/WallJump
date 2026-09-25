package me.arthed.walljump.player;

import me.arthed.walljump.WallJump;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerManager {

    private final Map<Player, WPlayer> players = new HashMap<>();

    //toggles saved by older versions of WallJump, moved into each player's data the next time they join
    private final File legacyDataFile;
    private YamlConfiguration legacyData;

    public PlayerManager() {
        legacyDataFile = new File(WallJump.getInstance().getDataFolder(), "data.yml");
        if(legacyDataFile.exists())
            legacyData = YamlConfiguration.loadConfiguration(legacyDataFile);
    }

    public void registerPlayer(Player player) {
        WPlayer wplayer = new WPlayer(player);
        migrateLegacyToggle(wplayer);
        players.put(player, wplayer);
    }

    public void unregisterPlayer(Player player) {
        WPlayer wplayer = players.remove(player);
        if(wplayer != null)
            wplayer.stopWallJumping();
    }

    public WPlayer getWPlayer(Player player) {
        return players.get(player);
    }

    public Collection<WPlayer> getWPlayers() {
        return players.values();
    }

    private void migrateLegacyToggle(WPlayer wplayer) {
        String uuid = wplayer.getPlayer().getUniqueId().toString();
        if(legacyData == null || !legacyData.contains(uuid))
            return;

        wplayer.setEnabled(legacyData.getBoolean(uuid));
        legacyData.set(uuid, null);
        try {
            if(legacyData.getKeys(false).isEmpty()) {
                legacyData = null;
                if(!legacyDataFile.delete())
                    legacyDataFile.deleteOnExit();
            } else {
                legacyData.save(legacyDataFile);
            }
        } catch(IOException e) {
            WallJump.getInstance().getLogger().log(Level.WARNING, "Failed to update data.yml", e);
        }
    }

}
