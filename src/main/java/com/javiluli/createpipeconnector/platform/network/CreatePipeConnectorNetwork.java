package com.javiluli.createpipeconnector.platform.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Expone el canal de Forge sin depender de ninguna feature concreta.
 *
 * <p>El bootstrap registra los payloads y las features solo utilizan
 * {@link #sendToServer(Object)} para enviar sus acciones.</p>
 */
public final class CreatePipeConnectorNetwork {
    private static final String PROTOCOL_VERSION = "2";
    private static final String CHANNEL_PATH = "main";
    private static int messageId;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, CHANNEL_PATH),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /** Impide crear instancias del canal global. */
    private CreatePipeConnectorNetwork() {
    }

    /**
     * Registra un payload manteniendo una numeracion estable y centralizada.
     *
     * @param payloadType tipo del mensaje
     * @param encoder serializador del mensaje
     * @param decoder deserializador del mensaje
     * @param handler manejador ejecutado al recibirlo
     * @param <T> tipo concreto del payload
     */
    public static <T> void registerMessage(
            Class<T> payloadType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler
    ) {
        CHANNEL.registerMessage(messageId++, payloadType, encoder, decoder, handler);
    }

    /** Envia cualquier payload registrado desde el cliente al servidor. */
    public static void sendToServer(Object payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }
}
