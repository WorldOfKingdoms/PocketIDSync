package be.esmay.pocketidsync.channel;

import be.esmay.pocketidsync.PocketIDSyncPlugin;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class LuckpermsChannel extends JedisPubSub {

    private final PocketIDSyncPlugin plugin;

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals("luckperms:update")) return;

        JSONObject object = new JSONObject(message);
        String type = object.getString("type");

        if (!type.equals("userupdate")) return;

        JSONObject content = object.getJSONObject("content");
        String targetUniqueId = content.getString("userUuid");

        handle(UUID.fromString(targetUniqueId));
    }

    private void handle(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            be.esmay.pocketidsync.api.models.User user = this.plugin.getPocketID().users().getUserByUUID(uuid).join();
            if (user != null) {
                this.plugin.getUserIdCache().put(uuid, user.getId());

                this.plugin.updateGroups(uuid, uuid.toString().split("-")[0], user.getUserGroups()).join();
            }
        });
    }
}