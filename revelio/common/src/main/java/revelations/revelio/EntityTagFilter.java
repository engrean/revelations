package revelations.revelio;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hargravescw
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
            if (entityAtt.isEntity()) {
                CharSequence token = termAttribute.subSequence(0, termAttribute.length());
                Matcher matcher = regex.matcher(token);
                if (matcher.matches()){
                    entityType = matcher.group(1);
                    subTokens = matcher.group(2).split("\\s+");
                    recordNextEntity(subTokens);
                }
            }
        }else{
            increment = false;
        }
        return increment;
    }

    protected void recordNextEntity(String[] subTokens){
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
            char[] buffer = termAttribute.buffer();
            for (int i = 0; i < token.length(); i++){
                buffer[i] = token.charAt(i);
            }
            termAttribute.setLength(token.length());
            index++;
        }else{
            index = 0;
        }
    }
}
