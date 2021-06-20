package com.sylvcraft.events;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import com.sylvcraft.MobbinHood;

public class EntityShootBow implements Listener {
  MobbinHood plugin;
  
  public EntityShootBow(MobbinHood instance) {
    plugin = instance;
  }

  @EventHandler
  public void onEntityShootBow(EntityShootBowEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    
    Player p = (Player)e.getEntity();
    int slot = p.getInventory().first(Material.ARROW);
    if (slot == -1) return;

    List<EntityType> types = plugin.getMobArrowTypes(p.getInventory());
    if (!types.contains(plugin.getCurrentType(p.getUniqueId()))) return;
    
    int arrowSlot = plugin.getArrowSlot(p.getInventory(), plugin.getCurrentType(p.getUniqueId()));
    if (arrowSlot == -1) return;
    
    e.setConsumeItem(false);
    ItemStack i = p.getInventory().getItem(arrowSlot);
    i.setAmount(i.getAmount()-1);
    p.getInventory().setItem(arrowSlot, i);

    if (plugin.isShootingProjectile(p.getUniqueId())) {
      Entity mob = plugin.getArrowEntity(p.getInventory().getItem(arrowSlot), e);
      if (mob == null) return;

      e.setProjectile(mob);
      return;
    }
    
    Entity arrow = plugin.getMobArrow(p.getLocation(), plugin.getCurrentType(p.getUniqueId()));
    arrow.setVelocity(e.getProjectile().getVelocity());
    e.setProjectile(arrow);
  }
}