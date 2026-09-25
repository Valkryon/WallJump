package me.arthed.walljump.config;

import me.arthed.walljump.WallJump;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallJumpConfiguration extends YamlConfiguration {

    private final File configFile;
    private Map<String, Object> data;

    public WallJumpConfiguration(String fileName) {
        super();

        WallJump plugin = WallJump.getInstance();
        configFile = new File(plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }
        reload();

        InputStream defaultConfigInputStream = WallJump.class.getResourceAsStream("/" + fileName);
        InputStreamReader defaultConfigReader = new InputStreamReader(defaultConfigInputStream, StandardCharsets.UTF_8);
        setDefaults(YamlConfiguration.loadConfiguration(defaultConfigReader));
    }

    public void reload() {
        try {
            load(configFile);
            data = new HashMap<>();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            save(configFile);
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public Material getMaterial(String path) {
        if(data.containsKey(path))
            return (Material)data.get(path);

        Material result = Material.matchMaterial(getString(path));
        data.put(path, result);
        return result;
    }

    public List<Material> getMaterialList(String path) {
        if(data.containsKey(path))
            return (List<Material>)data.get(path);

        List<Material> result = new ArrayList<>();
        for(String materialName : getStringList(path)) {
            Material material = Material.matchMaterial(materialName);
            if(material != null)
                result.add(material);
        }
        data.put(path, result);
        return result;
    }

    /**
     * Gets a number from the config, using the value set for the given block in "blockSettings" if there is one.
     */
    public double getBlockDouble(String path, Material block) {
        ConfigurationSection blockSection = getBlockSettings().get(block);
        if(blockSection != null && blockSection.isSet(path))
            return blockSection.getDouble(path);
        return getDouble(path);
    }

    private Map<Material, ConfigurationSection> getBlockSettings() {
        if(data.containsKey("blockSettings"))
            return (Map<Material, ConfigurationSection>)data.get("blockSettings");

        Map<Material, ConfigurationSection> result = new EnumMap<>(Material.class);
        //read without falling back to the default config, so block settings are only used when they are in config.yml
        if(get("blockSettings", null) instanceof ConfigurationSection blockSettings) {
            for(String materialName : blockSettings.getKeys(false)) {
                Material material = Material.matchMaterial(materialName);
                ConfigurationSection section = blockSettings.getConfigurationSection(materialName);
                if(material == null || section == null) {
                    WallJump.getInstance().getLogger().warning("Unknown block in blockSettings: " + materialName);
                    continue;
                }
                result.put(material, section);
            }
        }
        data.put("blockSettings", result);
        return result;
    }

    public World getWorld(String path) {
        if(data.containsKey(path))
            return (World)data.get(path);

        World result = Bukkit.getWorld(getString(path));
        data.put(path, result);
        return result;
    }

    public List<World> getWorldList(String path) {
        if(data.containsKey(path))
            return (List<World>)data.get(path);

        List<World> result = new ArrayList<>();
        for(String worldName : getStringList(path)) {
            World world = Bukkit.getWorld(worldName);
            if(world != null)
                result.add(world);
        }
        data.put(path, result);
        return result;
    }

}
