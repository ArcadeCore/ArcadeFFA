package org.drappula.arcadeFfa;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.drappula.arcadeApi.ArcadeAPIProvider;
import org.drappula.arcadeApi.events.MatchEndEvent;
import org.drappula.arcadeApi.events.MatchStateChangeEvent;
import org.drappula.arcadeApi.events.ParticipantEliminateEvent;
import org.drappula.arcadeApi.systems.game.IMatch;
import org.drappula.arcadeApi.systems.game.IParticipant;
import org.drappula.arcadeApi.systems.game.ITeam;
import org.drappula.arcadeApi.systems.game.MatchState;
import org.drappula.arcadeApi.systems.map.IArcadeMap;

public class FFAListener implements Listener {
    private final FFAGame game;
    private final Map<IMatch, Map<UUID, Integer>> kills = new HashMap<>();
    private final Map<IMatch, BukkitTask> timers = new HashMap<>();
    private final Map<IMatch, double[]> borders = new HashMap<>();

    public FFAListener(FFAGame game) {
        this.game = game;
    }

    private boolean mine(IMatch match) {
        return match.getGame().getId().equals(game.getId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        IParticipant participant = ArcadeAPIProvider.get().getParticipant(game, event.getEntity());
        if (participant != null && !participant.isEliminated()) {
            participant.eliminate(event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onParticipantEliminate(ParticipantEliminateEvent event) {
        IParticipant victim = event.getParticipant();
        IMatch match = victim.getMatch();
        if (!mine(match)) return;

        recordStats(match, victim, event.getKiller());

        // Victim still counts as alive while this event fires; decide next tick.
        Bukkit.getScheduler().runTask(ArcadeFFA.get(), () -> checkWin(match));
    }

    private void recordStats(IMatch match, IParticipant victim, Player killer) {
        var stats = ArcadeAPIProvider.get().getStatsManager();
        try {
            Instant start = match.getStartedAt();
            if (start != null) {
                long secs = Duration.between(start, Instant.now()).toSeconds();
                stats.addStat(victim.getPlayer().getUniqueId(), victim.getPlayer().getName(),
                        game.getId(), "survival_seconds", (int) secs);
            }
            IParticipant killerPart = killer == null ? null : ArcadeAPIProvider.get().getParticipant(game, killer);
            if (killerPart != null && killerPart != victim && !killerPart.isEliminated()) {
                stats.addStat(killer.getUniqueId(), killer.getName(), game.getId(), "kills", 1);
                kills.computeIfAbsent(match, m -> new HashMap<>()).merge(killer.getUniqueId(), 1, Integer::sum);
                IArcadeMap map = match.getMap();
                if (map != null && map.getBooleanConfig(FFAGame.KILL_HEAL_KEY)) {
                    var max = killer.getAttribute(Attribute.MAX_HEALTH);
                    killer.setHealth(max == null ? 20.0 : max.getValue());
                }
            }
        } catch (SQLException e) {
            ArcadeFFA.get().getSLF4JLogger().error("Failed to record FFA stats", e);
        }
    }

    private void checkWin(IMatch match) {
        if (!match.isRunning()) return;
        if (game.teamSize() > 0 && match.getTeams() != null && !match.getTeams().isEmpty()) {
            List<ITeam> left = match.getTeams().stream().filter(t -> !t.isEliminated()).toList();
            if (left.size() == 1) {
                match.endWithWinners(new ArrayList<>(left.get(0).getAliveMembers()));
            } else if (left.isEmpty()) {
                match.end();
            }
            return;
        }
        if (match.getAliveCount() == 1) {
            match.endWithWinners(match.getAliveParticipants());
        } else if (match.getAliveCount() == 0) {
            match.end();
        }
    }

    @EventHandler
    public void onMatchStateChange(MatchStateChangeEvent event) {
        if (event.getNewState() != MatchState.STARTED) return;
        IMatch match = event.getMatch();
        if (!mine(match)) return;
        IArcadeMap map = match.getMap();
        if (map == null) return;

        int borderSize = map.getIntConfig(FFAGame.BORDER_SIZE_KEY);
        if (borderSize > 0 && map.getWorld() != null) {
            WorldBorder border = map.getWorld().getWorldBorder();
            borders.put(match, new double[]{border.getCenter().getX(), border.getCenter().getZ(), border.getSize()});
            var spawns = map.getSpawnPoints();
            if (!spawns.isEmpty()) {
                border.setCenter(spawns.stream().mapToDouble(l -> l.getX()).average().orElse(0),
                        spawns.stream().mapToDouble(l -> l.getZ()).average().orElse(0));
            }
            border.setSize(borderSize);
        }

        int limit = map.getIntConfig(FFAGame.TIME_LIMIT_KEY);
        if (limit > 0) {
            timers.put(match, Bukkit.getScheduler().runTaskLater(ArcadeFFA.get(), () -> timeUp(match), limit * 20L));
        }
    }

    /** Time limit: alive players with the most kills win (ties share the win). */
    private void timeUp(IMatch match) {
        if (!match.isRunning()) return;
        Map<UUID, Integer> k = kills.getOrDefault(match, Map.of());
        List<IParticipant> alive = match.getAliveParticipants();
        int best = alive.stream().mapToInt(p -> k.getOrDefault(p.getPlayer().getUniqueId(), 0)).max().orElse(0);
        match.endWithWinners(alive.stream()
                .filter(p -> k.getOrDefault(p.getPlayer().getUniqueId(), 0) == best).toList());
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        IMatch match = event.getMatch();
        if (!mine(match)) return;
        kills.remove(match);
        BukkitTask timer = timers.remove(match);
        if (timer != null) timer.cancel();
        double[] prev = borders.remove(match);
        if (prev != null && match.getMap() != null && match.getMap().getWorld() != null) {
            WorldBorder border = match.getMap().getWorld().getWorldBorder();
            border.setCenter(prev[0], prev[1]);
            border.setSize(prev[2]);
        }
    }

    /** Core declares friendly-fire but does not enforce it. */
    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Entity damager = event.getDamager();
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) damager = shooter;
        if (!(damager instanceof Player attacker)) return;
        IParticipant a = ArcadeAPIProvider.get().getParticipant(game, attacker);
        IParticipant v = ArcadeAPIProvider.get().getParticipant(game, victim);
        if (a == null || v == null || a.getTeam() == null || a.getTeam() != v.getTeam()) return;
        if (!game.getTeamSettings().isFriendlyFire()) event.setCancelled(true);
    }
}
