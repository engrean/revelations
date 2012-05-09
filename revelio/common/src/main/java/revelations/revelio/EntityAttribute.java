package revelations.revelio;

import org.apache.lucene.util.Attribute;

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
 * An example of the prefix of the entity entityType might be using the BILOU format where:
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
public interface EntityAttribute extends Attribute {

    /**
     * Should combine the entity span entityType with the entity entityType or just the span entityType if the entity entityType is not set
     * @return a string representing both the span entityType and the entity entityType
     */
    String toEntityTag();

    /**
     * Returns true if the entity is an entity of interest such as PERSON, LOCALE, ORG, etc ...
     * @return false if the entity is not of interest.
     */
    boolean isEntity();

    /**
     * Sets the token as being capitalized.
     * @param capitalized set to true to mark the token as being capitalized. The default should be false.
     */
    void setIsCapitalized(boolean capitalized);

    /**
     * Returns true if the token is capitalized.
     * @return true if the token is capitalized.
     */
    boolean isCapitalized();

    /**
     * Sets the token as being a punctuation mark.
     */
    void setIsPunctuationMark(boolean punctuationMark);

    /**
     * Returns true if the token is a punctuation mark of some sort
     * @return true if the token is a punctuation mark of some sort
     */
    boolean isPunctuationMark();

    /**
     * Sets the entity entityType of the token. BILOU tags do not belong here.
     * @param type The entityType of the entity. Some examples might be PERSON, LOCALE, ORGANIZATION
     */
    void setEntityType(String type);

    /**
     * Gets the entity entityType of the token.
     * @return  The entityType of the entity.
     */
    String getEntityType();

    /**
     * Sets the token as a begin token of the entity span
     */
    void setEntityBegin();

    /**
     * Sets the token as an inside token of the entity span
     */
    void setEntityInside();

    /**
     * Sets the token as a last token of the entity span
     */
    void setEntityLast();

    /**
     * Sets the token as an outside entity token
     */
    void setEntityOutside();

    /**
     * Sets the token as an unit token of the entity span
     */
    void setEntityUnit();

    /**
     * Sets the entity span entityType if it is desired to use something outside of BILOU. This method is also needed
     * for the copyTo method in AttributeImpl class.
     * @param spanType the entityType of the span for multi-token entity support
     */
    void setEntitySpanType(String spanType);

    /**
     * Gets the prefix of the entity entityType for entities which span multiple tokens.
     * @return The prefix of the entity entityType
     */
    String getEntitySpanType();
}
