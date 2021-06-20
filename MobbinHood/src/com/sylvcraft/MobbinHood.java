package com.sylvcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sylvcraft.commands.mbh;
import com.sylvcraft.events.EntityShootBow;
import com.sylvcraft.events.PlayerInteract;
import com.sylvcraft.events.ProjectileHit;

public class MobbinHood extends JavaPlugin {
  NamespacedKey arrowType = new NamespacedKey(this, "mobArrow");
  Map<UUID, EntityType> currentType = new HashMap<>();
  Map<UUID, Boolean> shootProjectile = new HashMap<>();
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new EntityShootBow(this), this);
    pm.registerEvents(new PlayerInteract(this), this);
    pm.registerEvents(new ProjectileHit(this), this);
    getCommand("mbh").setExecutor(new mbh(this));
  }

  public Entity getMobArrow(Location loc, EntityType et) {
    Entity arrow = loc.getWorld().spawnEntity(loc.add(0,1,0), EntityType.ARROW);
    arrow.getPersistentDataContainer().set(arrowType, PersistentDataType.STRING, et.name());
    return arrow;
  }

  public EntityType getProjectileEntity(Projectile p) {
    if (!p.getPersistentDataContainer().has(arrowType, PersistentDataType.STRING)) return null;
    
    try {
      EntityType et = EntityType.valueOf(p.getPersistentDataContainer().get(arrowType, PersistentDataType.STRING));
      return et;
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
  
  public int getArrowSlot(Inventory inv, EntityType et) {
    for (int x=0; x<inv.getContents().length; x++) {
      ItemStack i = inv.getContents()[x];
      if (i == null || i.getType() != Material.ARROW) continue;
      
      EntityType invType = getArrowEntity(i);
      if (invType != et) continue;

      return x;
    }
    return -1;    
  }
  
  public boolean isShootingProjectile(UUID u) {
    return shootProjectile.containsKey(u)?shootProjectile.get(u):getConfig().getBoolean("players." + u.toString() + ".mode", true);
  }
  
  public void setShootingProjectile(UUID u, boolean val) {
    shootProjectile.put(u, val);
    getConfig().set("players." + u.toString() + ".mode", val);
    saveConfig();
  }
  
  public EntityType getCurrentType(UUID u) {
    return currentType.containsKey(u)?currentType.get(u):null;
  }
  
  public void setCurrentType(UUID u, EntityType et) {
    currentType.put(u, et);
  }
  
  public List<EntityType> getMobArrowTypes(Inventory inv) {
    List<EntityType> ret = new ArrayList<>();
    for (int x=0; x<inv.getContents().length; x++) {
      ItemStack i = inv.getContents()[x];
      if (i == null || i.getType() != Material.ARROW) continue;
      
      EntityType et = getArrowEntity(i);
      if (et == null) continue;
      
      ret.add(et);
    }
    return ret;
  }
  
  public EntityType getArrowEntity(ItemStack i) {
    if (!i.hasItemMeta()) return null;
    if (!i.getItemMeta().getPersistentDataContainer().has(arrowType, PersistentDataType.STRING)) return null;
    
    try {
      EntityType et = EntityType.valueOf(i.getItemMeta().getPersistentDataContainer().get(arrowType, PersistentDataType.STRING));
      return et;
    } catch (IllegalArgumentException ex) {
      ex.printStackTrace();
      return null;
    }
  }
  
  public Entity getArrowEntity(ItemStack i, EntityShootBowEvent e) {
    if (!i.hasItemMeta()) return null;
    if (!i.getItemMeta().getPersistentDataContainer().has(arrowType, PersistentDataType.STRING)) return null;
    
    try {
      EntityType et = EntityType.valueOf(i.getItemMeta().getPersistentDataContainer().get(arrowType, PersistentDataType.STRING));
      Entity mob = e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation().add(0, 1, 0), et);
      mob.setVelocity(e.getProjectile().getVelocity());
      return mob;
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
  
  public ItemStack getArrow(String entityType, int qty) {
    try {
      EntityType et = EntityType.valueOf(entityType.toUpperCase());
      ItemStack ret = new ItemStack(Material.ARROW, qty);
      ItemMeta im = ret.getItemMeta();
      im.setDisplayName(StringUtils.capitalize(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arrow-display", entityType + " Arrow"))));
      im.getPersistentDataContainer().set(arrowType, PersistentDataType.STRING, et.name());
      ret.setItemMeta(im);
      return ret;
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
  
  public void msg(String msgCode, CommandSender sender) {
  	if (getConfig().getString("messages." + msgCode) == null) return;
  	msgTransmit(getConfig().getString("messages." + msgCode), sender);
  }

  public void msg(String msgCode, CommandSender sender, Map<String, String> data) {
  	if (getConfig().getString("messages." + msgCode) == null) return;
  	String tmp = getConfig().getString("messages." + msgCode, msgCode);
  	for (Map.Entry<String, String> mapData : data.entrySet()) {
  	  tmp = tmp.replace(mapData.getKey(), mapData.getValue());
  	}
  	msgTransmit(tmp, sender);
  }
  
  public void msgTransmit(String msg, CommandSender sender) {
  	for (String m : (msg + " ").split("%br%")) {
  		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
  	}
  }

  public String pluralize(String message, int value) {
    String ret = message.replaceAll("!#", String.valueOf(value));
    ret = ret.replaceAll("!s", ((value == 1)?"":"s"));        // swords | swords
    ret = ret.replaceAll("!es", ((value == 1)?"":"es"));      // bus | buses
    ret = ret.replaceAll("!ies", ((value == 1)?"y":"ies"));   // penny | pennies
    ret = ret.replaceAll("!oo", ((value == 1)?"oo":"ee"));    // tooth | teeth
    ret = ret.replaceAll("!an", ((value == 1)?"an":"en"));    // woman | women
    ret = ret.replaceAll("!us", ((value == 1)?"us":"i"));     // cactus | cacti
    ret = ret.replaceAll("!is", ((value == 1)?"is":"es"));    // analysis | analyses
    ret = ret.replaceAll("!o", ((value == 1)?"o":"oes"));     // potato | potatoes
    ret = ret.replaceAll("!on", ((value == 1)?"a":"on"));     // criteria | criterion
    ret = ret.replaceAll("!lf", ((value == 1)?"lf":"lves"));  // elf | elves
    ret = ret.replaceAll("!ia", ((value == 1)?"is":"are"));
    ret = ret.replaceAll("!ww", ((value == 1)?"was":"were"));
    return ret;
  }
}