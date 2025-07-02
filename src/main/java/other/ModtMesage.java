package other;

import adralik.srvBits.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Map;

import static adralik.srvBits.Main.config;

public class ModtMesage implements Listener {

    private static final String BASE_PATH = "MODT";
    private static final int JOIN_MESSAGE_DELAY = config.getInt(BASE_PATH + ".join-message-delay", 10);
    private static final List<String> CHAT_MESSAGE = config.getStringList(BASE_PATH + ".chat-message");
    private static final List<Map<?, ?>> LINK_MAP = config.getMapList(BASE_PATH + ".links");

    private static final String SOUND_TYPE = config.getString(BASE_PATH + ".sound.type", "null");
    private static final double SOUND_VOLUME = config.getDouble(BASE_PATH + ".sound.volume", 1);
    private static final double SOUND_PITCH = config.getDouble(BASE_PATH + ".sound.pitch", 1);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Main.javaPlugin, () -> {
            playSound(player);
            Component message = buildMessage();
            player.sendMessage(message);
        }, JOIN_MESSAGE_DELAY * 20L);
    }

    private void playSound(Player player) {
        try {
            player.playSound(player, Sound.valueOf(SOUND_TYPE), (float) SOUND_VOLUME, (float) SOUND_PITCH);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("invalid sound type: " + SOUND_TYPE);
        }
    }

    public Component buildMessage() {
        TextComponent.Builder messageBuilder = Component.text();
        boolean firstLine = true;

        for (String line : CHAT_MESSAGE) {
            if (!firstLine) {
                messageBuilder.append(Component.newline());
            }

            messageBuilder.append(parseLine(line));
            firstLine = false;
        }

        return messageBuilder.build();
    }

    private Component parseLine(String line) {
        TextComponent.Builder lineBuilder = Component.text();
        int lastIndex = 0;
        int openBraceIndex;

        while ((openBraceIndex = line.indexOf('{', lastIndex)) != -1) {
            int closeBraceIndex = line.indexOf('}', openBraceIndex + 1);

            if (closeBraceIndex == -1) {
                break;
            }
            if (openBraceIndex > lastIndex) {
                String textBefore = line.substring(lastIndex, openBraceIndex);
                lineBuilder.append(Component.text(textBefore));
            }

            String placeholderId = line.substring(openBraceIndex + 1, closeBraceIndex);
            Map<String, Object> matchedSection = findLinkSection(placeholderId);

            if (matchedSection != null) {
                String name = (String) matchedSection.get("name");
                String url = (String) matchedSection.get("url");
                String description = (String) matchedSection.get("description");

                lineBuilder.append(
                        Component.text(name)
                                .clickEvent(ClickEvent.openUrl(url))
                                .hoverEvent(HoverEvent.showText(
                                        Component.text(description)
                                ))
                );
            } else {
                lineBuilder.append(Component.text("{" + placeholderId + "}"));
            }

            lastIndex = closeBraceIndex + 1;
        }

        if (lastIndex < line.length()) {
            lineBuilder.append(Component.text(line.substring(lastIndex)));
        }

        return lineBuilder.build();
    }

    private Map<String, Object> findLinkSection(String id) {
        for (Map<?, ?> map : LINK_MAP) {
            String sectionId = (String) map.get("id");
            if (id.equals(sectionId)) {
                return (Map<String, Object>) map;
            }
        }
        return null;
    }
}
