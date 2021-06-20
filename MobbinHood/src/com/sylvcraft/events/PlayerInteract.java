package com.sylvcraft.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.sylvcraft.MobbinHood;

public class PlayerInteract implements Listener {
  MobbinHood plugin;
  Map<UUID, Integer> currentSelection = new HashMap<>();
  
  public PlayerInteract(MobbinHood plugin) {
    this.plugin = plugin;
  }
  
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
    if (e.getHand() != EquipmentSlot.HAND) return;

    ItemStack i = e.getPlayer().getInventory().getItemInMainHand(); 
    if (i.getType() != Material.BOW) return;

    UUID u = e.getPlayer().getUniqueId();    
    List<EntityType> ets = plugin.getMobArrowTypes(e.getPlayer().getInventory());
    if (ets.size() == 0) return;
    
    Map<String, String> data = new HashMap<String, String>();
    int current = currentSelection.containsKey(u)?currentSelection.get(u)+1:0;
    if (current >= ets.size()) {
      current = -1;
      data.put("%type%", "normal");
      plugin.setCurrentType(u, null);
    } else {
      data.put("%type%", ets.get(current).name());
      plugin.setCurrentType(u, ets.get(current));
    }
    
    plugin.msg("arrow-type-set", e.getPlayer(), data);
    currentSelection.put(u, current);
  }
}
