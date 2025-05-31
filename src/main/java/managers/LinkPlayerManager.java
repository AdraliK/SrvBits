package managers;

import adralik.srvBits.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

import static adralik.srvBits.Main.config;

public class LinkPlayerManager implements Listener {

    private static final int DARKNESS_EFFECT_TIME = 20 * 60 * 5;
    private static final String UN_AUTH_MESSAGE_NORMAL = config.getString("unauthorized-message.normal", "your text 1");
    private static final String UN_AUTH_MESSAGE_NETHER = config.getString("unauthorized-message.nether", "your text 2");
    private static final String UN_AUTH_MESSAGE_END = config.getString("unauthorized-message.end", "your text 3");
    private static final String PERMISSION = config.getString("authorized-permission", "permission");

    private final HashMap<UUID, BukkitTask> endTasks = new HashMap<>();
    private final HashMap<UUID, BukkitTask> messageTasks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION)) return;

        World.Environment dimension = player.getWorld().getEnvironment();
        applyDimensionEffect(dimension, player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeTasks(player);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION)) return;

        World.Environment dimension = player.getWorld().getEnvironment();
        removeTasks(player);
        applyDimensionEffect(dimension, player);
    }

    private void applyDimensionEffect(World.Environment dimension, Player player) {
        switch (dimension) {
            case NETHER -> {
                sendMessage(player, UN_AUTH_MESSAGE_NETHER, 10);
                player.setFireTicks(Integer.MAX_VALUE);
            }
            case THE_END -> {
                sendMessage(player, UN_AUTH_MESSAGE_END, 10);
                applyTheEndEffect(player);
            }
            default -> {
                sendMessage(player, UN_AUTH_MESSAGE_NORMAL, 15);
                removeTasks(player);
                removeEffects(player);
            }
        }
    }

    private void sendMessage(Player player, String message, int seconds) {
        UUID uuid = player.getUniqueId();

        if (messageTasks.containsKey(uuid)) {
            messageTasks.remove(uuid).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= seconds || !player.isOnline()) {
                    this.cancel();
                    messageTasks.remove(uuid).cancel();
                    return;
                }
                player.sendActionBar(message);
                count++;
            }
        }.runTaskTimer(Main.javaPlugin, 0L, 20L);

        messageTasks.put(uuid, task);
    }


    private void applyTheEndEffect(Player player) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.javaPlugin, () -> {
            if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, DARKNESS_EFFECT_TIME, 0));
            }
        }, 0, DARKNESS_EFFECT_TIME);

        endTasks.put(player.getUniqueId(), task);
    }

    private void removeEffects(Player player) {
        player.setFireTicks(0);
        player.removePotionEffect(PotionEffectType.DARKNESS);
    }

    private void removeTasks(Player player) {
        UUID playerId = player.getUniqueId();
        if (endTasks.containsKey(playerId)) {
            endTasks.remove(playerId).cancel();
        }
    }
}
