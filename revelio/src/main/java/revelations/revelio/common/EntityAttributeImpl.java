package revelations.revelio.common;

import org.apache.lucene.util.AttributeImpl;


/**
 * Keeps track of entity metadata about each token in text which is to be used in an NER system.
 * It is currently limited to:
 * <ul>
 *     <li>Capitalization or lowercase</li>
 *     <li>If the token is a punctuation mark</li>
 *     <li>The entityType of entity which is free form. An example entity entityType might be PERSON, LOCATION, etc ...</li>
 *     <li>The prefix of the entity entityType if it is desired to span multiple tokens</li>
 * </ul>
 *
 * This implementation uses BILOU for its prefix entityType:
 * <ul>
 *     <li>B stands for BEGIN entity</li>
 *     <li>I stands for INSIDE entity</li>
 *     <li>L stands for LAST entity</li>
 *     <li>O stands for OTHER or not an entity</li>
 *     <li>U stands for UNIT level entity</li>
 * </ul>
 * <p>
 * For example with the given string "I would love to eat breakfast with Avram Noam Chomsky in Montana.":<br/>
 * Avram would be the BEGINning of an entity, Noam would be the INSIDE of that entity and Chomsky would be
 * the LAST of that entity. Montana would be  UNIT level entity, meaning there is only one token that is an entity.
 * All of the other tokens would be OTHER entity types.</p>
 * Thus the first entity might be marked up as follows:<br/>
 * Avram B-PERSON<br/>
 * Noam I-PERSON<br/>
 * Chomsky L-PERSON<br/>
 * <br/>
 * And the other entity would be:<br/>
 * Montana U-LOCALE<br/>
 * Or whatever you want to call a location.
 *
 * Capitalization and punctuation are simply used as features.
 *
 * @author Christian Hargraves
 *         Date: 5/9/12
 */
public class EntityAttributeImpl extends AttributeImpl implements EntityAttribute, BilouTags{
    private boolean isCapitalized;
    private boolean isPunctuation;
    private String entitySpanType = OUTSIDE;
    private String entityType;

    @Override
    public String toEntityTag() {
        StringBuilder tag = new StringBuilder(entitySpanType);
        if (entityType != null){
            tag.append("-").append(entityType);
        }
        return tag.toString();
    }

    @Override
    public boolean isEntity() {
        return !entitySpanType.equals(OUTSIDE);
    }

    @Override
    public void setIsCapitalized(boolean capitalized) {
        isCapitalized = capitalized;
    }

    @Override
    public boolean isCapitalized() {
        return isCapitalized;
    }

    @Override
    public void setIsPunctuationMark(boolean isPunctuationMark) {
        this.isPunctuation = isPunctuationMark;
    }

    @Override
    public boolean isPunctuationMark() {
        return isPunctuation;
    }

    @Override
    public void setEntityType(String type) {
        entityType = type;
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

    @Override
    public void setEntityBegin() {
        entitySpanType = BEGIN;
    }

    @Override
    public void setEntityInside() {
        entitySpanType = INSIDE;
    }

    @Override
    public void setEntityLast() {
        entitySpanType = LAST;
    }

    @Override
    public void setEntityOutside() {
        entitySpanType = OUTSIDE;
    }

    @Override
    public void setEntityUnit() {
        entitySpanType = UNIT;
    }

    @Override
    public String getEntitySpanType() {
        return entitySpanType;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear() {
        setIsCapitalized(false);
        setIsPunctuationMark(false);
        setEntityOutside();
        setEntityType(null  );
    }

    @Override
    public void setEntitySpanType(String spanType){
        this.entitySpanType = spanType;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        EntityAttribute attr = (EntityAttribute) target;
        attr.setEntitySpanType(entitySpanType);
        attr.setEntityType(entityType);
        attr.setIsCapitalized(isCapitalized);
        attr.setIsPunctuationMark(isPunctuation);
    }

    @Override
    public int hashCode(){
        int capitalHash = (isCapitalized) ? 31 : 37;
        int punctuationHash = (isPunctuation) ? 41: 43;
        return toEntityTag().hashCode() + capitalHash + punctuationHash;
    }

    @Override
    public String toString(){
        return toEntityTag();
    }
}
