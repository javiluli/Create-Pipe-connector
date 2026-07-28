package com.javiluli.createpipeconnector.network;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.network.payload.ServerPipeConnectorPayloadHandler;
import com.javiluli.createpipeconnector.network.payload.ToggleAutoPumpsPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.network.payload.WrenchPipeDisplayPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;

public final class CreatePipeConnectorNetwork {
    private static final String NETWORK_VERSION = "1";
    private static int messageId;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Objects.requireNonNull(ResourceLocation.tryParse(Constants.MOD_ID + ":main")),
            () -> NETWORK_VERSION,
            NETWORK_VERSION::equals,
            NETWORK_VERSION::equals
    );

    private CreatePipeConnectorNetwork() {
    }

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
                WrenchPipeDisplayPayload.class,
                WrenchPipeDisplayPayload::encode,
                WrenchPipeDisplayPayload::decode,
                ServerPipeConnectorPayloadHandler::handleWrenchPipeDisplay
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

    public static void sendToServer(SelectPipeTargetPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(CancelPipeConnectionPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(ToggleAutoPumpsPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(WrenchPipeDisplayPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    private static int nextMessageId() {
        return messageId++;
    }
}
