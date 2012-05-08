package revelations.revelio;

import spock.lang.Specification
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.tokenattributes.TermAttribute
import org.apache.lucene.analysis.tokenattributes.TypeAttribute
import static org.apache.lucene.analysis.tokenattributes.TypeAttribute.DEFAULT_TYPE;
import static revelations.revelio.TaggedTokenizer.*;

/**
 * @author Christian Hargraves
 * Date: 5/3/12
 */
public class TaggedTokenizerTest extends Specification {
    TaggedTokenizer tokenizer;

    def "English sentence with less than mark, words, and spaces"() {
        given:
        String sentence = 'the brown<fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('<', PUNCTUATION_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('jumped', DEFAULT_TYPE),
                new TokenHelper('at', DEFAULT_TYPE),
                new TokenHelper('them', DEFAULT_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with greater than mark, words, and spaces"() {
        given:
        String sentence = 'the brown>fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('>', PUNCTUATION_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('jumped', DEFAULT_TYPE),
                new TokenHelper('at', DEFAULT_TYPE),
                new TokenHelper('them', DEFAULT_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with equals mark, words, and spaces"() {
        given:
        String sentence = 'the brown=fox jumped at them.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('=', PUNCTUATION_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('jumped', DEFAULT_TYPE),
                new TokenHelper('at', DEFAULT_TYPE),
                new TokenHelper('them', DEFAULT_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, <TIMEX tags no space"() {
        given:
        String sentence = 'the brown fox jumped at<TIMEX TYPE="TIME">3:00 PM</TIMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('jumped', DEFAULT_TYPE),
                new TokenHelper('at', DEFAULT_TYPE),
                new TokenHelper('<TIMEX TYPE="TIME">3:00 PM</TIMEX>', ENTITY_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, <TIMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox jumped at <TIMEX TYPE="TIME">3:00 PM</TIMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('jumped', DEFAULT_TYPE),
                new TokenHelper('at', DEFAULT_TYPE),
                new TokenHelper('<TIMEX TYPE="TIME">3:00 PM</TIMEX>', ENTITY_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, <NUMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox is <NUMEX TYPE="MONEY">$12</NUMEX>.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('is', DEFAULT_TYPE),
                new TokenHelper('<NUMEX TYPE="MONEY">$12</NUMEX>', ENTITY_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, <ENAMEX tags and spaces inside tag"() {
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charlie Brown</ENAMEX>, eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper(',', PUNCTUATION_TYPE),
                new TokenHelper('<ENAMEX TYPE="PERSON">Charlie Brown</ENAMEX>', ENTITY_TYPE),
                new TokenHelper(',', PUNCTUATION_TYPE),
                new TokenHelper('eats', DEFAULT_TYPE),
                new TokenHelper('rats', DEFAULT_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, <ENAMEX tags and spaces"() {
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charly</ENAMEX>, eats rats.'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper(',', PUNCTUATION_TYPE),
                new TokenHelper('<ENAMEX TYPE="PERSON">Charly</ENAMEX>', ENTITY_TYPE),
                new TokenHelper(',', PUNCTUATION_TYPE),
                new TokenHelper('eats', DEFAULT_TYPE),
                new TokenHelper('rats', DEFAULT_TYPE),
                new TokenHelper('.', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, punctuation and spaces"() {
        given:
        String sentence = 'the (brown), fox!'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('(', PUNCTUATION_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper(')', PUNCTUATION_TYPE),
                new TokenHelper(',', PUNCTUATION_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE),
                new TokenHelper('!', PUNCTUATION_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with only words and spaces"() {
        given:
        String sentence = 'the brown fox'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('brown', DEFAULT_TYPE),
                new TokenHelper('fox', DEFAULT_TYPE)]

        then:
        expected == actual
    }

    def "English sentence with words, numbers and spaces"() {
        given:
        String sentence = 'the 11 foxes'

        when:
        List actual = tokenize(sentence)
        def expected = [
                new TokenHelper('the', DEFAULT_TYPE),
                new TokenHelper('11', DEFAULT_TYPE),
                new TokenHelper('foxes', DEFAULT_TYPE)]

        then:
        expected == actual
    }

    private List<TokenHelper> tokenize(String text) {
        List<TokenHelper> tokens = new ArrayList<TokenHelper>();
        tokenizer = new TaggedTokenizer(Version.LUCENE_CURRENT, new StringReader(text))
        TermAttribute termAtt = tokenizer.getAttribute(TermAttribute.class);
        TypeAttribute typeAtt = tokenizer.getAttribute(TypeAttribute.class);
        while (tokenizer.incrementToken()) {
            TokenHelper token = new TokenHelper(termAtt.term(), typeAtt.type())
            tokens.add(token);
        }
        return tokens
    }

}