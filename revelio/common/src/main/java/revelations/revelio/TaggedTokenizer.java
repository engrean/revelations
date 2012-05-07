package revelations.revelio;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

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
    private static final Set<String> entityTypes = new HashSet<String>();
    static{
        entityTypes.add("<ENAMEX");
        entityTypes.add("<TIMEX");
        entityTypes.add("<NUMEX");
    }

    /**
     * Creates a new {@link TaggedTokenizer} instance
     *
     * @param matchVersion Lucene version to match See {@link <a href="#version">above</a>}
     * @param input        the input to split up into tokens
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
        return !UCharacter.isUWhiteSpace(c);
    }

    /**
     * Returns true if a codepoint should be considered a punctuation mark. This
     * tells the tokenizer to include this as a separate token.
     */
    protected boolean isPunctuationChar(int type) {
        return ((type >= UCharacterCategory.DASH_PUNCTUATION &&
                type <= UCharacterCategory.OTHER_PUNCTUATION
        ) ||
                (type >= UCharacterCategory.INITIAL_PUNCTUATION &&
                        type <= UCharacterCategory.FINAL_QUOTE_PUNCTUATION
                ));
    }

    @Override
    public final boolean incrementToken() throws IOException {
        //TODO: Need to make this shorter.
        clearAttributes();
        TokenMetaData meta = new TokenMetaData();
        meta.buffer = termAtt.buffer();
        meta.start = -1;  // this variable is always initialized
        meta.length = 0;
        meta.end = -1;
        while (true) {
            if (bufferIndex >= dataLen) {
                offset += dataLen;
                if (!charUtils.fill(ioBuffer, input)) { // read supplementary char aware with CharacterUtils
                    dataLen = 0; // so next offset += dataLen won't decrement offset
                    if (meta.length > 0) {
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
            final int nc = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex + 1);
            final char[] chars = UCharacter.toChars(c);
            final char[] nchars = UCharacter.toChars(nc);
            final int charCount = UCharacter.charCount(c);
            final int ncharCount = UCharacter.charCount(nc);
            bufferIndex += charCount;
            int type = UCharacter.getType(c);
//            System.out.println(new String(UCharacter.toChars(c)) + "=> " + UCharacterCategory.toString(type));

            //if math symbol, check for
            if (isPunctuationChar(type) && !meta.isEntityStart) {
                addChar(meta, meta.buffer, c, charCount);
                break;
            } else if (isTokenChar(c)) {               // if it's a token char
                addChar(meta, meta.buffer, c, charCount);
                if (meta.length >= MAX_WORD_LEN ||// buffer overflow! make sure to check for >= surrogate pair could break == test
                        (!meta.isEntityStart && isPunctuationChar(UCharacter.getType(nc))))
                    break;
                if (charCount == 1) {
                    if (chars[0] == '<') {//detect if it's a start tag or an end tag.
                        if (ncharCount == 1 && nchars[0] == '/') {//found </
                            meta.isPossibleEntityEnd = true;
                        } else if (ncharCount == 1) {
                            meta.isPossibleEntityStart = true;
                        }
                    }else if (meta.isPossibleEntityEnd && chars[0] == '>'){
                        meta.isEntityEnd = true;
                        meta.isEntityStart = false;
                        break;
                    }
                }
            } else if (meta.length > 0) {             // at non-Letter w/ chars
                if (meta.isPossibleEntityStart) {
                    String token = new String(ioBuffer.getBuffer(), meta.start, meta.length);
                    if (meta.isEntityEnd) {
                        if ('>' == meta.buffer[meta.length]) {
                            break;
                        }
                    } else if (!meta.isEntityStart && entityTypes.contains(token)) {
                        meta.isEntityStart = true;
                    }

                    if (meta.isEntityStart){
                        addChar(meta, meta.buffer, c, charCount);
                    }
                } else {
                    break;
                }
            }
        }
        termAtt.setLength(meta.length);
        assert meta.start != -1;
        offsetAtt.setOffset(correctOffset(meta.start), finalOffset = correctOffset(meta.end));
        return true;
    }

    protected void addChar(TokenMetaData meta, char[] buffer, int c, int charCount) {
        if (meta.length == 0) {                // start of token
            assert meta.start == -1;
            meta.start = offset + bufferIndex - charCount;
            meta.end = meta.start;
        } else if (meta.length >= buffer.length - 1) { // check if a supplementary could run out of bounds
            buffer = termAtt.resizeBuffer(2 + meta.length); // make sure a supplementary fits in the buffer
        }
        meta.end += charCount;
        meta.length += UCharacter.toChars(c, buffer, meta.length); // buffer it, normalized
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

    private class TokenMetaData {
        private int length;
        private int start = -1;
        private int end = -1;
        private char[] buffer;
        private boolean isEntityStart;
        private boolean isPossibleEntityStart;
        private boolean isEntityEnd;
        private boolean isPossibleEntityEnd;
    }

}