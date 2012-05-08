package revelations.revelio;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

/**
 * @author hargravescw
 *         Date: 5/8/12
 */
public class EntityTagFilter extends TokenFilter {

    public EntityTagFilter(TokenStream in){
        super(in);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        return input.incrementToken();
    }
}
