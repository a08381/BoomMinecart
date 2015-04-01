/*
 * To change this license header, choose License Headers in Project Properties.	
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.a08381;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.yi.acru.bukkit.Lockette.Lockette;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a08381
 */
public class HMBOOM implements Listener  {
    
    private HopperMinecart entity;
    private SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private InventoryMoveItemEvent ev;

    @EventHandler (ignoreCancelled = true)
    public void HopperMining(InventoryMoveItemEvent event) {
        if (BoomMinecart.config.getBoolean("Boom.enable")) {
            if (BoomMinecart.isLockette) {
                if (event.getSource().getHolder() instanceof BlockState) {
                    BlockState block = (BlockState) event.getSource().getHolder();
                    if (Lockette.isProtected(block.getBlock())) {
                        ev = event;
                        Boom();
                    }
                } else if (event.getSource().getHolder() instanceof DoubleChest) {
                    DoubleChest block = (DoubleChest) event.getSource().getHolder();
                    if (Lockette.isProtected(block.getWorld().getBlockAt(block.getLocation()))) {
                        ev = event;
                        Boom();
                    }
                }
            } else {
                ev = event;
                Boom();
            }
        }
    }
    
    @EventHandler (ignoreCancelled = true)
    public void command(AsyncPlayerChatEvent event) {
        if (event.getMessage().toLowerCase().trim().matches("^see\\s+boom\\s+log.*")
                & event.getPlayer().hasPermission("boomminecart.seelog")){
            event.setCancelled(true);
            List<String> logs = new ArrayList<>();
            File file = Bukkit.getPluginManager().getPlugin("BoomMinecart").getDataFolder();
            String[] args;
            args = file.list();
            for (String str : args){
                if (str.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.yml")) {
                    int d = str.lastIndexOf(".");
                    if (d>-1 && d<str.length()) {
                        logs.add(str.substring(0, d));
                    }
                }
            }
            if (event.getMessage().toLowerCase().trim().matches("^see\\s+boom\\s+log$")) {
                Player sender = event.getPlayer();
                sender.sendMessage("§6后台为您扫描到 §c"+logs.size()+" §6个  矿车记录");
                if (!logs.isEmpty()) {
                    if (logs.size()<=10) {
                        for (String log : logs) {
                            sender.sendMessage("§6"+log);
                        }
                    } else {
                        for (String log : logs.subList(0, 10)) {
                            sender.sendMessage("§6"+log);
                        }
                        sender.sendMessage("§7......省略 "+(logs.size()-10)+" 条");
                    }
                }
            } else if (event.getMessage().trim().toLowerCase().matches("^see\\s+boom\\s+log\\s+\\d{4}-\\d{2}-\\d{2}$")) {
                int ind = event.getMessage().trim().length();
                Player sender = event.getPlayer();
                String stlog = event.getMessage().trim().substring(ind-10, ind);
                List<String> logL = new ArrayList<>();
                for (String loz : logs) {
                    if (loz.startsWith(stlog)) {
                        logL.add(loz);
                    }
                }
                sender.sendMessage("§6后台为您扫描到 §c"+logL.size()+" §6个  矿车记录");
                if (!logL.isEmpty()) {
                    if (logL.size()<=10) {
                        for (String log : logL) {
                            sender.sendMessage("§6"+log);
                        }
                    } else {
                        for (String log : logL.subList(0, 10)) {
                            sender.sendMessage("§6"+log);
                        }
                        sender.sendMessage("§7......省略 "+(logL.size()-10)+" 条");
                    }
                }
            } else if (event.getMessage().trim().toLowerCase().matches("^see\\s+boom\\s+log\\s+\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}$")) {
                int ind = event.getMessage().trim().length();
                Player sender = event.getPlayer();
                String fn = event.getMessage().trim().substring(ind-19, ind);
                File conf = new File (Bukkit.getPluginManager().getPlugin("BoomMinecart").getDataFolder(), "/"+fn+".yml");
                if (conf.exists()) {
                    YamlConfiguration logc = new YamlConfiguration();
                    try {
                        logc.load(conf);
                    } catch (IOException | InvalidConfigurationException ex) {
                        Logger.getLogger(HMBOOM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String[] wt = fn.split("-");
                    List<String> wtl = Arrays.asList(wt);
                    sender.sendMessage(String.format(
                            "§6在 %1$s 年 %2$s 月 %3$s 日  %4$s 时 %5$s 分 %6$s 秒",
                            wtl.get(0),
                            wtl.get(1),
                            wtl.get(2),
                            wtl.get(3),
                            wtl.get(4),
                            wtl.get(5)
                    ));
                    String line = StringUtils.join(logc.getList("NearbyPlayer").toArray(), "， ");
                    sender.sendMessage(String.format(
                            "§6世界 §b%1$s§6， 坐标 §a%2$s§6， §a%3$s§6， §a%4$s §6处炸毁了一辆漏斗矿车",
                            logc.getString("Location.World"),
                            logc.getString("Location.X"),
                            logc.getString("Location.Y"),
                            logc.getString("Location.Z")
                    ));
                    sender.sendMessage("§6附近的玩家有：");
                    sender.sendMessage("§c"+line);
                } else {
                    sender.sendMessage("§4未查询到相关记录");
                }
            }
        }
    }
    
    @EventHandler (ignoreCancelled = true)
    public void SpawnLimit(CreatureSpawnEvent event) {
        if (BoomMinecart.config.getList("Server.DEADWAR.NMobLimit.world").contains(event.getEntity().getWorld().getName())) {
            if (event.getEntity().getLocation().getY() >= BoomMinecart.config.getDouble("Server.DEADWAR.NMobLimit.hight")){
                event.setCancelled(true);
            }
        }
    }
/*
    @EventHandler (ignoreCancelled = true)
    public void BoneBugFix(PlayerInteractEvent event) {
        if (BoomMinecart.bonebugfix) {
            if (event.getItem().getType() == Material.INK_SACK
                    && event.getClickedBlock().getType() == Material.SAPLING
                    && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.getClickedBlock().
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                event.getClickedBlock().getWorld().generateTree(event.getClickedBlock().getLocation(), tree);
            }
        }
    }
*/
    @EventHandler (ignoreCancelled = true)
    public void PlotAnimalsProtect(EntityDamageByEntityEvent event) {
        if (BoomMinecart.config.getBoolean("Server.DEADWAR.PlotMe.ProtectAnimals.enable")) {
            if (
                    (event.getEntity() instanceof Animals || event.getEntity() instanceof Golem)
                            && PlotManager.isPlotWorld(event.getEntity().getWorld())) {
                if (event.getDamager() instanceof Player) {
                    Player player = (Player) event.getDamager();
                    Plot plot = PlotManager.getPlotById(event.getEntity().getLocation());
                    if (plot != null) {
                        if (plot.getOwner() == player.getName()
                                || plot.isAllowed(player.getName())
                                || player.hasPermission("BoomMinecart.PlotProtection")) {
                            return;
                        }
                        event.setCancelled(true);
                    }
                } else if (event.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) event.getDamager();
                    if (arrow.getShooter() instanceof Player) {
                        Player player = (Player) arrow.getShooter();
                        Plot plot = PlotManager.getPlotById(event.getEntity().getLocation());
                        if (plot != null) {
                            if (plot.getOwner() == player.getName()
                                    || plot.isAllowed(player.getName())
                                    || player.hasPermission("BoomMinecart.PlotProtection")) {
                                return;
                            }
                            if (!BoomMinecart.config.getBoolean("Server.DEADWAR.PlotMe.ProtectAnimals.ProtectRoad"))
                                event.setCancelled(true);
                        }
                        if (BoomMinecart.config.getBoolean("Server.DEADWAR.PlotMe.ProtectAnimals.ProtectRoad"))
                            event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void SoilChangeEvent (PlayerInteractEvent event) {
        if (BoomMinecart.config.getBoolean("Server.DEADWAR.PlotMe.ProtectSoil")) {
            if (event.getClickedBlock().getType() == Material.SOIL
                    && event.getAction() == Action.PHYSICAL
                    && PlotManager.isPlotWorld(event.getPlayer().getWorld()))
                event.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void SoilChangeEvent (EntityInteractEvent event) {
        if (BoomMinecart.config.getBoolean("Server.DEADWAR.PlotMe.ProtectSoil")) {
            if (event.getBlock().getType() == Material.SOIL
                    && event.getEntityType() != EntityType.PLAYER
                    && PlotManager.isPlotWorld(event.getEntity().getWorld()))
                event.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void CommandLimit(PlayerCommandPreprocessEvent event) {
        /*
        if (cmd.toLowerCase().matches("^/op\\s*") 
                && sender.hasPermission("bukkit.command.op")) {
            if(cmd.toLowerCase().matches("^/op\\s*$") 
                    || cmd.toLowerCase().matches("^/op\\s+"+sender.getName()+"\\s*$")) {
                if (sender.hasPermission("boomminecart.opself")) {
                    return;
                }
            } else {
                if (sender.hasPermission("boomminecart.opothers")) {
                    return;
                }
            }
            event.setCancelled(true);
        } else if (cmd.toLowerCase().matches("^/man[gu]addp\\s+.+\\s+\\*.*")) {
            if (sender.hasPermission("boomminecart.pm")) {
                return;
            }
            event.setCancelled(true);
        } else
        */
        if (BoomMinecart.config.getBoolean("Server.DEADWAR.CommandFix")) {
            String cmd = event.getMessage();
            if (cmd.toLowerCase().startsWith("/p ")) {
                event.setCancelled(event.getPlayer().performCommand("plotme " + cmd.toLowerCase().substring(3, cmd.length())));
            }
        }
    }

    private void Boom(){
        if (ev.getDestination().getHolder() instanceof HopperMinecart) {
            ev.setCancelled(true);
            if (entity != (HopperMinecart) ev.getDestination().getHolder()) {
                entity = (HopperMinecart) ev.getDestination().getHolder();
                double x, y, z;
                x = entity.getLocation().getX();
                y = entity.getLocation().getY();
                z = entity.getLocation().getZ();
                World world = entity.getWorld();
                Double ar = BoomMinecart.config.getDouble("Nearby.around");
                List<Entity> entities = entity.getNearbyEntities(ar, ar, ar);
                entity.remove();
                int power = BoomMinecart.config.getInt("Boom.power");
                if (power >= 1)
                    world.createExplosion(x, y, z, power, false, BoomMinecart.config.getBoolean("Boom.break"));
                Bukkit.broadcast(
                        String.format(
                                "§a[爆破小分队]§6成功炸毁一台漏斗矿车于§d【%1$s， %2$d， %3$d， %4$d】",
                                world.getName(),
                                (int) x,
                                (int) y,
                                (int) z
                        ),
                        "BoomMinecart.broadcast"
                );
                File file = new File(Bukkit.getPluginManager().getPlugin("BoomMinecart").getDataFolder(), "/" + time.format(new Date()) + ".yml");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(HMBOOM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);
                } catch (InvalidConfigurationException | IOException ex) {
                    Logger.getLogger(HMBOOM.class.getName()).log(Level.SEVERE, null, ex);
                }
                config.set("Location.World", world.getName());
                config.set("Location.X", (int) x);
                config.set("Location.Y", (int) y);
                config.set("Location.Z", (int) z);
                List<String> players = new ArrayList<>();
                for (Entity Entity : entities) {
                    if (Entity instanceof Player) {
                        Player player = (Player) Entity;
                        players.add(player.getName());
                    }
                }
                config.set("NearbyPlayer", players.toArray());
                try {
                    config.save(file);
                } catch (IOException ex) {
                    Logger.getLogger(HMBOOM.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
