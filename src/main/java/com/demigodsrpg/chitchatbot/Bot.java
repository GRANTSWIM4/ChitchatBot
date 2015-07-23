package com.demigodsrpg.chitchatbot;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchatbot.ai.Brain;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Bot extends JavaPlugin {
    static final Brain BRAIN = new Brain();
    static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "F" + ChatColor.DARK_GRAY + "]" +
            ChatColor.GOLD + "[BOT]" + ChatColor.DARK_RED + "GustaBot" + ChatColor.GRAY + ": " + ChatColor.WHITE;

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        getLogger().info("Brain enabled, ready to chat.");
        getServer().getPluginManager().registerEvents(new BotListener(), this);
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> Chitchat.sendMessage(Bot.PREFIX +
                Bot.BRAIN.getSentence()), 20, 7000);
    }

    @Override
    public void onDisable() {
        BRAIN.purge();
        HandlerList.unregisterAll(this);
        getLogger().info("Brain purged, poor thing... :C");
    }
}
