package black.nigger.jl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class Main extends JavaPlugin implements Listener {

    private HashSet<Player> on = new HashSet<Player>();

    @Override
    public void onEnable() {
        for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()){
            on.add(onlinePlayer);
        }
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("toggleconnectionmsgs")) {
            Player player = (Player)sender;
            if(on.contains(player)) {
                on.remove(player);
                player.sendMessage(ChatColor.GOLD + "Connection messages hidden.");
                return true;
            } else {
                on.add(player);
                player.sendMessage(ChatColor.GOLD + "Connection messages unhidden.");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        on.add(player);
        event.setJoinMessage(null);
        for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()){
            if(on.contains(onlinePlayer)){
                onlinePlayer.sendMessage(ChatColor.GRAY + player.getName() + " joined");
            }
        }
    }

    @EventHandler
    public void OnQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        on.remove(player);
        event.setQuitMessage(null);
        for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()){
            if(on.contains(onlinePlayer)){
                onlinePlayer.sendMessage(ChatColor.GRAY + player.getName() + " left");
            }
        }
    }

}