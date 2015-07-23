package com.demigodsrpg.chitchatbot;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchatbot.ai.Brain;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Bot extends JavaPlugin {
    static Bot INST;
    static final Brain BRAIN = new Brain();
    static final Brain SENPAI_BRAIN = new Brain();
    static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "F" + ChatColor.DARK_GRAY + "]" +
            ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "BOT" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + "GustaBot"
            + ChatColor.GRAY + ": " + ChatColor.WHITE;
    static final String SENPAI = ChatColor.GRAY + "«" + ChatColor.GOLD + "" + "bot" + ChatColor.GRAY + "»" +
            ChatColor.DARK_AQUA + "SenpaiBot" + ChatColor.GRAY + ": " + ChatColor.WHITE;

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        INST = this;
        BRAIN.add(COPYPASTA); // initial data
        SENPAI_BRAIN.add(SENPAI_PASTA);
        getServer().getPluginManager().registerEvents(new BotListener(), this);
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            String sentence = Bot.BRAIN.getSentence();
            if (!"".equals(sentence)) {
                Chitchat.sendMessage(Bot.PREFIX + Bot.BRAIN.getSentence());
            }
        }, 200, 7000);
        getLogger().info("Brain enabled, ready to chat.");
    }

    @Override
    public void onDisable() {
        BRAIN.purge();
        SENPAI_BRAIN.purge();
        HandlerList.unregisterAll(this);
        getLogger().info("Brain purged, poor thing... :C");
    }

    private static final String COPYPASTA = "What the fuck did you just fucking say about me, you little bitch?";
    private static final String SENPAI_PASTA = "Senpai won't notice you.";
}
