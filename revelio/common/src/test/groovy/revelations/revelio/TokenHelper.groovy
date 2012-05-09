package revelations.revelio

/**
 * @author Christian Hargraves
 * Date: 5/8/12
 */
public class TokenHelper {
    public String token;
    public EntityAttribute entityType;
    public String entityTypeS;

    TokenHelper(String token, EntityAttribute entityType) {
        this.token = token
        this.entityType = entityType
    }

    TokenHelper(String token, String entityTypeS) {
        this.token = token
        this.entityTypeS = entityTypeS
    }

    public String toString() {
        String str = "$token->";
        if (entityType){
            str += entityType
        }else if (entityTypeS){
            str += entityTypeS
        }
        return str;
    }

    public boolean equals(Object that) {
        return (token.equals(that.token) && entityType.toEntityTag().equals(that.entityTypeS))
    }
}