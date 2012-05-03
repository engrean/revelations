package revelations.revelio;

import java.io.IOException;
import java.io.Reader;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharacterUtils;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.CharacterUtils.CharacterBuffer;

/**
 * @author hargravescw
 *         Date: 5/3/12
 */
public final class TaggedTokenizer extends Tokenizer {

    private int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;
    private static final int MAX_WORD_LEN = 255;
    private static final int IO_BUFFER_SIZE = 4096;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final CharacterUtils charUtils;
    private final CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);


    /**
     * Creates a new {@link TaggedTokenizer} instance
     *
     * @param matchVersion
     *          Lucene version to match See {@link <a href="#version">above</a>}
     * @param input
     *          the input to split up into tokens
     */
    public TaggedTokenizer(Version matchVersion, Reader input) {
        super(input);
        charUtils = CharacterUtils.getInstance(matchVersion);

    }

    /**
     * Returns true if a codepoint should be included in a token. This tokenizer
     * generates as tokens adjacent sequences of codepoints which satisfy this
     * predicate. Codepoints for which this is false are used to define token
     * boundaries and are not included in tokens.
     */
    protected boolean isTokenChar(int c) {
        return !UCharacter.isWhitespace(c);
    }

    /**
     * Returns true if a codepoint should be considered a punctuation mark. This
     * tells the tokenizer to include this as a separate token.
     */
    protected boolean isPunctuationChar(int type) {
        return ( (type >= UCharacterCategory.DASH_PUNCTUATION &&
              type <= UCharacterCategory.OTHER_PUNCTUATION
             ) ||
             (type >= UCharacterCategory.INITIAL_PUNCTUATION &&
              type <= UCharacterCategory.FINAL_QUOTE_PUNCTUATION
                ));
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        int length = 0;
        int start = -1; // this variable is always initialized
        int end = -1;
        char[] buffer = termAtt.buffer();
        while (true) {
            if (bufferIndex >= dataLen) {
                offset += dataLen;
                if(!charUtils.fill(ioBuffer, input)) { // read supplementary char aware with CharacterUtils
                    dataLen = 0; // so next offset += dataLen won't decrement offset
                    if (length > 0) {
                        break;
                    } else {
                        finalOffset = correctOffset(offset);
                        return false;
                    }
                }
                dataLen = ioBuffer.getLength();
                bufferIndex = 0;
            }
            // use CharacterUtils here to support < 3.1 UTF-16 code unit behavior if the char based methods are gone
            final int c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex);
            final int nc = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex+1);
            final int charCount = Character.charCount(c);
            bufferIndex += charCount;
            int type = UCharacter.getType(c);
//            System.out.println(new String(UCharacter.toChars(c)) + "=> " + UCharacterCategory.toString(type));

            if (isPunctuationChar(type)){
                if (length == 0) {                // start of token
                    assert start == -1;
                    start = offset + bufferIndex - charCount;
                    end = start;
                } else if (length >= buffer.length-1) { // check if a supplementary could run out of bounds
                    buffer = termAtt.resizeBuffer(2+length); // make sure a supplementary fits in the buffer
                }
                end += charCount;
                length += UCharacter.toChars(c, buffer, length); // buffer it, normalized
                break;
            }else if (isTokenChar(c)) {               // if it's a token char
                if (length == 0) {                // start of token
                    assert start == -1;
                    start = offset + bufferIndex - charCount;
                    end = start;
                } else if (length >= buffer.length-1) { // check if a supplementary could run out of bounds
                    buffer = termAtt.resizeBuffer(2+length); // make sure a supplementary fits in the buffer
                }
                end += charCount;
                length += UCharacter.toChars(c, buffer, length); // buffer it, normalized
                if (length >= MAX_WORD_LEN || isPunctuationChar(UCharacter.getType(nc))) // buffer overflow! make sure to check for >= surrogate pair could break == test
                    break;
            } else if (length > 0){             // at non-Letter w/ chars
                break;                           // return 'em
            }
        }

        termAtt.setLength(length);
        assert start != -1;
        offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
        return true;

    }

    @Override
    public final void end() {
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset(Reader input) throws IOException {
        super.reset(input);
        bufferIndex = 0;
        offset = 0;
        dataLen = 0;
        finalOffset = 0;
        ioBuffer.reset(); // make sure to reset the IO buffer!!
    }
}