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
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
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
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    private final CharacterUtils charUtils;
    private final CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);
    private static final Set<String> entityTypes = new HashSet<String>();
    public static final String ENTITY_TYPE = "entity";
    public static final String PUNCTUATION_TYPE = "punctuation";

    static {
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
                type <= UCharacterCategory.OTHER_PUNCTUATION)
                ||
                (type >= UCharacterCategory.INITIAL_PUNCTUATION &&
                        type <= UCharacterCategory.FINAL_QUOTE_PUNCTUATION
                ));
    }

    @Override
    public final boolean incrementToken() throws IOException {
        //TODO: Need to make this shorter.
        clearAttributes();
        final TokenMetaData meta = new TokenMetaData(termAtt.buffer(), -1, 0, -1);
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
            final TokenHelper tokenHelper = new TokenHelper(bufferIndex);
            bufferIndex += tokenHelper.charCount;

            if (isPunctuationChar(tokenHelper.currentType)) {
                addChar(meta, meta.buffer, tokenHelper.c, tokenHelper.charCount);
                typeAttribute.setType(PUNCTUATION_TYPE);
                break;
            } else if (isTokenChar(tokenHelper.c)) {               // if it's a token char
                addChar(meta, meta.buffer, tokenHelper.c, tokenHelper.charCount);
                if (meta.length >= MAX_WORD_LEN || // buffer overflow! make sure to check for >= surrogate pair could break == test
                        ((isPunctuationChar(UCharacter.getType(tokenHelper.nc)) || UCharacter.getType(tokenHelper.nc) == UCharacterCategory.MATH_SYMBOL)))
                    break;
//                if (tokenHelper.charCount == 1) {
                //Loop until you you know for sure it's an entity tag.
                if (tokenHelper.chars[0] == '<') {//detect if it's a start tag or an end tag.
                    lookForTag(meta, tokenHelper);
                    break;
                }else if (tokenHelper.currentType == UCharacterCategory.MATH_SYMBOL){
                    typeAttribute.setType(PUNCTUATION_TYPE);
                    break;
                }
            } else if (meta.length > 0) { // at non-Letter, non-punctuation, but possibly math symbol [<,=,>] w/ chars
                break;
            }
        }
        termAtt.setLength(meta.length);

        assert meta.start != -1;
        offsetAtt.setOffset(correctOffset(meta.start), finalOffset = correctOffset(meta.end));
        return true;
    }

    private void lookForTag(TokenMetaData meta, TokenHelper tokenHelper) {
        //TODO: We shouldn't need a token. This is just me being lazy...
        StringBuilder token = new StringBuilder();
        token.appendCodePoint(tokenHelper.c);
        int index = bufferIndex;
        final TokenMetaData subMeta = new TokenMetaData(termAtt.buffer(), meta.start, meta.length, meta.end);
        while (true) {
            final TokenHelper subTokenHelper = new TokenHelper(index++);
            addChar(subMeta, meta.buffer, subTokenHelper.c, subTokenHelper.charCount);
            if (!meta.isPossibleEntityEnd && meta.isEntityStart &&
                    subTokenHelper.chars[0] == '<' && subTokenHelper.nchars[0] == '/') {
                token.appendCodePoint(subTokenHelper.c);
                meta.isPossibleEntityEnd = true;
                meta.isEntityStart = false;
            } else if (meta.isPossibleEntityEnd && isTokenChar(subTokenHelper.c) && subTokenHelper.c == '>') {
                token.appendCodePoint(subTokenHelper.c);
                meta.isEntityEnd = true;
                meta.isPossibleEntityEnd = false;
                break;
            } else if (!meta.isEntityStart && !isTokenChar(subTokenHelper.c)) { //we get one try at this.
                String tagName = token.toString();
                if (entityTypes.contains(tagName)) {
                    meta.isEntityStart = true;
                    token.appendCodePoint(subTokenHelper.c);
                } else {
                    break;
                }
            }else{
                token.appendCodePoint(subTokenHelper.c);
            }

        }
        if (meta.isEntityEnd) {
            meta.buffer = subMeta.buffer;
            meta.end = subMeta.end;
            meta.length = subMeta.length;
            bufferIndex += subMeta.length - 1;
            typeAttribute.setType(ENTITY_TYPE);
        }else{
            typeAttribute.setType(PUNCTUATION_TYPE);
        }
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
        private boolean isEntityEnd;
        private boolean isPossibleEntityEnd;

        TokenMetaData(char[] b, int start, int length, int end) {
            this.buffer = b;
            this.start = start;
            this.end = end;
            this.length = length;
        }
    }

    private class TokenHelper {
        final int c;
        final int nc;
        final char[] chars;
        final char[] nchars;
        final int charCount;
        final int ncharCount;
        final int currentType;
        final int nextType;

        public TokenHelper(int bufferIndex) {
            c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex);
            nc = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex + 1);
            chars = UCharacter.toChars(c);
            nchars = UCharacter.toChars(nc);
            charCount = UCharacter.charCount(c);
            ncharCount = UCharacter.charCount(nc);
            currentType = UCharacter.getType(c);
            nextType = UCharacter.getType(nc);
        }

    }

}