package be.esmay.pocketidsync.api;

import be.esmay.pocketidsync.api.endpoints.CustomClaimsEndpoint;
import be.esmay.pocketidsync.api.endpoints.UserGroupsEndpoint;
import be.esmay.pocketidsync.api.endpoints.UsersEndpoint;
import lombok.Getter;

public final class PocketID {

    @Getter
    private final PocketIDClient client;
    private final UsersEndpoint users;
    private final UserGroupsEndpoint userGroups;
    private final CustomClaimsEndpoint customClaims;
    
    public PocketID(String baseUrl, String apiKey) {
        this.client = new PocketIDClient(baseUrl, apiKey);
        this.users = new UsersEndpoint(this.client);
        this.userGroups = new UserGroupsEndpoint(this.client);
        this.customClaims = new CustomClaimsEndpoint(this.client);
    }
    
    public UsersEndpoint users() {
        return this.users;
    }
    
    public UserGroupsEndpoint userGroups() {
        return this.userGroups;
    }
    
    public CustomClaimsEndpoint customClaims() {
        return this.customClaims;
    }

}