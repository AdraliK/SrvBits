package managers;

import adralik.srvBits.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static adralik.srvBits.Main.config;

public class SpawnProtection implements Listener {

    private static final String BASE_PATH = "spawn-protection";
    private static final int RADIUS = config.getInt(BASE_PATH + ".spawn-radius", 0);
    private static final int ADVANCEMENT_COUNT = config.getInt(BASE_PATH + ".advancement-count-limit", 0);
    private static final String RAW_ACTION_BAR_MESSAGE = config.getString(BASE_PATH + ".action-bar-message", "message");
    private static final int ATTEMPT_COUNT_LIMIT = config.getInt(BASE_PATH + ".attempt-count", 0);
    private static final List<String> CHAT_MESSAGE = config.getStringList(BASE_PATH + ".chat-message");

    private final Map<UUID, Integer> playerAttempts = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (shouldLimitPlayer(player, event.getBlock().getLocation())) {
            notifyIfAttemptsExceeded(player);
            sendMessage(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (shouldLimitPlayer(player, event.getBlock().getLocation())) {
            notifyIfAttemptsExceeded(player);
            sendMessage(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (shouldLimitPlayer(player, event.getBlock().getLocation())) {
            notifyIfAttemptsExceeded(player);
            sendMessage(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (shouldLimitPlayer(player, event.getBlock().getLocation())) {
            notifyIfAttemptsExceeded(player);
            sendMessage(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFlintUse(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.FLINT_AND_STEEL) {
            if (shouldLimitPlayer(player, event.getPlayer().getLocation())) {
                notifyIfAttemptsExceeded(player);
                sendMessage(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerAttempts.remove(event.getPlayer().getUniqueId());
    }

    private boolean shouldLimitPlayer(Player player, Location loc) {
        return isInProtectedZone(loc) && getAdvancementsCount(player) < ADVANCEMENT_COUNT;
    }

    private boolean isInProtectedZone(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;
        if (world.getEnvironment() != World.Environment.NORMAL) return false;

        Location spawn = world.getSpawnLocation();
        return loc.distanceSquared(spawn) <= RADIUS * RADIUS;
    }

    private int getAdvancementsCount(Player player) {
        int count = 0;
        Iterator<Advancement> iterator = Bukkit.advancementIterator();

        while (iterator.hasNext()) {
            Advancement advancement = iterator.next();
            if (advancement.getDisplay() == null) continue;

            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                count++;
            }
        }

        return count;
    }

    private void sendMessage(Player player) {
        int messageTime = 5;
        int advancementsCountLeft = ADVANCEMENT_COUNT - getAdvancementsCount(player);
        String message = RAW_ACTION_BAR_MESSAGE.replace("{advLastCount}", String.valueOf(advancementsCountLeft));

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= messageTime || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                player.sendActionBar(message);
                count++;
            }
        }.runTaskTimer(Main.javaPlugin, 0L, 20L);
    }

    private void notifyIfAttemptsExceeded(Player player) {
        UUID playerId = player.getUniqueId();
        int playerAttemptCount = playerAttempts.merge(playerId, 1, Integer::sum);

        if (playerAttemptCount == 1) {
            //сюда добавить звук
            CHAT_MESSAGE.forEach(player::sendMessage);
        }
        if (playerAttemptCount >= ATTEMPT_COUNT_LIMIT) {
            playerAttempts.remove(playerId);
        }
    }
}

