package com.sylvcraft.events;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.sylvcraft.MobbinHood;

public class ProjectileHit implements Listener {
  MobbinHood plugin;
  
  public ProjectileHit(MobbinHood instance) {
    plugin = instance;
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent e) {
    EntityType et = plugin.getProjectileEntity(e.getEntity());
    if (et == null) return;
    
    Location spawnLoc = e.getHitBlock().getRelative(e.getHitBlockFace()).getLocation().add(0.5,0,0.5);
    e.getHitBlock().getWorld().spawnEntity(spawnLoc, et);
  }

}
