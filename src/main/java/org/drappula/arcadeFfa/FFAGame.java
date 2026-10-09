package org.drappula.arcadeFfa;

import java.util.Arrays;
import java.util.List;
import org.drappula.arcadeApi.systems.game.Game;
import org.drappula.arcadeApi.systems.game.settings.TeamSettings;
import org.drappula.arcadeApi.systems.map.MapConfigOption;

public class FFAGame implements Game {
    public static final String BORDER_SIZE_KEY = "border-size";
    public static final String KILL_HEAL_KEY = "kill-heal";
    public static final String TIME_LIMIT_KEY = "time-limit-seconds";

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

    @Override
    public int getMaxPlayers() {
        int max = ArcadeFFA.get().getConfig().getInt("max-players");
        return Math.max(max, getMinPlayers());
    }

    public int teamSize() {
        return Math.max(0, ArcadeFFA.get().getConfig().getInt("team-size"));
    }

    @Override
    public TeamSettings getTeamSettings() {
        int size = teamSize();
        if (size == 0) return TeamSettings.DISABLED;
        int teams = Math.max(2, getMaxPlayers() / size);
        return new TeamSettings(size, teams, ArcadeFFA.get().getConfig().getBoolean("friendly-fire"));
    }

    @Override
    public List<MapConfigOption> getMapConfigOptions() {
        return Arrays.asList(
                MapConfigOption.integer(BORDER_SIZE_KEY, 0, 0, 60_000_000),
                MapConfigOption.booleanOption(KILL_HEAL_KEY, false),
                MapConfigOption.integer(TIME_LIMIT_KEY, 0, 0, 3600));
    }
}
