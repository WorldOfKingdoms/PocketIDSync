package be.esmay.pocketidsync.api.endpoints;

import be.esmay.pocketidsync.api.BaseEndpoint;
import be.esmay.pocketidsync.api.PocketIDClient;
import be.esmay.pocketidsync.api.models.OneTimeAccessToken;
import be.esmay.pocketidsync.api.models.PagedResponse;
import be.esmay.pocketidsync.api.models.User;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.proxy.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UsersEndpoint extends BaseEndpoint {

    public UsersEndpoint(PocketIDClient client) {
        super(client);
    }

    public CompletableFuture<PagedResponse<User>> listUsers(int page, int limit) {
        return getWithTypeToken("/users?pagination[page]=" + page + "&pagination[limit]=" + limit, new TypeToken<PagedResponse<User>>() {
        });
    }

    public CompletableFuture<User> getUserByUUID(UUID uniqueId) {
        CompletableFuture<PagedResponse<User>> result = getWithTypeToken("/users?pagination[page]=" + "1" + "&pagination[limit]=" + "1000", new TypeToken<PagedResponse<User>>() {
        });

        return result.thenApply(pagedResponse -> pagedResponse.getData().stream()
                .filter(user -> user.getMinecraftUUID().equals(uniqueId))
                .findFirst()
                .orElse(null));
    }

    public CompletableFuture<User> getUserByEmail(String email) {
        CompletableFuture<PagedResponse<User>> result = getWithTypeToken("/users?pagination[page]=" + "1" + "&pagination[limit]=" + "1000", new TypeToken<PagedResponse<User>>() {
        });

        return result.thenApply(pagedResponse -> pagedResponse.getData().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null));
    }

    public CompletableFuture<Boolean> userExists(UUID uniqueId) {
        return getUserByUUID(uniqueId).thenApply(Objects::nonNull);
    }

    public CompletableFuture<User> getUser(String userId) {
        return get("/users/" + userId, User.class);
    }

    public CompletableFuture<User> getCurrentUser() {
        return get("/users/me", User.class);
    }

    public CompletableFuture<User> createUser(Player player, String email) {
        Map<String, Object> values = Map.of(
                "disabled", false,
                "email", email,
                "firstName", player.getUsername(),
                "isAdmin", false,
                "username", player.getUniqueId().toString().split("-")[0]
        );

        return post("/users", values, User.class);
    }

    public CompletableFuture<User> updateUser(String userId, User user) {
        return put("/users/" + userId, user, User.class);
    }

    public CompletableFuture<Void> deleteUser(String userId) {
        return delete("/users/" + userId);
    }

    public CompletableFuture<User> updateUserGroups(String userId, List<String> groupIds) {
        Map<String, List<String>> body = Map.of("userGroupIds", groupIds);
        return put("/users/" + userId + "/user-groups", body, User.class);
    }
    
    public CompletableFuture<OneTimeAccessToken> createOneTimeAccessToken(String userId) {
        String expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).toString();
        Map<String, String> body = Map.of(
            "expiresAt", expiresAt,
            "userId", userId
        );
        return post("/users/" + userId + "/one-time-access-token", body, OneTimeAccessToken.class);
    }
}