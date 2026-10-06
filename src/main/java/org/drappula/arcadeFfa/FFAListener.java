package org.drappula.arcadeFfa;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.drappula.arcadeApi.ArcadeAPIProvider;
import org.drappula.arcadeApi.events.ParticipantEliminateEvent;
import org.drappula.arcadeApi.systems.game.IMatch;
import org.drappula.arcadeApi.systems.game.IParticipant;

public class FFAListener implements Listener {
    private final FFAGame game;

    public FFAListener(FFAGame game) {
        this.game = game;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        IParticipant participant = ArcadeAPIProvider.get().getParticipant(game, event.getEntity());
        if (participant != null && !participant.isEliminated()) {
            participant.eliminate();
        }
    }

    @EventHandler
    public void onParticipantEliminate(ParticipantEliminateEvent event) {
        IMatch match = event.getParticipant().getMatch();
        if (!match.getGame().getId().equals(game.getId())) return;
        if (match.getParticipants().size() != 2) return;

        Bukkit.getScheduler().runTask(ArcadeFFA.get(), match::end);
    }
}
