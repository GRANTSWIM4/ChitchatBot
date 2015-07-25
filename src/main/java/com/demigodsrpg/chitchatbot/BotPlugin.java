package com.demigodsrpg.chitchatbot;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BotPlugin extends JavaPlugin {
    public static BotPlugin INST;
    private List<Bot> BOTS = new ArrayList<>();

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        INST = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

        BOTS.addAll(getConfig().getConfigurationSection("bots").getKeys(false).stream().
                map(botName -> {
                    String prefix = ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("bots." + botName + ".prefix", ""));
                    boolean talks = getConfig().getBoolean("bots." + botName + ".talks", false);
                    long freqTicks = getConfig().getLong("bots." + botName + ".freqTicks", 7000);
                    List<String> listensTo = getConfig().getStringList("bots." + botName + ".listensTo");
                    return new Bot(botName, prefix, talks, freqTicks, listensTo);
                }).collect(Collectors.toList()));

        int count = 0;
        for (Bot bot : BOTS) {
            getServer().getPluginManager().registerEvents(bot, this);
            if (bot.getTalks()) {
                count++;
                getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
                    String sentence = bot.getBrain().getSentence();
                    if (!"".equals(sentence)) {
                        Chitchat.sendMessage(bot.getPrefix() + sentence);
                    }
                }, count * 200, bot.getFreqTicks());
            }
        }

        getLogger().info("Brains enabled, ready to chat.");
    }

    @Override
    public void onDisable() {
        for (Bot bot : BOTS) {
            bot.saveToFile();
            bot.getBrain().purge();
        }
        HandlerList.unregisterAll(this);
        getLogger().info("Brains purged, poor things... :C");
    }
}
