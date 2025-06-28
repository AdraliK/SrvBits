package managers;

import adralik.srvBits.Main;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

    private static final String SOUND_TYPE = config.getString(BASE_PATH + ".sound.type", "null");
    private static final double SOUND_VOLUME = config.getDouble(BASE_PATH + ".sound.volume", 1);
    private static final double SOUND_PITCH = config.getDouble(BASE_PATH + ".sound.pitch", 1);

    private final Map<UUID, Integer> playerAttempts = new HashMap<>();

    @EventHandler
    public void onBucketLavaUse(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (event.getBucket() == Material.LAVA_BUCKET &&
                shouldLimitPlayer(player, event.getBlock().getLocation())) {
            notifyIfAttemptsExceeded(player);
            sendMessage(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketLavaFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (event.getBucket() == Material.LAVA_BUCKET &&
                shouldLimitPlayer(player, event.getBlock().getLocation())) {
            notifyIfAttemptsExceeded(player);
            sendMessage(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onIllegalItemUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        if (illegalItems.contains(event.getItem().getType())) {
            if (shouldLimitPlayer(player, event.getClickedBlock().getLocation())) {
                notifyIfAttemptsExceeded(player);
                sendMessage(player);
                event.setCancelled(true);
            }
        }
    }

    private final List<Material> illegalItems = List.of(
            Material.FLINT_AND_STEEL,
            Material.FIRE_CHARGE,
            Material.END_CRYSTAL,
            Material.TNT,
            Material.DISPENSER,
            Material.DROPPER
            );

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        if (shouldLimitPlayer(attacker, attacker.getLocation()) ||
                shouldLimitPlayer(victim, attacker.getLocation())) {
            event.setCancelled(true);
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

        return Math.abs(loc.getX() - spawn.getX()) <= RADIUS &&
                Math.abs(loc.getY() - spawn.getY()) <= RADIUS &&
                Math.abs(loc.getZ() - spawn.getZ()) <= RADIUS;
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
            playSound(player);
            CHAT_MESSAGE.forEach(player::sendMessage);
        }
        if (playerAttemptCount >= ATTEMPT_COUNT_LIMIT) {
            playerAttempts.remove(playerId);
        }
    }

    private void playSound(Player player) {
        try {
            player.playSound(player, Sound.valueOf(SOUND_TYPE), (float) SOUND_VOLUME, (float) SOUND_PITCH);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("invalid sound type: " + SOUND_TYPE);
        }
    }
}

