package com.glisco.deathlog.network;

import com.glisco.deathlog.DeathLogCommon;
import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.client.gui.DeathLogScreen;
import com.glisco.deathlog.server.DeathLogServer;
import com.glisco.deathlog.storage.BaseDeathLogStorage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class DeathLogPackets {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(RequestDeletion.ID, PacketCodec.ofStatic(RequestDeletion::write, RequestDeletion::read));
        PayloadTypeRegistry.playC2S().register(RequestRestore.ID, PacketCodec.ofStatic(RequestRestore::write, RequestRestore::read));
        PayloadTypeRegistry.playC2S().register(InfoRequest.ID, PacketCodec.ofStatic(InfoRequest::write, InfoRequest::read));
        PayloadTypeRegistry.playS2C().register(OpenScreen.ID, PacketCodec.ofStatic(OpenScreen::write, OpenScreen::read));
        PayloadTypeRegistry.playS2C().register(DeathInfoData.ID, PacketCodec.ofStatic(DeathInfoData::write, DeathInfoData::read));

        ServerPlayNetworking.registerGlobalReceiver(RequestDeletion.ID, Server::handleDelete);
        ServerPlayNetworking.registerGlobalReceiver(RequestRestore.ID, Server::handleRestore);
        ServerPlayNetworking.registerGlobalReceiver(InfoRequest.ID, Server::sendInfo);
    }

    public static class Client {

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(OpenScreen.ID, Client::handleOpenScreen);
            ClientPlayNetworking.registerGlobalReceiver(DeathInfoData.ID, Client::receiveInfo);
        }

        private static void receiveInfo(DeathInfoData payload, ClientPlayNetworking.Context context) {
            var buffer = payload.data();
            var index = buffer.readVarInt();
            var info = DeathInfo.read(buffer, context.client().world.getRegistryManager());
            var client = context.client();

            client.execute(() -> {
                if (!(client.currentScreen instanceof DeathLogScreen screen)) {
                    BaseDeathLogStorage.LOGGER.warn("Received invalid death info packet");
                    return;
                }

                screen.updateInfo(info, index);
            });
        }

        private static void handleOpenScreen(OpenScreen payload, ClientPlayNetworking.Context context) {
            var buffer = payload.data();
            var storage = RemoteDeathLogStorage.read(buffer);
            var canRestore = buffer.readBoolean();
            context.client().execute(() -> DeathLogClient.openScreen(storage, canRestore));
        }

        public static void requestDeletion(UUID profile, int index) {
            ClientPlayNetworking.send(new RequestDeletion(profile, index));
        }

        public static void requestRestore(UUID profile, int index) {
            ClientPlayNetworking.send(new RequestRestore(profile, index));
        }

        public static void fetchInfo(UUID profile, int index) {
            ClientPlayNetworking.send(new InfoRequest(profile, index));
        }
    }

    public static class Server {

        public static void registerDedicatedListeners() {
            // Handlers are registered in DeathLogPackets.init()
        }

        public static void registerCommonListeners() {
            init();
        }

        private static void sendInfo(InfoRequest payload, ServerPlayNetworking.Context context) {
            var profileId = payload.profile();
            var index = payload.index();
            var server = context.player().getEntityWorld().getServer();

            server.execute(() -> {
                if (!DeathLogServer.hasPermission(context.player(), "deathlog.view")) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized info request from {}", context.player().getName().getString());
                    return;
                }

                var info = DeathLogCommon.getStorage().getDeathInfoList(profileId).get(index);
                var buffer = PacketByteBufs.create();
                buffer.writeVarInt(index);
                info.write(buffer, server.getRegistryManager());
                ServerPlayNetworking.send(context.player(), new DeathInfoData(buffer));
            });
        }

        private static void handleRestore(RequestRestore payload, ServerPlayNetworking.Context context) {
            var profileId = payload.profile();
            var index = payload.index();
            var server = context.player().getEntityWorld().getServer();

            server.execute(() -> {
                if (!DeathLogServer.hasPermission(context.player(), "deathlog.restore")) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized restore packet from {}", context.player().getName().getString());
                    return;
                }

                var targetPlayer = server.getPlayerManager().getPlayer(profileId);
                if (targetPlayer == null) {
                    BaseDeathLogStorage.LOGGER.warn("Received restore packet for invalid player");
                    return;
                }

                final var infoList = DeathLogCommon.getStorage().getDeathInfoList(profileId);
                if (index > infoList.size() - 1) {
                    BaseDeathLogStorage.LOGGER.warn("Received restore packet with invalid index from '{}'", context.player().getName().getString());
                    return;
                }

                infoList.get(index).restore(targetPlayer);
            });
        }

        private static void handleDelete(RequestDeletion payload, ServerPlayNetworking.Context context) {
            var profileId = payload.profile();
            var index = payload.index();
            var server = context.player().getEntityWorld().getServer();

            server.execute(() -> {
                if (!DeathLogServer.hasPermission(context.player(), "deathlog.delete")) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized delete packet from {}", context.player().getName().getString());
                    return;
                }

                DeathLogServer.getStorage().delete(DeathLogServer.getStorage().getDeathInfoList(profileId).get(index), profileId);
            });
        }

        public static void openScreen(UUID profileId, ServerPlayerEntity target) {
            var buffer = PacketByteBufs.create();
            var infos = DeathLogServer.getStorage().getDeathInfoList(profileId);

            buffer.writeCollection(infos, (packetByteBuf, info) -> info.writePartial(packetByteBuf));
            buffer.writeUuid(profileId);
            buffer.writeBoolean(target.getEntityWorld().getServer().getPlayerManager().getPlayer(profileId) != null);

            ServerPlayNetworking.send(target, new OpenScreen(buffer));
        }
    }

    public record RequestDeletion(UUID profile, int index) implements CustomPayload {
        public static final CustomPayload.Id<RequestDeletion> ID = new CustomPayload.Id<>(Identifier.of("deathlog", "request_deletion"));

        public static void write(PacketByteBuf buf, RequestDeletion value) {
            value.write(buf);
        }

        public static RequestDeletion read(PacketByteBuf buf) {
            return new RequestDeletion(buf.readUuid(), buf.readVarInt());
        }

        public void write(PacketByteBuf buf) {
            buf.writeUuid(profile);
            buf.writeVarInt(index);
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RequestRestore(UUID profile, int index) implements CustomPayload {
        public static final CustomPayload.Id<RequestRestore> ID = new CustomPayload.Id<>(Identifier.of("deathlog", "request_restore"));

        public static void write(PacketByteBuf buf, RequestRestore value) {
            value.write(buf);
        }

        public static RequestRestore read(PacketByteBuf buf) {
            return new RequestRestore(buf.readUuid(), buf.readVarInt());
        }

        public void write(PacketByteBuf buf) {
            buf.writeUuid(profile);
            buf.writeVarInt(index);
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record InfoRequest(UUID profile, int index) implements CustomPayload {
        public static final CustomPayload.Id<InfoRequest> ID = new CustomPayload.Id<>(Identifier.of("deathlog", "fetch_info"));

        public static void write(PacketByteBuf buf, InfoRequest value) {
            value.write(buf);
        }

        public static InfoRequest read(PacketByteBuf buf) {
            return new InfoRequest(buf.readUuid(), buf.readVarInt());
        }

        public void write(PacketByteBuf buf) {
            buf.writeUuid(profile);
            buf.writeVarInt(index);
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record OpenScreen(PacketByteBuf data) implements CustomPayload {
        public static final CustomPayload.Id<OpenScreen> ID = new CustomPayload.Id<>(Identifier.of("deathlog", "open_screen"));

        public static void write(PacketByteBuf buf, OpenScreen value) {
            value.write(buf);
        }

        public static OpenScreen read(PacketByteBuf buf) {
            return new OpenScreen(new PacketByteBuf(buf.readBytes(buf.readableBytes())));
        }

        public void write(PacketByteBuf buf) {
            buf.writeBytes(data);
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record DeathInfoData(PacketByteBuf data) implements CustomPayload {
        public static final CustomPayload.Id<DeathInfoData> ID = new CustomPayload.Id<>(Identifier.of("deathlog", "send_info"));

        public static void write(PacketByteBuf buf, DeathInfoData value) {
            value.write(buf);
        }

        public static DeathInfoData read(PacketByteBuf buf) {
            return new DeathInfoData(new PacketByteBuf(buf.readBytes(buf.readableBytes())));
        }

        public void write(PacketByteBuf buf) {
            buf.writeBytes(data);
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
