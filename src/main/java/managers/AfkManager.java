package managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static adralik.srvBits.Main.config;

public class AfkManager extends PlaceholderExpansion implements Listener {

    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private static final long AFK_TIME_MINUTES = config.getInt("afk-time", 1);
    private final long afkTimeout = AFK_TIME_MINUTES * 60 * 1000;

    public AfkManager() {
        this.register();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            updateActivity(player);
        }
    }

    private void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isAfk(Player player) {
        return System.currentTimeMillis() - lastActivity.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) > afkTimeout;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastActivity.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public String getIdentifier() {
        return "afkstatus";
    }

    @Override
    public String getAuthor() {
        return "SrvBits";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";
        return isAfk(player) ? "ກ" : "noAFK";
    }
}

