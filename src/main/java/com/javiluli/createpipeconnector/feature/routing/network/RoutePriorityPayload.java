package com.javiluli.createpipeconnector.feature.routing.network;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza la prioridad usada por el buscador de rutas.
 */
public record RoutePriorityPayload(RoutePriority priority) implements CustomPacketPayload {
    public static final Type<RoutePriorityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "route_priority"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RoutePriorityPayload> STREAM_CODEC = StreamCodec.ofMember(RoutePriorityPayload::write, RoutePriorityPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static RoutePriorityPayload read(RegistryFriendlyByteBuf buffer) {
        return new RoutePriorityPayload(buffer.readEnum(RoutePriority.class));
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(priority);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

