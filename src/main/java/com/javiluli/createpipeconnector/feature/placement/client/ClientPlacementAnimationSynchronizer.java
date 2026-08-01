package com.javiluli.createpipeconnector.feature.placement.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.feature.placement.network.PlacementAnimationSettingsPayload;
import com.javiluli.createpipeconnector.platform.network.CreatePipeConnectorNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sincroniza la configuracion local al entrar y despues de guardarla. */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientPlacementAnimationSynchronizer {
    private static boolean loginSyncPending;

    /** Impide crear instancias del sincronizador de cliente. */
    private ClientPlacementAnimationSynchronizer() {
    }

    /** Envia las preferencias cuando el jugador termina de entrar al mundo. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        loginSyncPending = !syncIfConnected();
    }

    /** Cancela reintentos pendientes cuando la conexion termina. */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        loginSyncPending = false;
    }

    /** Reintenta la sincronizacion cuando Forge aun no habia publicado la conexion. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && loginSyncPending) {
            loginSyncPending = !syncIfConnected();
        }
    }

    /** Envia las preferencias actuales si existe una conexion jugable. */
    public static boolean syncIfConnected() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            CreatePipeConnectorNetwork.sendToServer(
                    new PlacementAnimationSettingsPayload(PlacementAnimationClientConfig.get())
            );
            return true;
        }
        return false;
    }
}
