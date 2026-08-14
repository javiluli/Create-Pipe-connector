package com.javiluli.createpipeconnector.feature.material.shulker.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.material.shulker.config.ShulkerMaterialClientConfig;
import com.javiluli.createpipeconnector.feature.material.shulker.network.ShulkerMaterialSettingsPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sincroniza al servidor la preferencia local de acceso a shulkers. */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
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
    public static void onClientTick(ClientTickEvent.Post event) {
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
        PacketDistributor.sendToServer(new ShulkerMaterialSettingsPayload(enabled));
        lastSyncedValue = enabled;
        return true;
    }
}
