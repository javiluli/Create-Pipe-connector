package com.javiluli.createpipeconnector.feature.material.shulker.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.material.shulker.config.ShulkerMaterialClientConfig;
import com.javiluli.createpipeconnector.feature.material.shulker.network.ShulkerMaterialSettingsPayload;
import com.javiluli.createpipeconnector.platform.network.CreatePipeConnectorNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sincroniza al servidor la preferencia local de acceso a shulkers. */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientShulkerMaterialSynchronizer {
    private static Boolean lastSyncedValue;
    private static boolean loginSyncPending;

    /** Impide crear instancias del sincronizador. */
    private ClientShulkerMaterialSynchronizer() {
    }

    /** Prepara el primer envio cuando comienza una sesion de juego. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        lastSyncedValue = null;
        loginSyncPending = !syncIfConnected(ShulkerMaterialClientConfig.isEnabled());
    }

    /** Descarta el estado sincronizado al salir del mundo. */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        lastSyncedValue = null;
        loginSyncPending = false;
    }

    /** Detecta cambios guardados desde la pantalla de configuracion de Create. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        boolean enabled = ShulkerMaterialClientConfig.isEnabled();
        if (loginSyncPending || lastSyncedValue == null || lastSyncedValue != enabled) {
            loginSyncPending = !syncIfConnected(enabled);
        }
    }

    /** Envia la preferencia cuando existe una conexion jugable. */
    private static boolean syncIfConnected(boolean enabled) {
        if (Minecraft.getInstance().getConnection() == null) {
            return false;
        }
        CreatePipeConnectorNetwork.sendToServer(new ShulkerMaterialSettingsPayload(enabled));
        lastSyncedValue = enabled;
        return true;
    }
}
