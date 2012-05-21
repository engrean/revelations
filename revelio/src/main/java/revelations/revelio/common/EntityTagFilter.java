package revelations.revelio.common;

import com.ibm.icu.lang.UCharacter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christian Hargraves
 *         Date: 5/8/12
 */
public class EntityTagFilter extends TokenFilter {

    private EntityAttribute entityAtt = addAttribute(EntityAttribute.class);
    private CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private static final Pattern regex = Pattern.compile("<[A-Z]+ TYPE=\"([^\"]+)\">([^<]+)</[A-Z]+>");
    private String[] subTokens;
    private int index;
    private String entityType;


    public EntityTagFilter(TokenStream in) {
        super(in);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        boolean increment = (subTokens != null && index < subTokens.length);
        if (increment){
            recordNextEntity(subTokens);
        }else if(input.incrementToken()){
            increment = true;
            CharSequence token = termAttribute.subSequence(0, termAttribute.length());
            if (entityAtt.isEntity()) {
                Matcher matcher = regex.matcher(token);
                if (matcher.matches()){
                    entityType = matcher.group(1);
                    //TODO: I should use the whitespace tokenizer for this instead.
                    subTokens = matcher.group(2).split("\\s+");
                    recordNextEntity(subTokens);
                }
            }else{
                checkCapitalization(token.toString(), entityAtt);
            }
        }else{
            increment = false;
        }
        return increment;
    }

    protected void recordNextEntity(String[] subTokens){
        //TODO: Need to add support for capitalization detection
        if (index < subTokens.length){
            String token = subTokens[index];
            if (subTokens.length == 1){
                entityAtt.setEntityUnit();
            }else if (index == 0){
                entityAtt.setEntityBegin();
            }else if (index < (subTokens.length - 1)){
                entityAtt.setEntityInside();
            }else{
                entityAtt.setEntityLast();
            }
            entityAtt.setEntityType(entityType);
            for (int i = 0; i < token.length(); i++){
                termAttribute.buffer()[i] = token.charAt(i);
            }
            termAttribute.setLength(token.length());
            checkCapitalization(token, entityAtt);
            index++;
        }else{
            index = 0;
        }
    }

    protected static void checkCapitalization(String token, EntityAttribute entityAtt){
        if (token != null && entityAtt != null){
            int codePoint = UCharacter.codePointAt(token, 0);
            entityAtt.setIsCapitalized(UCharacter.isUUppercase(codePoint));
        }
    }
}
