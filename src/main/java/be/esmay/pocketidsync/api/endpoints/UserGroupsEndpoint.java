package be.esmay.pocketidsync.api.endpoints;

import be.esmay.pocketidsync.api.BaseEndpoint;
import be.esmay.pocketidsync.api.PocketIDClient;
import be.esmay.pocketidsync.api.models.PagedResponse;
import be.esmay.pocketidsync.api.models.UserGroup;
import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserGroupsEndpoint extends BaseEndpoint {

    public UserGroupsEndpoint(PocketIDClient client) {
        super(client);
    }
    
    public CompletableFuture<PagedResponse<UserGroup>> listGroups(int page, int limit) {
        return getWithTypeToken("/user-groups?pagination[page]=" + page + "&pagination[limit]=" + limit, 
                new TypeToken<PagedResponse<UserGroup>>() {});
    }
    
    public CompletableFuture<PagedResponse<UserGroup>> listGroups(int page, int limit, String search) {
        String endpoint = "/user-groups?pagination[page]=" + page + "&pagination[limit]=" + limit;
        if (search != null && !search.trim().isEmpty()) {
            endpoint += "&search=" + search;
        }
        return getWithTypeToken(endpoint, new TypeToken<PagedResponse<UserGroup>>() {});
    }
    
    public CompletableFuture<PagedResponse<UserGroup>> listGroups(int page, int limit, String search, String sortBy, String sortOrder) {
        String endpoint = "/user-groups?pagination[page]=" + page + "&pagination[limit]=" + limit;
        if (search != null && !search.trim().isEmpty())
            endpoint += "&search=" + search;

        if (sortBy != null && !sortBy.trim().isEmpty())
            endpoint += "&sort[field]=" + sortBy;

        if (sortOrder != null && !sortOrder.trim().isEmpty())
            endpoint += "&sort[order]=" + sortOrder;

        return getWithTypeToken(endpoint, new TypeToken<PagedResponse<UserGroup>>() {});
    }
    
    public CompletableFuture<UserGroup> createGroup(String name, String friendlyName) {
        Map<String, String> body = Map.of(
            "name", name,
            "friendlyName", friendlyName
        );

        return post("/user-groups", body, UserGroup.class);
    }
}