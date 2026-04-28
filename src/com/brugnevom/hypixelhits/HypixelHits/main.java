package com.brugnevom.hypixelhits.HypixelHits;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_8_R3.EntityPlayer;

public class main extends JavaPlugin {
	public static Map<UUID, Integer> swingCount = new HashMap<>();
	private final LocationHistoryManager historyManager = new LocationHistoryManager();
	public static Plugin thisplugin;
	
	public static int secondcount = 0;
	public static double prohibitedCPS = 20;
	public static boolean shouldCheckCPS = true, shouldThirdSprintHit;
	
	public static String hitdelaydesc = "hit delay (how much delay of hurt time before each hit): ";
	public static String damagedesc = "damage multiplier (damage dealt multiplies by this value everytime a player combos): ";
	public static String cpslimitingdesc = "CPS limiting (enable checking whether the comboer is clicking too much): ";
	public static String cpslimitdesc = "CPS limit (hypixel comobing won't work if the player is clicking beyond this value in a second): ";
	public static String thirdsprinthitdesc = "Third Sprint Hit (Enable sprint hit for the third combo hit): ";
	
	public static String folderPath = Paths.get("").toAbsolutePath().toString() + File.separator + "plugins" + File.separator + "HypixelHit" + File.separator;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new runTick(this), this);
		getCommand("reloadhit").setExecutor(new ExecuteHit());
		read();
		thisplugin = this;
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			
			@Override
			public void run() {
				if(runTick.damager != null && runTick.victim != null) {
					secondcount++;
					if(secondcount >= 5) {
						resetSwingCounts();
						secondcount = 0;
					}
					if(runTick.victim.isOnGround()) {
						runTick.groundy = runTick.victim.getLocation().getY();
						runTick.hitcount = 0;
					}
					
					if(!shouldThirdSprintHit) {
						if(runTick.victim != null && runTick.damager != null && runTick.nmsPlayer != null && runTick.nmsdPlayer != null) {
							if(runTick.victim.getLocation().getY() >= runTick.groundy + 0.2 || runTick.hitcount >= 2) {
								runTick.damager.setSprinting(false);
							} else runTick.damager.setSprinting(true);
							//runTick.damager.setSprinting(true);
						}
					}
				}
				
			}
			
		}, 0, 0);
		
		Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                historyManager.record(p.getUniqueId(), p.getLocation());
            }
        }, 0L, 1L);
		
	    ProtocolLibrary.getProtocolManager().addPacketListener(
	        new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION) {
	            @Override
	            public void onPacketReceiving(PacketEvent event) {
	                Player player = event.getPlayer();
	                if(runTick.damager != null && runTick.victim != null) {
		                if (player.isOnline() && player.isValid() && player == runTick.damager) {
			                swingCount.put(runTick.damager.getUniqueId(), swingCount.getOrDefault(runTick.damager.getUniqueId(), 0) + 1);
		
			                if(shouldCheckCPS) {
				                if (swingCount.get(runTick.damager.getUniqueId()) <= prohibitedCPS / 4) {
				                    runTick.shouldcustom = true;
				                } else if(runTick.hitcount >= 3) {
				                	runTick.shouldcustom = false;
				                }
			                } else runTick.shouldcustom = true;
		                }
	                }
	            }
	        });
	}
	public void resetSwingCounts() {
	    swingCount.clear();
	    runTick.shouldcustom = false;
	}
	
	public void enableDesyncForPair(Player attacker, Player victim) {
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new DesyncLogic(this, historyManager, attacker, victim)
        );
    }
	
	public LocationHistoryManager getHistoryManager() {
        return historyManager;
    }

	public static void read() {
		try {
			BufferedReader bfr = new BufferedReader(new FileReader(folderPath + "config.txt"));
			
			try {
				runTick.customhit = Boolean.parseBoolean(bfr.readLine().replace("enabled: ", ""));
				runTick.intmaxdmtick = Integer.parseInt(bfr.readLine().replace(hitdelaydesc, ""));
				runTick.damage = Double.parseDouble(bfr.readLine().replace(damagedesc, ""));
				shouldCheckCPS = Boolean.parseBoolean(bfr.readLine().replace(cpslimitingdesc, ""));
				prohibitedCPS = Double.parseDouble(bfr.readLine().replace(cpslimitdesc, ""));
				shouldThirdSprintHit = Boolean.parseBoolean(bfr.readLine().replace(thirdsprinthitdesc, ""));
				
				bfr.close();
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
			try {
				Files.createDirectories(Paths.get(folderPath));
				
				try {
					BufferedWriter bf = new BufferedWriter(new FileWriter(folderPath + "config.txt"));
					
					bf.write("enabled: " + true); bf.newLine();
					bf.write(hitdelaydesc + 17); bf.newLine();
					bf.write(damagedesc + 0.7); bf.newLine();
					bf.write(cpslimitingdesc + true); bf.newLine();
					bf.write(cpslimitdesc + 20); bf.newLine();
					bf.write(thirdsprinthitdesc + false); bf.newLine();
					
					bf.close();
				} catch (IOException e1) {
				}
			} catch (IOException e1) {
			}
		}
	}

}