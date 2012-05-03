package revelations.revelio;

import spock.lang.Specification
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.tokenattributes.TermAttribute

/**
 * @author hargravescw
 * Date: 5/3/12
 */
public class TaggedTokenizerTest extends Specification {
    TaggedTokenizer tokenizer;

    def "English sentence with words, <ENAMEX tags and spaces"(){
        given:
        String sentence = 'the brown fox, <ENAMEX TYPE="PERSON">Charly</ENAMEX>, eats rats.'

        when:
        List<String> actual = tokenize(sentence)
        def expected = ['the', 'brown', 'fox', ',', 'Charly', ',', 'eats', 'rats', '.']

        then:
        expected == actual
    }

    def "English sentence with words, punctuation and spaces"(){
        given:
        String sentence = 'the (brown), fox!'

        when:
        List<String> actual = tokenize(sentence)
        def expected = ['the', '(', 'brown', ')', ',', 'fox','!']

        then:
        expected == actual
    }

    def "English sentence with only words and spaces"(){
        given:
        String sentence = 'the brown fox'

        when:
        List<String> actual = tokenize(sentence)
        def expected = ['the', 'brown', 'fox']

        then:
        expected == actual
    }

    def "English sentence with words, numbers and spaces"(){
        given:
        String sentence = 'the 11 foxes'

        when:
        List<String> actual = tokenize(sentence)
        def expected = ['the', '11', 'foxes']

        then:
        expected == actual
    }

    private List<String> tokenize(String text){
        List<String> tokens = new ArrayList<String>();
        tokenizer = new TaggedTokenizer(Version.LUCENE_36, new StringReader(text))
        TermAttribute termAtt = tokenizer.getAttribute(TermAttribute.class);
        while(tokenizer.incrementToken()){
            tokens.add(termAtt.term());
        }
        return tokens
    }
}