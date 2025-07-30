package be.esmay.pocketidsync.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private boolean isAdmin;
    private boolean isActive;
    private List<UserGroup> userGroups;
    private List<CustomClaim> customClaims;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public Map<String, Object> getCustomClaimsAsMap() {
        if (this.customClaims == null) {
            return Map.of();
        }
        return this.customClaims.stream().collect(Collectors.toMap(CustomClaim::getKey, CustomClaim::getValue));
    }

    public UUID getMinecraftUUID() {
        return this.getCustomClaimsAsMap().get("minecraft_uuid") != null ? UUID.fromString(this.getCustomClaimsAsMap().get("minecraft_uuid").toString()) : null;
    }

    public String getMinecraftName() {
        return this.getCustomClaimsAsMap().get("minecraft_name") != null ? this.getCustomClaimsAsMap().get("minecraft_name").toString() : null;
    }

    public List<CustomClaim> modifyUserClaims(UUID uniqueId, String userName) {
        if (this.customClaims == null) {
            this.customClaims = List.of();
        }

        Map<String, Object> claimsMap = this.getCustomClaimsAsMap();
        claimsMap.put("minecraft_uuid", uniqueId.toString());
        claimsMap.put("minecraft_name", userName);

        this.customClaims = claimsMap.entrySet().stream()
                .map(entry -> new CustomClaim(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return this.customClaims;
    }
}