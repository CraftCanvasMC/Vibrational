package dev.mitask.vibrational;

import io.papermc.paper.datapack.DatapackRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.GameEventKeys;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.LogManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@SuppressWarnings("all")
public class VibrationalBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load("plugins/Vibrational/config.yml");
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.getLogger().info("Failed to load config.yml at bootstrap, using default range 16");
            return;
        }
        int range = config.getInt("range", 16);

        final LifecycleEventManager<BootstrapContext> manager = context.getLifecycleManager();

        manager.registerEventHandler(RegistryEvents.GAME_EVENT.compose().newHandler(event -> {
            event.registry().register(
                    GameEventKeys.create(Key.key("vibrational:voice")),
                    builder -> builder.range(range)
            );
        }));

        manager.registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
            DatapackRegistrar registrar = event.registrar();
            try {
                final URI uri = Objects.requireNonNull(
                        VibrationalBootstrap.class.getResource("/vibrational")
                ).toURI();
                registrar.discoverPack(uri, "events");
            } catch (final URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
