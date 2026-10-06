package org.drappula.arcadeFfa;

import org.drappula.arcadeApi.systems.game.Game;

public class FFAGame implements Game {
    @Override
    public String getId() {
        return "ffa";
    }

    @Override
    public String getDisplayName() {
        return "Free For All";
    }

    @Override
    public int getPlayersRequired() {
        return ArcadeFFA.get().getConfig().getInt("players-required");
    }
}
