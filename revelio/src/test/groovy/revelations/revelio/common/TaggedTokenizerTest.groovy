package revelations.revelio.common;

import spock.lang.Specification
import org.apache.lucene.util.Version

import static BilouTags.*
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute

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

    def "English sentence starting with <ENAMEX tag 2"() {
        given:
        String sentence = '<ENAMEX TYPE="WORK_OF_ART">Blue Velvet</ENAMEX> is a <TIMEX TYPE="DATE">1986</TIMEX> <ENAMEX TYPE="ITE.gpe">American</ENAMEX> mystery film.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('<ENAMEX TYPE="WORK_OF_ART">Blue Velvet</ENAMEX>', ENTITY_TYPE),
                new TokenTestHelper('is', OUTSIDE),
                new TokenTestHelper('a', OUTSIDE),
                new TokenTestHelper('<TIMEX TYPE="DATE">1986</TIMEX>', ENTITY_TYPE),
                new TokenTestHelper('<ENAMEX TYPE="ITE.gpe">American</ENAMEX>', ENTITY_TYPE),
                new TokenTestHelper('mystery', OUTSIDE),
                new TokenTestHelper('film', OUTSIDE),
                new TokenTestHelper('.', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence starting with <ENAMEX tag"() {
        given:
        String sentence = '<ENAMEX TYPE="PERSON">Charly</ENAMEX> eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('<ENAMEX TYPE="PERSON">Charly</ENAMEX>', ENTITY_TYPE),
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

    def "English sentence starting with <."() {
        given:
        String sentence = '<the) brown fox!'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('<', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper(')', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('!', OUTSIDE, "PUNCTUATION")]

        then:
        actual.equals(expected)
    }

    def "English sentence starting with punctuation."() {
        given:
        String sentence = '(the) brown fox!'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenTestHelper('(', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('the', OUTSIDE),
                new TokenTestHelper(')', OUTSIDE, "PUNCTUATION"),
                new TokenTestHelper('brown', OUTSIDE),
                new TokenTestHelper('fox', OUTSIDE),
                new TokenTestHelper('!', OUTSIDE, "PUNCTUATION")]

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

    def "Offsets for English sentence with only words and spaces"() {
        given:
        String sentence = 'the brown fox'

        when:
        List actual = tokenize(sentence)

        then:
        //the
        actual[0].startOffset == 0
        actual[0].endOffset == 3
        //brown
        actual[1].startOffset == 4
        actual[1].endOffset == 9
        //fox
        actual[2].startOffset == 10
        actual[2].endOffset == 13
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
        OffsetAttribute offsetAtt = tokenizer.getAttribute(OffsetAttribute.class);
        while (tokenizer.incrementToken()) {
            String term = termAtt.subSequence(0, termAtt.length());
            TokenTestHelper token = new TokenTestHelper(term, (EntityAttribute) entityAtt.clone(), offsetAtt.startOffset(), offsetAtt.endOffset())
            tokens.add(token);
        }
        return tokens
    }

}