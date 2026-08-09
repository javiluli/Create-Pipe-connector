package com.javiluli.createpipeconnector.feature.placement.network;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Envia al servidor la preferencia local de construccion progresiva. */
public record PlacementAnimationSettingsPayload(boolean enabled, int piecesPerSecond)
        implements CustomPacketPayload {
    public static final Type<PlacementAnimationSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "placement_animation_settings")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PlacementAnimationSettingsPayload> STREAM_CODEC =
            StreamCodec.ofMember(PlacementAnimationSettingsPayload::write, PlacementAnimationSettingsPayload::read);

    /** Crea el payload desde una configuracion ya saneada. */
    public PlacementAnimationSettingsPayload(PlacementAnimationSettings settings) {
        this(settings.enabled(), settings.piecesPerSecond());
    }

    /** Decodifica el estado y la velocidad solicitados. */
    private static PlacementAnimationSettingsPayload read(RegistryFriendlyByteBuf buffer) {
        return new PlacementAnimationSettingsPayload(buffer.readBoolean(), buffer.readVarInt());
    }

    /** Codifica el estado y la velocidad solicitados. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
        buffer.writeVarInt(piecesPerSecond);
    }

    /** {@inheritDoc} */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
