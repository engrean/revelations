package revelations.revelio

/**
 * @author Christian Hargraves
 * Date: 5/8/12
 */
public class TokenHelper {
    public String token;
    public String type;

    TokenHelper(token, type) {
        this.token = token
        this.type = type
    }

    public String toString() {
        return "$token->$type";
    }

    public boolean equals(Object that) {
        return (token.equals(that.token) && type.equals(that.type))
    }
}