package revelations.revelio;
import spock.lang.Specification
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.tokenattributes.TermAttribute
import org.apache.lucene.analysis.tokenattributes.TypeAttribute

import static revelations.revelio.BilouTags.*
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * @author Christian Hargraves
 * Date: 5/8/12
 */
public class EntityTagFilterTest extends Specification {
//TODO: test for punctuation
    def "ENAMEX type PERSON, using BIL and O"(){
        given:
        String sentence = 'the quick <ENAMEX TYPE="PERSON">Megan D. Fox</ENAMEX> is actually red.'

        when:
        List<TokenHelper> actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('quick', OUTSIDE),
                new TokenHelper('Megan', "${BEGIN}-PERSON"),
                new TokenHelper('D.', "${INSIDE}-PERSON"),
                new TokenHelper('Fox', "${LAST}-PERSON"),
                new TokenHelper('is', OUTSIDE),
                new TokenHelper('actually', OUTSIDE),
                new TokenHelper('red', OUTSIDE),
                new TokenHelper('.', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    def "ENAMEX type PERSON, using U and O"(){
        given:
        String sentence = 'the quick <ENAMEX TYPE="PERSON">Fox</ENAMEX> is actually red.'

        when:
        List<TokenHelper> actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('quick', OUTSIDE),
                new TokenHelper('Fox', "${UNIT}-PERSON"),
                new TokenHelper('is', OUTSIDE),
                new TokenHelper('actually', OUTSIDE),
                new TokenHelper('red', OUTSIDE),
                new TokenHelper('.', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    def "What happens when there aren't any tags/entities"(){
        given:
        String sentence = "the quick brown fox is actually red."

        when:
        List<TokenHelper> actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('quick', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('is', OUTSIDE),
                new TokenHelper('actually', OUTSIDE),
                new TokenHelper('red', OUTSIDE),
                new TokenHelper('.', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    private List<TokenHelper> tokenize(String text) {
        List<TokenHelper> tokens = new ArrayList<TokenHelper>();
        EntityTagFilter tokenizer = new EntityTagFilter(new TaggedTokenizer(Version.LUCENE_CURRENT, new StringReader(text)))
        CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
        EntityAttribute entityAtt = tokenizer.getAttribute(EntityAttribute.class);
        while (tokenizer.incrementToken()) {
            String term = termAtt.subSequence(0, termAtt.length());
            TokenHelper token = new TokenHelper(term, (EntityAttribute)entityAtt.clone())
            tokens.add(token);
        }
        return tokens
    }

}