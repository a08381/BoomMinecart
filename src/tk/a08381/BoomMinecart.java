/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.a08381;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a08381
 */
public class BoomMinecart extends JavaPlugin  {
    
    private static Listener hmboom = new HMBOOM();
    public static boolean isLockette = false;
    public static YamlConfiguration config = new YamlConfiguration();
    //public static boolean bonebugfix = false;

    public void onEnable() {
        try {
            URL url = new URL ("http://a08381.tk/version/BoomMinecart.yml");
            YamlConfiguration _temp = new YamlConfiguration();
            _temp.load(url.openStream());
            double version = _temp.getDouble("version");
            _temp.load(this.getClass().getResourceAsStream("/plugin.yml"));
            if (_temp.getDouble("version") < version) {
                URL _url = new URL("http://a08381.tk/plugins/BoomMinecart.jar");
                BufferedInputStream bis = new BufferedInputStream(_url.openStream());
                FileOutputStream fos = new FileOutputStream(new File(Bukkit.getUpdateFolder(), "/BoomMinecart.jar"));
                byte[] buffer = new byte[4096];
                int count = 0;
                while ((count = bis.read(buffer, 0, count)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                bis.close();
            }
        } catch (InvalidConfigurationException | IOException ex) {
            Logger.getLogger(BoomMinecart.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(Bukkit.getPluginManager().getPlugin("Lockette") != null){
            getLogger().info("发现Lockette");
            isLockette = true;
            getLogger().info("现在只有被Lockette保护的容器受到BoomMinecart保护");
        } else {
            getLogger().info("未发现Lockette");
            getLogger().info("现在所有容器都将受到BoomMinecart保护");
        }
        File file = Bukkit.getPluginManager().getPlugin("BoomMinecart").getDataFolder();
        if(!file.exists()){
            file.mkdir();
        }
        file = new File (Bukkit.getPluginManager().getPlugin("BoomMinecart").getDataFolder(), "/config.yml");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BoomMinecart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            config.load(file);
            config.set("Boom.enable", config.getBoolean("Boom.enable", true));
            config.set("Boom.power", config.getDouble("Boom.power", 8));
            config.set("Boom.break", config.getBoolean("Boom.break", false));
            config.set("Nearby.around", config.getInt("Nearby.around", 10));
            config.set("Server.DEADWAR.NMobLimit.world", config.getList("Server.DEADWAR.NMobLimit.world", Arrays.asList("world_nether")));
            config.set("Server.DEADWAR.NMobLimit.hight", config.getDouble("Server.DEADWAR.NMobLimit.hight", 128));
            config.set("Server.DEADWAR.CommandFix", config.getBoolean("Server.DEADWAR.CommandFix", true));
            config.set("Server.DEADWAR.PlotMe.ProtectAnimals.enable", config.getBoolean("Server.DEADWAR.PlotMe.ProtectAnimals.enable", true));
            config.set("Server.DEADWAR.PlotMe.ProtectAnimals.ProtectRoad", config.getBoolean("Server.DEADWAR.PlotMe.ProtectAnimals.ProtectRoad", true));
            config.set("Server.DEADWAR.PlotMe.ProtectSoil", config.getBoolean("Server.DEADWAR.PlotMe.ProtectSoil", true));
            config.save(file);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(BoomMinecart.class.getName()).log(Level.SEVERE, null, ex);
        }
        getServer().getPluginManager().registerEvents(hmboom, this);
        getLogger().info("BoomMinecart准备完毕！请指示！");
    }

    public void onDisable() {
        HandlerList.unregisterAll(hmboom);
        getLogger().info("BoomMinecart武装解除完毕！");
    }
    
}
