package com.javiluli.createpipeconnector.feature.placement.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.feature.placement.network.PlacementAnimationSettingsPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sincroniza la configuracion local al entrar y despues de modificarla. */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientPlacementAnimationSynchronizer {
    private static boolean loginSyncPending;
    private static PlacementAnimationSettings lastSyncedSettings;

    /** Impide crear instancias del sincronizador de cliente. */
    private ClientPlacementAnimationSynchronizer() {
    }

    /** Envia las preferencias cuando el jugador termina de entrar al mundo. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        lastSyncedSettings = null;
        loginSyncPending = !syncIfConnected();
    }

    /** Cancela reintentos pendientes cuando termina la conexion. */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        loginSyncPending = false;
        lastSyncedSettings = null;
        ClientPlacementLeadPreview.clear();
    }

    /** Reintenta la conexion y detecta cambios guardados por Catnip. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        PlacementAnimationSettings currentSettings = PlacementAnimationClientConfig.get();
        if (loginSyncPending || !currentSettings.equals(lastSyncedSettings)) {
            loginSyncPending = !syncIfConnected(currentSettings);
        }
    }

    /** Envia las preferencias actuales si existe una conexion jugable. */
    public static boolean syncIfConnected() {
        return syncIfConnected(PlacementAnimationClientConfig.get());
    }

    /** Envia una instantanea ya leida para evitar consultar dos veces la configuracion. */
    private static boolean syncIfConnected(PlacementAnimationSettings settings) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return false;
        }
        PacketDistributor.sendToServer(new PlacementAnimationSettingsPayload(settings));
        lastSyncedSettings = settings;
        if (!settings.enabled()) {
            ClientPlacementLeadPreview.clear();
        }
        return true;
    }
}
