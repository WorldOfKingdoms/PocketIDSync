package be.esmay.pocketidsync.commands;

import be.esmay.pocketidsync.PocketIDSyncPlugin;
import be.esmay.pocketidsync.api.models.CustomClaim;
import be.esmay.pocketidsync.api.models.OneTimeAccessToken;
import be.esmay.pocketidsync.api.models.User;
import be.esmay.pocketidsync.utils.ChatUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class PocketIDCommand implements SimpleCommand {

    private final PocketIDSyncPlugin plugin;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            source.sendMessage(ChatUtils.format("This command can only be used by players."));
            return;
        }

        if (args.length != 1) {
            source.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandUsage()));
            return;
        }

        String email = args[0];
        if (!email.matches(EMAIL_REGEX)) {
            source.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandInvalidEmail()));
            return;
        }

        CompletableFuture.runAsync(() -> {
            User existingUser = this.plugin.getPocketID().users().getUserByUUID(player.getUniqueId()).join();
            if (existingUser != null) {
                player.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandAlreadyRegistered()));
                return;
            }

            existingUser = this.plugin.getPocketID().users().getUserByEmail(email).join();
            if (existingUser != null) {
                player.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandEmailAlreadyRegistered()));
                return;
            }

            User user = this.plugin.getPocketID().users().createUser(player, email).join();
            if (user == null) {
                player.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandFailedToRegister()));
                return;
            }

            this.plugin.getUserIdCache().put(player.getUniqueId(), user.getId());

            List<CustomClaim> claims = user.modifyUserClaims(player.getUniqueId(), player.getUsername());

            OneTimeAccessToken token = this.plugin.getPocketID().users().createOneTimeAccessToken(user.getId()).join();
            if (token == null) {
                player.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandFailedToGenerateToken()));
            } else {
                Component successMessage = ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandRegisteredSuccessfully());
                successMessage = successMessage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandClickToFinish())));
                successMessage = successMessage.clickEvent(ClickEvent.openUrl(this.plugin.getDefaultConfiguration().getBaseUrl() + "/lc/" + token.getToken()));
                player.sendMessage(successMessage);
            }

            boolean success = this.plugin.updateGroups(player.getUniqueId(), player.getUsername(), user.isAdmin(), user.getUserGroups()).join();
            if (!success)
                return; // The user has been deleted because none of the groups matched, no need to continue. (Server would be stupid to give permission to a user that has no groups, but just in case)

            this.plugin.getPocketID().customClaims().updateUserClaims(user.getId(), claims);
        }).exceptionally(throwable -> {
            this.plugin.getLogger().error("PocketID command failed for {}", player.getUsername(), throwable);
            player.sendMessage(ChatUtils.format(this.plugin.getDefaultConfiguration().getCommandFailedToRegister()));
            return null;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pocketidsync.register");
    }
}
