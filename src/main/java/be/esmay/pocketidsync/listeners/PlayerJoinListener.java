package be.esmay.pocketidsync.listeners;

import be.esmay.pocketidsync.PocketIDSyncPlugin;
import be.esmay.pocketidsync.api.models.CustomClaim;
import be.esmay.pocketidsync.utils.ChatUtils;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PlayerJoinListener {

    private final PocketIDSyncPlugin plugin;

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        
        this.plugin.getPocketID().users().getUserByUUID(player.getUniqueId()).thenAccept(user -> {
            if (user != null) {
                this.plugin.getUserIdCache().put(player.getUniqueId(), user.getId());

                List<CustomClaim> claims = user.modifyUserClaims(player.getUniqueId(), player.getUsername());
                user.setFirstName(player.getUsername());

                this.plugin.updateGroups(player.getUniqueId(), player.getUsername(), user.isAdmin(), user.getUserGroups()).thenAccept((bool) -> {
                    if (!bool) return; // The user has been deleted because none of the groups matched, no need to continue.

                    this.plugin.getPocketID().customClaims().updateUserClaims(user.getId(), claims);
                    this.plugin.getPocketID().users().updateUser(user.getId(), user);
                });

                return;
            }

            if (player.hasPermission("pocketidsync.register")) {
                player.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getRegisterAccountMessage()));
                return;
            }
        }).exceptionally(throwable -> {
            this.plugin.getLogger().error("PocketID sync on join failed for {}", player.getUsername(), throwable);
            return null;
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        this.plugin.getUserIdCache().remove(event.getPlayer().getUniqueId());
    }
}
