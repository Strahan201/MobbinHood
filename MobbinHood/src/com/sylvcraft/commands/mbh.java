package com.sylvcraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sylvcraft.MobbinHood;

public class mbh implements TabExecutor {
  MobbinHood plugin;
  
  public mbh(MobbinHood instance) {
    plugin = instance;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    switch (args.length) {
    case 0:
      return null;
      
    case 1:
      return Arrays.asList("reload","give","mode");
      
    case 2:
      switch (args[0].toLowerCase()) {
      case "give":
        return getEntityTypes(args[1].toUpperCase());
      }
      return null;
      
    default:
      return null;
    }
  }
  
  private List<String> getEntityTypes(String typed) {
    List<String> ret = new ArrayList<>();
    for (EntityType et : EntityType.values()) {
      if (typed.trim() == "" || (et.name().length() >= typed.length() && et.name().subSequence(0, typed.length()).equals(typed))) ret.add(et.name());
    }
    return ret;
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      if (!(sender instanceof Player)) {
        plugin.msg("player-only", null);
        return true;
      }

      if (args.length == 0) {
        showHelp(sender);
        return true;
      }

      Player p = (Player)sender;
      Map<String, String> data = new HashMap<String, String>();
      switch (args[0].toLowerCase()) {
      case "mode":
        plugin.setShootingProjectile(p.getUniqueId(), !plugin.isShootingProjectile(p.getUniqueId()));
        data.put("%mode%", plugin.isShootingProjectile(p.getUniqueId())?plugin.getConfig().getString("messages.mode-arrow-is-entity", "arrow is entity"):plugin.getConfig().getString("messages.mode-arrow-spawns-entity", "arrow spawns entity"));
        plugin.msg("arrow-mode", sender, data);
        break;
        
      case "reload":
        if (!(sender.hasPermission("mobbinhood.reload"))) {
          plugin.msg("access-denied", sender);
          return true;
        }
        
        plugin.reloadConfig();
        plugin.msg("cfg-reload", sender);
        break;
        
      case "give":
        if (args.length == 1) {
          showHelp(sender);
          return true;
        }
        
        try {
          data.put("%entity%", args[1]);
          int qty = args.length > 2?Integer.valueOf(args[2]):1;
          data.put("%qty%", String.valueOf(qty));
          ItemStack i = plugin.getArrow(args[1], qty);
          if (i == null) {
            plugin.msg("invalid-entity", sender, data);
            return true;
          }
          
          p.getInventory().addItem(i);
          plugin.msg("give-arrow", sender, data);
        } catch (NumberFormatException ex) {
          plugin.msg("invalid-qty", sender);
        }
        break;
      }

      return true;
    } catch (Exception ex) {
      return false;
    }
  }

	void showHelp(CommandSender sender) {
    int displayed = 0;
		if (sender.hasPermission("MobbinHood.xxxx")) { plugin.msg("xxxx", sender); displayed++; }
		if (displayed == 0) plugin.msg("access-denied", sender);
  }
}
