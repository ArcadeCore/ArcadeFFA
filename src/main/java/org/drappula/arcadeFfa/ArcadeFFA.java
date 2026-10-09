package org.drappula.arcadeFfa;

import org.bukkit.plugin.java.JavaPlugin;
import org.drappula.arcadeApi.ArcadeAPIProvider;

public final class ArcadeFFA extends JavaPlugin {
    private static ArcadeFFA instance;
    public static ArcadeFFA get() {
        return instance;
    }

    private final FFAGame game = new FFAGame();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        ArcadeAPIProvider.get().getGameManager().registerGame(game);
        getServer().getPluginManager().registerEvents(new FFAListener(game), this);
        FFACommand command = new FFACommand(game);
        getCommand("ffa").setExecutor(command);
        getCommand("ffa").setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        ArcadeAPIProvider.get().getGameManager().unregisterGame(game);
    }
}
