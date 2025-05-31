package managers;

import adralik.srvBits.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinProtectionManager implements Listener {

    private final Map<UUID, BukkitTask> movementCheckTasks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 3, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 3, 128, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 1, false, false, false));

        player.setInvulnerable(true);

        Bukkit.getScheduler().runTaskLater(Main.javaPlugin, () -> {

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.javaPlugin, new Runnable() {
                private Location lastLocation = player.getLocation();

                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancelTask(player);
                        return;
                    }
                    if (!player.getLocation().getBlock().equals(lastLocation.getBlock())) {
                        player.setInvulnerable(false);
                        cancelTask(player);
                    }
                    lastLocation = player.getLocation();
                }
            }, 0L, 5L);

            movementCheckTasks.put(player.getUniqueId(), task);

            Bukkit.getScheduler().runTaskLater(Main.javaPlugin, () -> {
                if (player.isInvulnerable()) {
                    player.setInvulnerable(false);
                    cancelTask(player);
                }
            }, 20 * 15);

        }, 20 * 3);
    }

    private void cancelTask(Player player) {
        BukkitTask task = movementCheckTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
