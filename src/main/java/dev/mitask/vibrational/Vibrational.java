package dev.mitask.vibrational;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public final class Vibrational extends JavaPlugin {
    public org.bukkit.GameEvent VOICE_GAME_EVENT;
    public static Vibrational INSTANCE;

    @Nullable
    private VibrationalPlugin vibrationalPlugin;

    @Override
    public void onEnable() {
        getLogger().info("Loading Vibrational");

        saveDefaultConfig();

        ResourceKey<GameEvent> event = ResourceKey.create(BuiltInRegistries.GAME_EVENT.key(), ResourceLocation.parse("vibrational:voice"));
        Reference2IntOpenHashMap<ResourceKey<GameEvent>> map = (Reference2IntOpenHashMap<ResourceKey<GameEvent>>) VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT;
        map.put(event, getConfig().getInt("voiceSculkFrequency", 15));

        INSTANCE = this;
        resetEvent();

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            vibrationalPlugin = new VibrationalPlugin();
            service.registerPlugin(vibrationalPlugin);
            getLogger().info("Successfully registered vibrational plugin");
        } else {
            getLogger().info("Failed to register vibrational plugin");
        }
    }

    public void resetEvent() {
        VOICE_GAME_EVENT = Registry.GAME_EVENT.get(Key.key("vibrational:voice"));
    }

    @Override
    public void onDisable() {
        if (vibrationalPlugin != null) {
            getServer().getServicesManager().unregister(vibrationalPlugin);
            getLogger().info("Successfully unregistered vibrational plugin");
        }
    }
}
