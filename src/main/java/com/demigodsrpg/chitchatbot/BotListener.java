package com.demigodsrpg.chitchatbot;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BotListener implements Listener {
    private final List<String> HQM = Arrays.asList("HQM", "HmmmQuestionMark", "HandyQuestMarker");
    private final Random RAND = new Random();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().contains("@gustabot")) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(Bot.INST, () -> {
                String[] parts = message.trim().split("\\s+");
                Chitchat.sendMessage(Bot.PREFIX + Bot.BRAIN.getSentence(parts[RAND.nextInt(parts.length)]));
            }, 30);
            return;
        } else if (message.toLowerCase().contains("@senpaibot")) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(Bot.INST, () -> {
                String[] parts = message.toLowerCase().trim().split("\\s+");
                Chitchat.sendMessage(Bot.SENPAI + Bot.SENPAI_BRAIN.getSentence(parts[RAND.nextInt(parts.length)]));
            }, 30);
            return;
        }
        if (HQM.contains(event.getPlayer().getName())) {
            Bot.SENPAI_BRAIN.add(message.toLowerCase());
        }
        Bot.BRAIN.add(message);
    }
}
