package be.esmay.pocketidsync.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class UserGroup {

    private String id;
    private String ldapId;
    private String name;
    private String friendlyName;

    private List<CustomClaim> customClaims;
    private String createdAt;
    private int userCount;

}