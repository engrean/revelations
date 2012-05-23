package revelations.revelio.common

/**
 * @author Christian Hargraves
 * Date: 5/8/12
 */
public class TokenTestHelper {
    public String token;
    public EntityAttribute entityType;
    public String entityTypeS;
    public String metaData;
    public int startOffset;
    public int endOffset;

    TokenTestHelper(String token, EntityAttribute entityType) {
        this.token = token
        this.entityType = entityType
    }

    TokenTestHelper(String token, EntityAttribute entityType, int startOffset, int endOffset) {
        this.token = token
        this.entityType = entityType
        this.startOffset = startOffset
        this.endOffset = endOffset
    }

    TokenTestHelper(String token, String entityTypeS) {
        this.token = token
        this.entityTypeS = entityTypeS
    }

    TokenTestHelper(String token, String entityTypeS, String metaData) {
        this.token = token
        this.entityTypeS = entityTypeS
        this.metaData = metaData
    }

    public String toString() {
        String str = "$token->";
        if (entityType) {
            str += entityType
        } else if (entityTypeS) {
            str += entityTypeS
        }
        str += '->'
        if (metaData) {
            str += metaData;
        } else {
            str += getMeta();
        }

        return str;
    }

    public boolean equals(Object that) {
        boolean isEqual = (token.equals(that.token) && entityType.toEntityTag().equals(that.entityTypeS));
        if (isEqual && that.metaData) {
            final String meta = getMeta()
            isEqual = (that.metaData.equals(meta));
        }
        return isEqual;
    }

    public String getMeta() {
        String meta = '';
        if (entityType && entityType.isPunctuationMark()) {
            meta = 'PUNCTUATION';
        } else if (entityType && entityType.isCapitalized()) {
            meta = 'CAPITALIZED'
        }
        return meta;
    }
}