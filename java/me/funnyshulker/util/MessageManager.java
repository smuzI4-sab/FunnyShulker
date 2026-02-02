package me.funnyshulker.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final Map<String, String> messages;

    public MessageManager(FileConfiguration config) {
        this.messages = new HashMap<>();
        loadMessages(config);
    }

    private void loadMessages(FileConfiguration config) {
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                String message = config.getString("messages." + key, "");
                if (!message.isEmpty()) {
                    messages.put(key, message);
                }
            }
        }
        loadDefaultMessages();
    }

    private void loadDefaultMessages() {
        Map<String, String> defaultMessages = new HashMap<>();
        defaultMessages.put("shulker-open-error", "&cНе удалось открыть шалкер-бокс!");
        defaultMessages.put("shulker-blacklisted", "&cЭтот предмет нельзя положить в шалкер-бокс!");
        defaultMessages.put("shulker-self-remove", "&cНельзя вытащить сам шалкер-бокс!");
        defaultMessages.put("shulker-move-blocked", "&cНельзя перемещать открытый шалкер-бокс!");
        defaultMessages.put("shulker-drop-blocked", "&cНельзя выбросить открытый шалкер-бокс!");
        defaultMessages.put("shulker-swap-blocked", "&cНельзя переложить открытый шалкер-бокс!");
        defaultMessages.put("shulker-no-space", "&cВ шалкер-боксе нет свободного места!");
        defaultMessages.put("shulker-saved", "&aСодержимое шалкер-бокса сохранено");
        defaultMessages.put("auto-collect-full", "&eШалкер переполнен! Не могу собрать предметы");

        for (Map.Entry<String, String> entry : defaultMessages.entrySet()) {
            if (!messages.containsKey(entry.getKey())) {
                messages.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getMessage(String key) {
        String message = messages.getOrDefault(key, "&cСообщение не найдено: " + key);
        return message.replace('&', '§');
    }
}