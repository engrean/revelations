package revelations.revelio.common;

import spock.lang.Specification
import org.apache.lucene.util.Version

import static BilouTags.*
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

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
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('<', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('jumped', OUTSIDE),
                new TokenTestHelper('at', OUTSIDE),
                new TokenTestHelper('them', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with greater than mark, words, and spaces"() {
        given:
        String sentence = 'the brown>fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('>', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('jumped', OUTSIDE),
                new TokenTestHelper('at', OUTSIDE),
                new TokenTestHelper('them', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with equals mark, words, and spaces"() {
        given:
        String sentence = 'the brown=fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('=', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('jumped', OUTSIDE),
                new TokenTestHelper('at', OUTSIDE),
                new TokenTestHelper('them', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <TIMEX tags no space"() {
        given:
        String sentence = 'the brown fox jumped at<TIMEX TYPE="TIME">3:00PM</TIMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('jumped', OUTSIDE),
                new TokenTestHelper('at', OUTSIDE),
                new TokenTestHelper('<TIMEX TYPE="TIME">3:00PM</TIMEX>', ENTITY_TYPE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <TIMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox jumped at <TIMEX TYPE="TIME">3:00 PM</TIMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('jumped', OUTSIDE),
                new TokenTestHelper('at', OUTSIDE),
                new TokenTestHelper('<TIMEX TYPE="TIME">3:00 PM</TIMEX>', ENTITY_TYPE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <NUMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox is <NUMEX TYPE="MONEY">$12</NUMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('<NUMEX TYPE="MONEY">$12</NUMEX>', ENTITY_TYPE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <ENAMEX tags and spaces inside tag"() {
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charlie Brown</ENAMEX>, eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('<ENAMEX TYPE="PERSON">Charlie Brown</ENAMEX>', ENTITY_TYPE),
                new TokenTestHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('eats', OUTSIDE),
                new TokenTestHelper('rats', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, <ENAMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charly</ENAMEX>, eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('<ENAMEX TYPE="PERSON">Charly</ENAMEX>', ENTITY_TYPE),
                new TokenTestHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('eats', OUTSIDE),
                new TokenTestHelper('rats', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, punctuation and spaces"() {
        given:
        String sentence = 'the (brown), fox!'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('(', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper(')', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper(',', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('!', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence with only words and spaces"() {
        given:
        String sentence = 'the brown fox'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    def "English sentence with words, numbers and spaces"() {
        given:
        String sentence = 'the 11 foxes'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper('11', OUTSIDE),
                new TokenTestHelper('foxes', OUTSIDE)]

        then:
        actual.equals(expected)
    }

    private List<TokenTestHelper> tokenize(String text) {
        List<TokenTestHelper> tokens = new ArrayList<TokenTestHelper>();
        tokenizer = new TaggedTokenizer(Version.LUCENE_CURRENT, new StringReader(text))
        CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
        EntityAttribute entityAtt = tokenizer.getAttribute(EntityAttribute.class);
        while (tokenizer.incrementToken()) {
            String term = termAtt.subSequence(0, termAtt.length());
            TokenTestHelper token = new TokenTestHelper(term, (EntityAttribute) entityAtt.clone())
            tokens.add(token);
        }
        return tokens
    }

}