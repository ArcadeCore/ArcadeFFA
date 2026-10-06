package org.drappula.arcadeFfa;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
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
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(FFACommand.get(game)));
    }

    @Override
    public void onDisable() {
        ArcadeAPIProvider.get().getGameManager().unregisterGame(game);
    }
}
