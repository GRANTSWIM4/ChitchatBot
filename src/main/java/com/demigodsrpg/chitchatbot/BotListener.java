package com.demigodsrpg.chitchatbot;

import com.demigodsrpg.chitchat.Chitchat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Random;

public class BotListener implements Listener {
    private final Random RAND = new Random();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Bot.BRAIN.add(message);
        if(event.getMessage().toLowerCase().contains("gustabot")) {
            String[] parts = message.trim().split("\\s+");
            Chitchat.sendMessage(Bot.PREFIX + Bot.BRAIN.getSentence(parts[RAND.nextInt(parts.length - 1)]));
        }
    }
}
