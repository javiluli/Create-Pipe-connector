package com.javiluli.createpipeconnector.network;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.network.payload.CopperCasingModePayload;
import com.javiluli.createpipeconnector.network.payload.PipeStyleModePayload;
import com.javiluli.createpipeconnector.network.payload.PreviewSnapshotPayload;
import com.javiluli.createpipeconnector.network.payload.PumpModePayload;
import com.javiluli.createpipeconnector.network.payload.RemotePreviewPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.network.payload.ReverseAutoPumpDirectionPayload;
import com.javiluli.createpipeconnector.network.payload.RoutePriorityPayload;
import com.javiluli.createpipeconnector.network.payload.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.network.payload.ServerPipeConnectorPayloadHandler;
import com.javiluli.createpipeconnector.network.payload.ToggleAutoPumpsPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.network.payload.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.network.payload.WrenchPipeDisplayPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Registers the Forge network channel and all connector payload directions.
 */
public final class CreatePipeConnectorNetwork {
    private static int messageId;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, Constants.CHANNEL_PATH),
            () -> Constants.PROTOCOL_VERSION,
            Constants.PROTOCOL_VERSION::equals,
            Constants.PROTOCOL_VERSION::equals
    );

    private CreatePipeConnectorNetwork() {
    }

    /**
     * Registers all payload codecs and server handlers in stable order.
     */
    public static void register() {
        CHANNEL.registerMessage(
                nextMessageId(),
                AddAnchorPayload.class,
                AddAnchorPayload::encode,
                AddAnchorPayload::decode,
                ServerPipeConnectorPayloadHandler::handleAddAnchor
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                RemoveLastAnchorPayload.class,
                RemoveLastAnchorPayload::encode,
                RemoveLastAnchorPayload::decode,
                ServerPipeConnectorPayloadHandler::handleRemoveLastAnchor
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                ToggleConnectorModePayload.class,
                ToggleConnectorModePayload::encode,
                ToggleConnectorModePayload::decode,
                ServerPipeConnectorPayloadHandler::handleToggleConnectorMode
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                PumpModePayload.class,
                PumpModePayload::encode,
                PumpModePayload::decode,
                ServerPipeConnectorPayloadHandler::handlePumpMode
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                CopperCasingModePayload.class,
                CopperCasingModePayload::encode,
                CopperCasingModePayload::decode,
                ServerPipeConnectorPayloadHandler::handleCopperCasingMode
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                PipeStyleModePayload.class,
                PipeStyleModePayload::encode,
                PipeStyleModePayload::decode,
                ServerPipeConnectorPayloadHandler::handlePipeStyleMode
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                ReverseAutoPumpDirectionPayload.class,
                ReverseAutoPumpDirectionPayload::encode,
                ReverseAutoPumpDirectionPayload::decode,
                ServerPipeConnectorPayloadHandler::handleReverseAutoPumpDirection
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                RoutePriorityPayload.class,
                RoutePriorityPayload::encode,
                RoutePriorityPayload::decode,
                ServerPipeConnectorPayloadHandler::handleRoutePriority
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                SelectPipeTargetPayload.class,
                SelectPipeTargetPayload::encode,
                SelectPipeTargetPayload::decode,
                ServerPipeConnectorPayloadHandler::handleSelectPipeTarget
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                CancelPipeConnectionPayload.class,
                CancelPipeConnectionPayload::encode,
                CancelPipeConnectionPayload::decode,
                ServerPipeConnectorPayloadHandler::handleCancelPipeConnection
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                ToggleAutoPumpsPayload.class,
                ToggleAutoPumpsPayload::encode,
                ToggleAutoPumpsPayload::decode,
                ServerPipeConnectorPayloadHandler::handleToggleAutoPumps
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                RemoveLastManualPumpPayload.class,
                RemoveLastManualPumpPayload::encode,
                RemoveLastManualPumpPayload::decode,
                ServerPipeConnectorPayloadHandler::handleRemoveLastManualPump
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                RemoveLastCopperCasingPayload.class,
                RemoveLastCopperCasingPayload::encode,
                RemoveLastCopperCasingPayload::decode,
                ServerPipeConnectorPayloadHandler::handleRemoveLastCopperCasing
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                ToggleCopperCasingPayload.class,
                ToggleCopperCasingPayload::encode,
                ToggleCopperCasingPayload::decode,
                ServerPipeConnectorPayloadHandler::handleToggleCopperCasing
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                ToggleManualPumpPayload.class,
                ToggleManualPumpPayload::encode,
                ToggleManualPumpPayload::decode,
                ServerPipeConnectorPayloadHandler::handleToggleManualPump
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                WrenchPipeDisplayPayload.class,
                WrenchPipeDisplayPayload::encode,
                WrenchPipeDisplayPayload::decode,
                ServerPipeConnectorPayloadHandler::handleWrenchPipeDisplay
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                PreviewSnapshotPayload.class,
                PreviewSnapshotPayload::encode,
                PreviewSnapshotPayload::decode,
                ServerPipeConnectorPayloadHandler::handlePreviewSnapshot,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextMessageId(),
                RemotePreviewPayload.class,
                RemotePreviewPayload::encode,
                RemotePreviewPayload::decode,
                RemotePreviewPayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendToServer(AddAnchorPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(RemoveLastAnchorPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(ToggleConnectorModePayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(PumpModePayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(CopperCasingModePayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(PipeStyleModePayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(ReverseAutoPumpDirectionPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(RoutePriorityPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(SelectPipeTargetPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(CancelPipeConnectionPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(ToggleAutoPumpsPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(RemoveLastManualPumpPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(RemoveLastCopperCasingPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(ToggleCopperCasingPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(ToggleManualPumpPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(WrenchPipeDisplayPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(PreviewSnapshotPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToPlayer(ServerPlayer player, RemotePreviewPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    private static int nextMessageId() {
        return messageId++;
    }
}
