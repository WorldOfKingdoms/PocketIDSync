package be.esmay.pocketidsync.utils;

import be.esmay.pocketidsync.utils.configuration.ConfigurateConfig;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;

@Getter
public final class DefaultConfiguration extends ConfigurateConfig {

    private final String version;

    private final String baseUrl;
    private final String apiKey;
    private final List<String> syncGroups;

    private final String luckpermsRedisHost;
    private final int luckpermsRedisPort;
    private final String luckpermsRedisPassword;

    private final String registerAccountMessage;
    private final String commandUsage;
    private final String commandInvalidEmail;
    private final String commandAlreadyRegistered;
    private final String commandEmailAlreadyRegistered;
    private final String commandFailedToRegister;
    private final String commandFailedToGenerateToken;
    private final String commandRegisteredSuccessfully;
    private final String commandClickToFinish;

    @SneakyThrows
    public DefaultConfiguration(Path folder) {
        super(folder, "config.yml");

        this.version = this.rootNode.node("_version").getString("1");

        this.baseUrl = this.rootNode.node("pocketid", "base-url").getString("https://api.pocketid.be");
        this.apiKey = this.rootNode.node("pocketid", "api-key").getString("YOUR_API_KEY_HERE");
        this.syncGroups = this.rootNode.node("sync-groups").getList(String.class, List.of("admin"));

        this.luckpermsRedisHost = this.rootNode.node("luckperms", "redis", "host").getString("localhost");
        this.luckpermsRedisPort = this.rootNode.node("luckperms", "redis", "port").getInt(6379);
        this.luckpermsRedisPassword = this.rootNode.node("luckperms", "redis", "password").getString("");

        this.registerAccountMessage = this.rootNode.node("messages", "register-account").getString("<red>Welcome to the Staff team! Please create a PocketID account using /pocketid <email>.");
        this.commandUsage = this.rootNode.node("messages", "command", "usage").getString("<red>Usage: /pocketid <email>");
        this.commandAlreadyRegistered = this.rootNode.node("messages", "command", "already-registered").getString("<red>You already have a PocketID account associated with your Minecraft account.");
        this.commandInvalidEmail = this.rootNode.node("messages", "command", "invalid-email").getString("<red>Invalid email format. Please provide a valid email address.");
        this.commandEmailAlreadyRegistered = this.rootNode.node("messages", "command", "email-already-registered").getString("<red>The email address is already registered to a PocketID account.");
        this.commandFailedToRegister = this.rootNode.node("messages", "command", "failed-to-register").getString("<red>Failed to create a PocketID account. Please try again later.");
        this.commandFailedToGenerateToken = this.rootNode.node("messages", "command", "failed-to-generate-token").getString("<red>Failed to generate a login token for your PocketID account. Please contact an Admin.");
        this.commandRegisteredSuccessfully = this.rootNode.node("messages", "command", "registered-successfully").getString("<green>PocketID account created successfully! Click here to sign in and connect a passkey. You have 1 hour to do this, if you fail to do this then contact an Admin.");
        this.commandClickToFinish = this.rootNode.node("messages", "command", "click-to-finish").getString("<green>Click here to finish your registration.");
        this.saveConfiguration();
    }
}
