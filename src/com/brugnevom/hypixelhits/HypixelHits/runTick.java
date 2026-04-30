package com.brugnevom.hypixelhits.HypixelHits;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.EnderChest;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;

import net.md_5.bungee.api.ChatColor;

public class runTick implements Listener {
	public static boolean customhit = true, shouldcustom, consistantkb;
	public static int intmaxdmtick;
	public static double damage, groundy;
	public static int hitcount;
	
	public static Player victim;
	public static Player damager;
	public static net.minecraft.server.v1_8_R3.EntityPlayer nmsPlayer;
	public static net.minecraft.server.v1_8_R3.EntityPlayer nmsdPlayer;
	
	Map<UUID, Integer> hitCount = new HashMap<>();
	
	public static int hitcombo;
	public main m;
	
	public runTick(main m) {
		this.m = m;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHit(EntityDamageByEntityEvent event) {
	    if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
	        victim = (Player) event.getEntity();
	        damager = (Player) event.getDamager();
	        nmsPlayer = ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) damager).getHandle();
	        nmsdPlayer = ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) victim).getHandle();
	        
	        if(customhit) {
	        	if(victim.isOnGround()) hitcount = 0;
		        else hitcount++;
		        if(hitcount >= 4) hitcount = 0;
		        
	        	if(shouldcustom) {
			        event.setDamage(event.getDamage() * damage);
			        victim.setMaximumNoDamageTicks(intmaxdmtick);
			        
			        if(consistantkb) {
				        if(hitcount >= 1 && !victim.isOnGround()) {
					        if(damager.getLocation().distance(victim.getLocation()) > 2.5) {
						        if(nmsdPlayer.hurtTicks > 0) {
							        Vector kb = new Vector(0, 0, 0);
						        	if(hitcount == 1) kb.setY(-0.3);
						        	if(hitcount == 2) kb.setY(-0.7);
							        victim.setVelocity(kb);
						        }
					        }
				        }
			        }
	        	} else {
	        		victim.setMaximumNoDamageTicks(20);
	        		event.setDamage(event.getDamage());
	        	}
	        } else {
        		victim.setMaximumNoDamageTicks(20);
        		event.setDamage(event.getDamage());
        	}
	    }
	}

}