package com.javiluli.createpipeconnector.platform.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Centraliza la obtencion del jugador y el cambio al hilo principal del servidor.
 *
 * <p>Los manejadores de paquetes utilizan esta clase para compartir el mismo
 * tratamiento de contextos sin repetir comprobaciones ni marcar paquetes dos veces.</p>
 */
public final class ServerPayloadContext {
    /** Impide crear instancias del adaptador de contexto. */
    private ServerPayloadContext() {
    }

    /**
     * Ejecuta una accion con el jugador que envio el paquete, si sigue conectado.
     *
     * @param contextSupplier proveedor del contexto de red de Forge
     * @param action accion que debe ejecutarse en el hilo del servidor
     */
    public static void enqueue(Supplier<NetworkEvent.Context> contextSupplier, Consumer<ServerPlayer> action) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                action.accept(player);
            }
        });
        context.setPacketHandled(true);
    }

    /**
     * Ejecuta una accion que necesita tanto al jugador como a su nivel de servidor.
     *
     * @param contextSupplier proveedor del contexto de red de Forge
     * @param action accion segura para modificar el mundo
     */
    public static void enqueueWithLevel(
            Supplier<NetworkEvent.Context> contextSupplier,
            BiConsumer<ServerPlayer, ServerLevel> action
    ) {
        enqueue(contextSupplier, player -> {
            if (player.level() instanceof ServerLevel serverLevel) {
                action.accept(player, serverLevel);
            }
        });
    }
}
