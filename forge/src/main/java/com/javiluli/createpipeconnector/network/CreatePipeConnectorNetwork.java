package com.javiluli.createpipeconnector.network;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.ServerPipeConnectorPayloadHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;

public final class CreatePipeConnectorNetwork {
    private static final String NETWORK_VERSION = "1";
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
                0,
                AddAnchorPayload.class,
                AddAnchorPayload::encode,
                AddAnchorPayload::decode,
                ServerPipeConnectorPayloadHandler::handleAddAnchor
        );
        CHANNEL.registerMessage(
                1,
                RemoveLastAnchorPayload.class,
                RemoveLastAnchorPayload::encode,
                RemoveLastAnchorPayload::decode,
                ServerPipeConnectorPayloadHandler::handleRemoveLastAnchor
        );
    }

    public static void sendToServer(AddAnchorPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }

    public static void sendToServer(RemoveLastAnchorPayload payload) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
    }
}
