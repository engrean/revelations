package revelations.revelio.common;
import spock.lang.Specification
import org.apache.lucene.util.Version

import static BilouTags.*
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

/**
 * @author Christian Hargraves
 * Date: 5/8/12
 */
public class EntityTagFilterTest extends Specification {

    def "isCapitalized sets attribute to false with null attribute"(){
        when:
        EntityTagFilter.checkCapitalization('Test', null)

        then:
        notThrown(NullPointerException)
    }

    def "isCapitalized sets attribute to false with null token"(){
        given:
        EntityAttribute entityAttribute = new EntityAttributeImpl();

        when:
        EntityTagFilter.checkCapitalization(null, entityAttribute)

        then:
        !entityAttribute.isCapitalized()
    }

    def "isCapitalized sets attribute to false with whitespace"(){
        given:
        EntityAttribute entityAttribute = new EntityAttributeImpl();

        when:
        EntityTagFilter.checkCapitalization(" ", entityAttribute)

        then:
        !entityAttribute.isCapitalized()
    }

    def "isCapitalized sets attribute to false with punctuation"(){
        given:
        EntityAttribute entityAttribute = new EntityAttributeImpl();

        when:
        EntityTagFilter.checkCapitalization("!", entityAttribute)

        then:
        !entityAttribute.isCapitalized()
    }

    def "isCapitalized sets attribute to true with capitalized word"(){
        given:
        EntityAttribute entityAttribute = new EntityAttributeImpl();

        when:
        EntityTagFilter.checkCapitalization("The", entityAttribute)

        then:
        entityAttribute.isCapitalized()
    }

    def "isCapitalized sets attribute to false with non-capitalized word"(){
        given:
        EntityAttribute entityAttribute = new EntityAttributeImpl();

        when:
        EntityTagFilter.checkCapitalization("the", entityAttribute)

        then:
        !entityAttribute.isCapitalized()
    }

    def "ENAMEX type PERSON, using all of BILOU part3"(){
        given:
        String sentence = '<ENAMEX TYPE="WORK_OF_ART">Blue</ENAMEX> was directed in <TIMEX TYPE="DATE">1986</TIMEX>'

        when:
        List<TokenTestHelper> actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('Blue', "${UNIT}-WORK_OF_ART", "CAPITALIZED"),
                new TokenTestHelper('was', OUTSIDE),
                new TokenTestHelper('directed', OUTSIDE),
                new TokenTestHelper('in', OUTSIDE),
                new TokenTestHelper('1986', "${UNIT}-DATE")]

        then:
        actual.equals(expected)
    }

    def "ENAMEX type PERSON, using all of BILOU part2"(){
        given:
        String sentence = '<ENAMEX TYPE="WORK_OF_ART">Blue Velvet</ENAMEX> is a <TIMEX TYPE="DATE">1986</TIMEX> <ENAMEX TYPE="ITE.gpe">American</ENAMEX> mystery film.'

        when:
        List<TokenTestHelper> actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('Blue', "${BEGIN}-WORK_OF_ART", "CAPITALIZED"),
                new TokenTestHelper('Velvet', "${LAST}-WORK_OF_ART", "CAPITALIZED"),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('a', OUTSIDE),
                new TokenTestHelper('1986', "${UNIT}-DATE"),
                new TokenTestHelper('American', "${UNIT}-ITE.gpe", "CAPITALIZED"),
                new TokenTestHelper('mystery', OUTSIDE),
                new TokenTestHelper('film', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "ENAMEX type PERSON, using all of BILOU"(){
        given:
        String sentence = 'The <ENAMEX TYPE="WORK_OF_ART">Blue Velvet</ENAMEX> is a <TIMEX TYPE="DATE">1986</TIMEX> <ENAMEX TYPE="ITE.gpe">American</ENAMEX> mystery film written and directed by <ENAMEX TYPE="PERSON">David Lynch</ENAMEX>.'

        when:
        List<TokenTestHelper> actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('The', OUTSIDE, "CAPITALIZED"),
                new TokenTestHelper('Blue', "${BEGIN}-WORK_OF_ART", "CAPITALIZED"),
                new TokenTestHelper('Velvet', "${LAST}-WORK_OF_ART", "CAPITALIZED"),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('a', OUTSIDE),
                new TokenTestHelper('1986', "${UNIT}-DATE"),
                new TokenTestHelper('American', "${UNIT}-ITE.gpe", "CAPITALIZED"),
                new TokenTestHelper('mystery', OUTSIDE),
                new TokenTestHelper('film', OUTSIDE),
                new TokenTestHelper('written', OUTSIDE),
                new TokenTestHelper('and', OUTSIDE),
                new TokenTestHelper('directed', OUTSIDE),
                new TokenTestHelper('by', OUTSIDE),
                new TokenTestHelper('David', "${BEGIN}-PERSON", "CAPITALIZED"),
                new TokenTestHelper('Lynch', "${LAST}-PERSON", "CAPITALIZED"),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }



    def "ENAMEX type PERSON, using BIL and O"(){
        given:
        String sentence = 'The quick <ENAMEX TYPE="PERSON">Megan D. Fox</ENAMEX> is actually red.'

        when:
        List<TokenTestHelper> actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('The', OUTSIDE, "CAPITALIZED"),
                new TokenTestHelper('quick', OUTSIDE),
                new TokenTestHelper('Megan', "${BEGIN}-PERSON", "CAPITALIZED"),
                new TokenTestHelper('D.', "${INSIDE}-PERSON", "CAPITALIZED"),
                new TokenTestHelper('Fox', "${LAST}-PERSON", "CAPITALIZED"),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('actually', OUTSIDE),
                new TokenTestHelper('red', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "ENAMEX type PERSON, using U and O"(){
        given:
        String sentence = 'The quick <ENAMEX TYPE="PERSON">Fox</ENAMEX> is actually red.'

        when:
        List<TokenTestHelper> actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('The', OUTSIDE, "CAPITALIZED"),
                new TokenTestHelper('quick', OUTSIDE),
                new TokenTestHelper('Fox', "${UNIT}-PERSON", "CAPITALIZED"),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('actually', OUTSIDE),
                new TokenTestHelper('red', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "What happens when there aren't any tags/entities"(){
        given:
        String sentence = "The quick brown fox is actually red."

        when:
        List<TokenTestHelper> actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('The', OUTSIDE, "CAPITALIZED"),
                new TokenTestHelper('quick', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('actually', OUTSIDE),
                new TokenTestHelper('red', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    private List<TokenTestHelper> tokenize(String text) {
        List<TokenTestHelper> tokens = new ArrayList<TokenTestHelper>();
        EntityTagFilter tokenizer = new EntityTagFilter(new TaggedTokenizer(Version.LUCENE_CURRENT, new StringReader(text)))
        CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
        EntityAttribute entityAtt = tokenizer.getAttribute(EntityAttribute.class);
        while (tokenizer.incrementToken()) {
            String term = termAtt.subSequence(0, termAtt.length());
            TokenTestHelper token = new TokenTestHelper(term, (EntityAttribute)entityAtt.clone())
            tokens.add(token);
        }
        return tokens
    }

}