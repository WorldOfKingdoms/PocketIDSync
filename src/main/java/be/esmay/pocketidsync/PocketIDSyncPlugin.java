package be.esmay.pocketidsync;

import be.esmay.pocketidsync.api.PocketID;
import be.esmay.pocketidsync.api.models.UserGroup;
import be.esmay.pocketidsync.channel.LuckpermsChannel;
import be.esmay.pocketidsync.commands.PocketIDCommand;
import be.esmay.pocketidsync.listeners.PlayerJoinListener;
import be.esmay.pocketidsync.utils.DefaultConfiguration;
import be.esmay.pocketidsync.utils.LuckPermsUtils;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@Plugin(
        id = "pocketidsync",
        name = "PocketIDSync",
        version = "1.0.0",
        authors = {"Esmaybe"},
        dependencies = {
                @Dependency(id = "luckperms")
        }
)
public final class PocketIDSyncPlugin {

    @Getter
    private static PocketIDSyncPlugin instance;

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    private PocketID pocketID;
    private final DefaultConfiguration defaultConfiguration;

    private final List<UserGroup> groups = new ArrayList<>();
    private final Map<UUID, String> userIdCache = new HashMap<>();

    private JedisPool luckpermsRedisPool;
    private LuckpermsChannel luckpermsChannel;

    @Inject
    public PocketIDSyncPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;

        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        this.defaultConfiguration = new DefaultConfiguration(this.dataDirectory);
    }

    public CompletableFuture<Boolean> updateGroups(UUID uniqueId, String username, boolean isAdmin, List<UserGroup> groups) {
        String userId = this.userIdCache.get(uniqueId);
        if (userId == null) {
            this.logger.warn("User ID for player {} not found in cache, skipping group update.", username);
            return CompletableFuture.supplyAsync(() -> false);
        }

        return CompletableFuture.supplyAsync(() -> {
            User user = LuckPermsUtils.getUser(uniqueId);
            if (user == null)
                return false;

            List<String> playerGroups = user.getNodes(NodeType.INHERITANCE)
                    .stream()
                    .map(InheritanceNode::getGroupName)
                    .map(String::toLowerCase)
                    .filter(group -> this.defaultConfiguration.getSyncGroups().contains(group))
                    .toList();

            groups.removeIf(group -> this.defaultConfiguration.getSyncGroups().contains(group.getName().toLowerCase()) && !playerGroups.contains(group.getName().toLowerCase()));

            playerGroups.stream().filter(group -> groups.stream().noneMatch(g -> g.getName().equals(group))).forEach(group -> {
                UserGroup userGroup = this.getUserGroupByName(group);
                if (userGroup != null) {
                    groups.add(userGroup);
                }
            });

            if (groups.stream().noneMatch(group -> this.defaultConfiguration.getSyncGroups().contains(group.getName().toLowerCase())) && !isAdmin) {
                this.logger.info("No valid groups found for user {}, deleting user from PocketID.", username);
                this.pocketID.users().deleteUser(userId).join();
                this.userIdCache.remove(uniqueId);
                return false;
            }

            this.pocketID.users().updateUserGroups(userId, groups.stream().map(UserGroup::getId).toList());
            return true;
        });
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("Initializing PocketIDSync plugin...");

        this.pocketID = new PocketID(this.defaultConfiguration.getBaseUrl(), this.defaultConfiguration.getApiKey());
        this.groups.addAll(this.pocketID.userGroups().listGroups(1, 1000).join().getData());

        this.proxyServer.getEventManager().register(this, new PlayerJoinListener(this));

        CommandManager commandManager = this.proxyServer.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("pocketid").plugin(this).build();
        PocketIDCommand pocketIDCommand = new PocketIDCommand(this);
        commandManager.register(commandMeta, pocketIDCommand);

        this.luckpermsRedisPool = this.setupRedisPool(this.defaultConfiguration.getLuckpermsRedisHost(), this.defaultConfiguration.getLuckpermsRedisPort(), this.defaultConfiguration.getLuckpermsRedisPassword());
        CompletableFuture.runAsync(() -> {
            Jedis jedis = this.luckpermsRedisPool.getResource();
            jedis.subscribe(this.luckpermsChannel = new LuckpermsChannel(this), "luckperms:update");
        });

        this.logger.info("PocketIDSync plugin initialized successfully.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("Shutting down PocketIDSync plugin...");

        this.pocketID = null;

        this.logger.info("PocketIDSync plugin shut down successfully.");
    }

    private JedisPool setupRedisPool(String hostName, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);

        JedisPool jedisPool = new JedisPool(poolConfig, hostName, port, 0, password);
        this.getLogger().info("Connected to Redis server at {}:{}", hostName, port);

        return jedisPool;
    }

    private UserGroup getUserGroupByName(String name) {
        UserGroup userGroup = this.groups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (userGroup == null) {
            userGroup = this.pocketID.userGroups().createGroup(name, name).join();
        }

        return userGroup;
    }
}