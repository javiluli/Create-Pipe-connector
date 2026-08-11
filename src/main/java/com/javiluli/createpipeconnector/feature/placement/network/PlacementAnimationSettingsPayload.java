package com.javiluli.createpipeconnector.feature.placement.network;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import net.minecraft.network.FriendlyByteBuf;

/** Envia al servidor la preferencia local de construccion progresiva. */
public record PlacementAnimationSettingsPayload(boolean enabled, int delayMilliseconds) {
    /** Crea el payload desde una configuracion ya saneada. */
    public PlacementAnimationSettingsPayload(PlacementAnimationSettings settings) {
        this(settings.enabled(), settings.delayMilliseconds());
    }

    /** Decodifica el estado y la velocidad solicitados. */
    public static PlacementAnimationSettingsPayload decode(FriendlyByteBuf buffer) {
        return new PlacementAnimationSettingsPayload(buffer.readBoolean(), buffer.readVarInt());
    }

    /** Codifica el estado y la velocidad solicitados. */
    public static void encode(PlacementAnimationSettingsPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.enabled());
        buffer.writeVarInt(payload.delayMilliseconds());
    }
}
