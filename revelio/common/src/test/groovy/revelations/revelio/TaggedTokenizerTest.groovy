package revelations.revelio;

import spock.lang.Specification
import org.apache.lucene.util.Version
import static org.apache.lucene.analysis.tokenattributes.TypeAttribute.DEFAULT_TYPE;
import static revelations.revelio.BilouTags.*
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * @author Christian Hargraves
 * Date: 5/3/12
 */
public class TaggedTokenizerTest extends Specification {
    TaggedTokenizer tokenizer;
//TODO: test for punctuation

    def "English sentence with less than mark, words, and spaces"() {
        given:
        String sentence = 'the brown<fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('<', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('jumped', OUTSIDE),
                new TokenHelper('at', OUTSIDE),
                new TokenHelper('them', OUTSIDE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with greater than mark, words, and spaces"() {
        given:
        String sentence = 'the brown>fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('>', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('jumped', OUTSIDE),
                new TokenHelper('at', OUTSIDE),
                new TokenHelper('them', OUTSIDE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with equals mark, words, and spaces"() {
        given:
        String sentence = 'the brown=fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('=', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('jumped', OUTSIDE),
                new TokenHelper('at', OUTSIDE),
                new TokenHelper('them', OUTSIDE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <TIMEX tags no space"() {
        given:
        String sentence = 'the brown fox jumped at<TIMEX TYPE="TIME">3:00PM</TIMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('jumped', OUTSIDE),
                new TokenHelper('at', OUTSIDE),
                new TokenHelper('<TIMEX TYPE="TIME">3:00PM</TIMEX>', ENTITY_TYPE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <TIMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox jumped at <TIMEX TYPE="TIME">3:00 PM</TIMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('jumped', OUTSIDE),
                new TokenHelper('at', OUTSIDE),
                new TokenHelper('<TIMEX TYPE="TIME">3:00 PM</TIMEX>', ENTITY_TYPE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <NUMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox is <NUMEX TYPE="MONEY">$12</NUMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('is', OUTSIDE),
                new TokenHelper('<NUMEX TYPE="MONEY">$12</NUMEX>', ENTITY_TYPE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <ENAMEX tags and spaces inside tag"() {
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charlie Brown</ENAMEX>, eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('<ENAMEX TYPE="PERSON">Charlie Brown</ENAMEX>', ENTITY_TYPE),
                new TokenHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('eats', OUTSIDE),
                new TokenHelper('rats', OUTSIDE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <ENAMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charly</ENAMEX>, eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('<ENAMEX TYPE="PERSON">Charly</ENAMEX>', ENTITY_TYPE),
                new TokenHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('eats', OUTSIDE),
                new TokenHelper('rats', OUTSIDE),
                new TokenHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, punctuation and spaces"() {
        given:
        String sentence = 'the (brown), fox!'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('(', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper(')', OUTSIDE, "PUNCTUATION"),
                new TokenHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenHelper('fox', OUTSIDE),
                new TokenHelper('!', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with only words and spaces"() {
        given:
        String sentence = 'the brown fox'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('brown', OUTSIDE),
                new TokenHelper('fox', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, numbers and spaces"() {
        given:
        String sentence = 'the 11 foxes'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', OUTSIDE),
                new TokenHelper('11', OUTSIDE),
                new TokenHelper('foxes', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    private List<TokenHelper> tokenize(String text) {
        List<TokenHelper> tokens = new ArrayList<TokenHelper>();
        tokenizer = new TaggedTokenizer(Version.LUCENE_CURRENT, new StringReader(text))
        CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
        EntityAttribute entityAtt = tokenizer.getAttribute(EntityAttribute.class);
        while (tokenizer.incrementToken()) {
            String term = termAtt.subSequence(0, termAtt.length());
            TokenHelper token = new TokenHelper(term, (EntityAttribute) entityAtt.clone())
            tokens.add(token);
        }
        return tokens
    }

}