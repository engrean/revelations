package revelations.revelio;
import spock.lang.Specification
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.tokenattributes.TermAttribute
import org.apache.lucene.analysis.tokenattributes.TypeAttribute

import static org.apache.lucene.analysis.tokenattributes.TypeAttribute.DEFAULT_TYPE
import static revelations.revelio.TaggedTokenizer.*;

/**
 * @author Christian Hargraves
 * Date: 5/8/12
 */
public class EntityTagFilterTest extends Specification {


    def "What happens when there aren't any tags/entities"(){
        given:
        String sentence = "the quick brown fox is actually red."

        when:
        List<TokenHelper> actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('quick', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('is', DEFAULT_TYPE),
                new TokenHelper('actually', DEFAULT_TYPE),
                new TokenHelper('red', DEFAULT_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    private List<TokenHelper> tokenize(String text) {
        List<TokenHelper> tokens = new ArrayList<TokenHelper>();
        EntityTagFilter tokenizer = new EntityTagFilter(new TaggedTokenizer(Version.LUCENE_CURRENT, new StringReader(text)))
        TermAttribute termAtt = tokenizer.getAttribute(TermAttribute.class);
        TypeAttribute typeAtt = tokenizer.getAttribute(TypeAttribute.class);
        while (tokenizer.incrementToken()) {
            TokenHelper token = new TokenHelper(termAtt.term(), typeAtt.type())
            tokens.add(token);
        }
        return tokens
    }


}