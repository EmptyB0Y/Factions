package com.redsifter.factions.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class FileManager {
    private FileConfiguration dataConfig = null;
    private File configFile;
    private String name;

    public FileManager(String name) throws IOException {
        this.name = name;
        saveDefaultConfig();
    }

    public void reloadConfig() throws FileNotFoundException {
        if (this.configFile == null) {
            this.configFile = new File ("plugins/Factions/", this.name);
        }
        this.dataConfig= YamlConfiguration.loadConfiguration(this.configFile);
        FileInputStream defaultStream = new FileInputStream("plugins/Factions/"+this.name);
        if(defaultStream != null){
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() throws FileNotFoundException {
        if(this.dataConfig == null){
            reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() throws IOException {
        if(this.dataConfig == null || this.configFile == null){
            return;
        }
        this.getConfig().save(this.configFile);
    }

    public void saveDefaultConfig() throws IOException {
        if(this.configFile == null || !this.configFile.exists()){
            this.configFile = new File("plugins/Factions/", this.name);
            this.configFile.createNewFile();
        }
    }
}
