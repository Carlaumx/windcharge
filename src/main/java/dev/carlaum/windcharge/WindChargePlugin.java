package dev.carlaum.windcharge;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WindChargePlugin extends JavaPlugin implements Listener {

    private double explosionPower;
    private boolean showParticles;
    private Particle particle;
    private double projectileSpeed;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        explosionPower = getConfig().getDouble("explosionPower");
        showParticles = getConfig().getBoolean("showParticles");
        projectileSpeed = getConfig().getDouble("projectileSpeed");

        try {
            particle = Particle.valueOf(getConfig().getString("particle"));
        } catch (IllegalArgumentException e) {
            particle = Particle.FLAME;
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.getInventory().getItemInMainHand().getType() == Material.WIND_CHARGE) {
                event.setCancelled(true);
                triggerWindCharge(player);
            }
        }
    }

    private void triggerWindCharge(Player player) {
        Vector direction = player.getLocation().getDirection().normalize().multiply(projectileSpeed);
        Entity projectile = player.launchProjectile(WindCharge.class, direction);

        projectile.setVelocity(direction);

        if (showParticles) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (projectile.isDead() || !projectile.isValid()) {
                        this.cancel();
                        return;
                    }

                    Location loc = projectile.getLocation();
                    projectile.getWorld().spawnParticle(particle, loc, 2, 0.1, 0.1, 0.1, 0.05);
                }
            }.runTaskTimer(this, 0L, 1L);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof WindCharge) {
            if (showParticles) projectile.getWorld().spawnParticle(particle, projectile.getLocation(), 500);

            projectile.getWorld().createExplosion(projectile.getLocation(), (float) explosionPower);
            projectile.remove();
        }
    }


}