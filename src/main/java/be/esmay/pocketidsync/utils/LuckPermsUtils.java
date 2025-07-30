package be.esmay.pocketidsync.utils;

import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public final class LuckPermsUtils {
    private static final LuckPerms luckPerms = LuckPermsProvider.get();

    @Nullable
    public User getUser(UUID uuid) {
        UserManager userManager = luckPerms.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }

    @Nullable
    public String getMetaData(UUID uuid, String key) {
        User user = getUser(uuid);
        if (user == null) return null;
        if (user.getCachedData().getMetaData().getMetaValue(key) == null) return null;
        return user.getCachedData().getMetaData().getMetaValue(key);
    }

    @Nullable
    public String getPrefix(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Nullable
    public String getGroup(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrimaryGroup();
    }

    @Nullable
    public String getSuffix(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getSuffix();
    }

    public static void setMetaData(UUID uuid, String key, String value) {
        User user = getUser(uuid);
        if (user == null) return;

        MetaNode node = MetaNode.builder(key, value).build();
        user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
        user.data().add(node);

        luckPerms.getUserManager().saveUser(user);
    }

    public static void removeMetaData(UUID uuid, String key) {
        User user = getUser(uuid);
        if (user == null) return;

        user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
        luckPerms.getUserManager().saveUser(user);
    }

    @Nullable
    public String getGroupMetaData(String group, String key) {
        Group luckPermsGroup = luckPerms.getGroupManager().getGroup(group);
        if (luckPermsGroup == null) return null;
        return luckPermsGroup.getCachedData().getMetaData().getMetaValue(key);
    }

    public static int getGroupWeight(String group) {
        Group luckPermsGroup = luckPerms.getGroupManager().getGroup(group);
        if (luckPermsGroup == null || luckPermsGroup.getWeight().isEmpty()) return 0;
        return luckPermsGroup.getWeight().getAsInt();
    }

    public static Boolean hasPermission(UUID uuid, String permission) {
        User user = getUser(uuid);
        if (user == null) return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    @NotNull
    public String getColor(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return "<gray>";
        if (user.getCachedData().getMetaData().getMetaValue("color") == null) return "<gray>";
        return user.getCachedData().getMetaData().getMetaValue("color");
    }
}