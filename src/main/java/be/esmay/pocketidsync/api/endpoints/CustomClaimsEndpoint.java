package be.esmay.pocketidsync.api.endpoints;

import be.esmay.pocketidsync.api.BaseEndpoint;
import be.esmay.pocketidsync.api.PocketIDClient;
import be.esmay.pocketidsync.api.models.CustomClaim;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CustomClaimsEndpoint extends BaseEndpoint {
    
    public CustomClaimsEndpoint(PocketIDClient client) {
        super(client);
    }
    
    public CompletableFuture<List<String>> getClaimSuggestions() {
        return getWithTypeToken("/custom-claims/suggestions", new TypeToken<List<String>>() {});
    }
    
    public CompletableFuture<List<CustomClaim>> updateUserClaims(String userId, List<CustomClaim> claims) {
        return putWithTypeToken("/custom-claims/user/" + userId, claims, new TypeToken<List<CustomClaim>>() {});
    }
    
    public CompletableFuture<List<CustomClaim>> updateGroupClaims(String groupId, List<CustomClaim> claims) {
        return putWithTypeToken("/custom-claims/group/" + groupId, claims, new TypeToken<List<CustomClaim>>() {});
    }

}