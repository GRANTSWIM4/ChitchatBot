package com.demigodsrpg.chitchatbot;

import com.demigodsrpg.chitchat.Chitchat;
import com.demigodsrpg.chitchatbot.ai.Brain;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Bot implements Listener {
    protected static final Random RANDOM = new Random();

    private final List<String> listensTo;
    private final Brain brain = new Brain();
    private final String name, prefix;
    private final boolean talks;
    private final long freqTicks;
    private final Cache<String, Integer> replyCache = CacheBuilder.newBuilder().
            expireAfterWrite(6, TimeUnit.SECONDS).
            build();

    public Bot(String name, String prefix, boolean talks, long freqTicks, String... listensTo) {
        this(name, prefix, talks, freqTicks, Arrays.asList(listensTo));
    }

    public Bot(String name, String prefix, boolean talks, long freqTicks, List<String> listensTo) {
        this.name = name;
        this.prefix = prefix;
        this.talks = talks;
        this.freqTicks = freqTicks;
        this.listensTo = listensTo;
    }

    public Brain getBrain() {
        return brain;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean getTalks() {
        return talks;
    }

    public long getFreqTicks() {
        return freqTicks;
    }

    public int getSpamAmount(String replyTo) {
        int amount = replyCache.asMap().getOrDefault(replyTo, 0);
        replyCache.put(replyTo, amount + 1);
        return amount;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().contains("@" + getName().toLowerCase())) {
            int spamAmount = getSpamAmount(event.getPlayer().getName());
            if (spamAmount < 1) {
                Bukkit.getScheduler().scheduleAsyncDelayedTask(BotPlugin.INST, () -> {
                    String[] parts = message.toLowerCase().trim().split("\\s+");
                    String sentence = getBrain().getSentence(parts[RANDOM.nextInt(parts.length)]);
                    if (!"".equals(sentence)) {
                        Chitchat.sendMessage(getPrefix() + sentence);
                    } else {
                        Chitchat.sendMessage(getPrefix() + "beep. boop. beep.");
                    }
                }, 10 * (1 + RANDOM.nextInt(2)));
            } else {
                // Let them know the bot doesn't like spam
                event.setCancelled(true);
                event.getPlayer().sendMessage(event.getFormat());
                if (spamAmount % 5 == 0) {
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(BotPlugin.INST, () -> {
                        event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "PM from" + " <" + ChatColor.DARK_AQUA +
                                getName() + ChatColor.DARK_GRAY + ">: " + ChatColor.GRAY + "Please don't spam me. :C");
                    }, 10);
                }
            }
        } else if (listensTo.isEmpty()) {
            getBrain().add(message);
        } else if (listensTo.contains(event.getPlayer().getName())) {
            getBrain().add(message);
        }
    }
}
