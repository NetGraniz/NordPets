package com.nordfjell.nordpets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NordPetsPlugin extends JavaPlugin implements Listener {
    private final Map<UUID, Long> lastNotificationAt = new ConcurrentHashMap<>();
    private volatile Set<String> enabledWorlds = Set.of();
    private volatile boolean protectFromPlayerOwnedPets;
    private volatile boolean notifyAttacker;
    private volatile long notificationCooldownMillis;
    private volatile String notification;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NordPets enabled without packet or protocol dependencies.");
    }

    @Override
    public void onDisable() {
        lastNotificationAt.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPetDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Tameable pet) || !pet.isTamed()) {
            return;
        }
        if (!enabledWorlds.isEmpty()
                && !enabledWorlds.contains(event.getEntity().getWorld().getName().toLowerCase(Locale.ROOT))) {
            return;
        }

        UUID ownerId = pet.getOwnerUniqueId();
        ResponsibleAttacker responsible = findResponsibleAttacker(event.getDamager(), 0);
        if (responsible == null) {
            return;
        }
        if (responsible.fromOwnedPet() && !protectFromPlayerOwnedPets) {
            return;
        }
        if (ownerId != null && ownerId.equals(responsible.playerId())) {
            return;
        }

        Player attacker = getServer().getPlayer(responsible.playerId());
        if (attacker != null && attacker.hasPermission("nordpets.bypass")) {
            return;
        }

        event.setCancelled(true);
        if (attacker != null && notifyAttacker) {
            notifyAttacker(attacker);
        }
    }

    private @Nullable ResponsibleAttacker findResponsibleAttacker(Entity damager, int depth) {
        if (depth > 4) {
            return null;
        }
        if (damager instanceof Player player) {
            return new ResponsibleAttacker(player.getUniqueId(), false);
        }
        if (damager instanceof Tameable tameable && tameable.isTamed()) {
            UUID ownerId = tameable.getOwnerUniqueId();
            return ownerId == null ? null : new ResponsibleAttacker(ownerId, true);
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Entity entity) {
                return findResponsibleAttacker(entity, depth + 1);
            }
            return null;
        }
        if (damager instanceof TNTPrimed tnt && tnt.getSource() != null) {
            return findResponsibleAttacker(tnt.getSource(), depth + 1);
        }
        if (damager instanceof AreaEffectCloud cloud) {
            UUID ownerId = cloud.getOwnerUniqueId();
            if (ownerId != null) {
                return new ResponsibleAttacker(ownerId, false);
            }
            ProjectileSource source = cloud.getSource();
            if (source instanceof Entity entity) {
                return findResponsibleAttacker(entity, depth + 1);
            }
        }
        if (damager instanceof EvokerFangs fangs && fangs.getOwner() != null) {
            return findResponsibleAttacker(fangs.getOwner(), depth + 1);
        }
        return null;
    }

    private void notifyAttacker(Player player) {
        long now = System.currentTimeMillis();
        Long previous = lastNotificationAt.get(player.getUniqueId());
        if (previous != null && now - previous < notificationCooldownMillis) {
            return;
        }
        lastNotificationAt.put(player.getUniqueId(), now);
        player.sendActionBar(Component.text(notification, NamedTextColor.RED));
    }

    private void loadSettings() {
        reloadConfig();
        List<String> configuredWorlds = getConfig().getStringList("enabled-worlds");
        Set<String> normalizedWorlds = new HashSet<>();
        for (String world : configuredWorlds) {
            if (world != null && !world.isBlank()) {
                normalizedWorlds.add(world.toLowerCase(Locale.ROOT));
            }
        }
        enabledWorlds = Set.copyOf(normalizedWorlds);
        protectFromPlayerOwnedPets = getConfig().getBoolean("protect-from-player-owned-pets", true);
        notifyAttacker = getConfig().getBoolean("notify-attacker", true);
        notificationCooldownMillis = Math.max(0L,
                getConfig().getLong("notification-cooldown-millis", 1500L));
        notification = getConfig().getString("notification",
                "You cannot hurt another player's pet.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            loadSettings();
            sender.sendMessage(Component.text("NordPets configuration reloaded.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Usage: /nordpets reload", NamedTextColor.RED));
        }
        return true;
    }

    private record ResponsibleAttacker(UUID playerId, boolean fromOwnedPet) {
    }
}

