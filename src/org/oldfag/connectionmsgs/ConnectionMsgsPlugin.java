package org.oldfag.connectionmsgs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ConnectionMsgsPlugin extends JavaPlugin implements Listener {
	private final File saveFile = new File("connectionmsgs.cookiedragon234");
	private final Set<UUID> off = new HashSet<>();
	
	public ConnectionMsgsPlugin() {
		super();
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		read();
		Runtime.getRuntime().addShutdownHook(new Thread(this::save));
	}
	
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		Bukkit.getCommandMap().register("toggleconnectionmsgs", new Command("toggleconnectionmsgs") {
			@Override
			public boolean execute(CommandSender sender, String command, String[] args) {
				if (sender instanceof Player) {
					UUID uuid = ((Player)sender).getUniqueId();
					if (off.remove(uuid)) {
						sender.sendMessage(ChatColor.GREEN + "Connection messages on");
					} else {
						off.add(uuid);
						sender.sendMessage(ChatColor.RED + "Connection messages off");
					}
					return true;
				}
				return false;
			}
		});
	}
	
	private void save() {
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(saveFile));
			for (UUID uuid : off) {
				writer.println(uuid.toString());
			}
			writer.close();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Failed to write file", t);
		}
	}
	
	public void read() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(saveFile));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) return;
				off.add(UUID.fromString(line));
			}
			reader.close();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Failed to write file", t);
		}
	}
	
	@EventHandler
	public void OnJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (event.getPlayer() instanceof CraftPlayer) {
			event.setJoinMessage(null);
			for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()){
				if(!off.contains(onlinePlayer.getUniqueId())){
					onlinePlayer.sendMessage(ChatColor.GRAY + player.getName() + " joined");
				}
			}
		}
	}
	
	ThreadLocal<Boolean> ignoreDc = ThreadLocal.withInitial(() -> Boolean.FALSE);
	
	@EventHandler
	public void onKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		String message;
		if (event.getReason().contains("timeout")) {
			message = ChatColor.GRAY + player.getName() + " lost connection";
		} else {
			message = ChatColor.GRAY + player.getName() + " has been kicked";
		}
		for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()){
			if (!off.contains(onlinePlayer.getUniqueId())){
				onlinePlayer.sendMessage(message);
			}
		}
		ignoreDc.set(Boolean.TRUE);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (ignoreDc.get()) {
			ignoreDc.set(Boolean.FALSE);
			event.setQuitMessage(null);
			return;
		}
		
		Player player = event.getPlayer();
		String message;
		StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
		if (stackTrace.length >= 9 && stackTrace[9].getClassName().equals("net.minecraft.server.v1_12_R1.ServerConnection")) {
			message = ChatColor.GRAY + player.getName() + " lost connection";
		} else {
			message = ChatColor.GRAY + player.getName() + " left";
		}
		for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()){
			if (!off.contains(onlinePlayer.getUniqueId())){
				onlinePlayer.sendMessage(message);
			}
		}
		event.setQuitMessage(null);
	}
}
