package dev.mitask.vibrational;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VibrationalPlugin implements VoicechatPlugin {
    public static VoicechatApi voicechatApi;
    private static ConcurrentHashMap<UUID, Long> cooldowns;

    @Nullable
    public static VoicechatServerApi voicechatServerApi;

    @Nullable
    private OpusDecoder decoder;

    @Override
    public String getPluginId() {
        return "vibrational";
    }

    @Override
    public void initialize(VoicechatApi api) {
        voicechatApi = api;
        cooldowns = new ConcurrentHashMap<>();
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        // Don't trigger any events when stopping to talk
        if (event.getPacket().getOpusEncodedData().length <= 0) return;
        if (!Vibrational.INSTANCE.getConfig().getBoolean("groupInteraction", false) && senderConnection.isInGroup()) return;
        if (!Vibrational.INSTANCE.getConfig().getBoolean("whisperInteraction", false) && event.getPacket().isWhispering()) return;

        if (!(senderConnection.getPlayer().getPlayer() instanceof Player player)) {
            Vibrational.INSTANCE.getLogger().warning("Received microphone packets from non-player");
            return;
        }

        if (!Vibrational.INSTANCE.getConfig().getBoolean("sneakInteraction", false) && player.isSneaking()) return;

        if (decoder == null) decoder = event.getVoicechat().createDecoder();
        decoder.resetState();
        short[] decoded = decoder.decode(event.getPacket().getOpusEncodedData());

        if (Utils.calculateAudioLevel(decoded) < Vibrational.INSTANCE.getConfig().getDouble("minActivationThreshold", -50)) return;

        if(Vibrational.INSTANCE.VOICE_GAME_EVENT == null) Vibrational.INSTANCE.resetEvent();

        if(activate(player)) {
            player.getScheduler().execute(
                    Vibrational.INSTANCE,
                    () -> player.getWorld().sendGameEvent(player, Vibrational.INSTANCE.VOICE_GAME_EVENT, player.getLocation().toVector()),
                    () -> System.out.println("test"),
                    0
            );
        }
    }

    private boolean activate(Player player) {
        Long lastTimestamp = cooldowns.get(player.getUniqueId());
        long currentTime = player.getWorld().getGameTime();
        if (lastTimestamp == null || currentTime - lastTimestamp > 20L) {
            cooldowns.put(player.getUniqueId(), currentTime);
            return true;
        }
        return false;
    }
}
