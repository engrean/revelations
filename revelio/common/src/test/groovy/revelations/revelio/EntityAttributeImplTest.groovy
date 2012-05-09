package revelations.revelio;

import spock.lang.Specification
import static revelations.revelio.BilouTags.*

/**
 * @author Christian Hargraves
 * Date: 5/9/12
 */
public class EntityAttributeImplTest extends Specification {

    EntityAttributeImpl attr = new EntityAttributeImpl();

    def cleanup (){
        attr.clear();
    }

    def "equals returns true when two objects are the same"(){
        given:
        EntityAttribute attr2 = new EntityAttributeImpl();
        attr.setEntityBegin()
        attr.setIsCapitalized(true)
        attr.setIsPunctuationMark(true)
        attr.setEntityType('joe')
        attr2.setEntityBegin()
        attr2.setIsCapitalized(true)
        attr2.setIsPunctuationMark(true)
        attr2.setEntityType('joe')

        when:
        boolean equals = attr.equals(attr2)

        then:
        !equals
    }

    def "equals returns false when two objects are different"(){
        given:
        EntityAttribute attr2 = new EntityAttributeImpl();
        attr2.setEntityBegin()
        attr2.setIsCapitalized(true)
        attr2.setIsPunctuationMark(true)
        attr2.setEntityType('joe')

        when:
        boolean equals = attr.equals(attr2)

        then:
        !equals
    }

    def "when toEntityTag is called with begin span type and an entity type of PER"(){
        given:
        attr.setEntityBegin()
        attr.setEntityType("PER")

        when:
        String tag = attr.toEntityTag();

        then:
        tag == "B-PER"
    }

    def "when toEntityTag is called without an entity type defined"(){
        expect:
        attr.toEntityTag() == BilouTags.OUTSIDE
    }


    def "copyTo from attr with non-default values to attr with default values"(){
        given:
        EntityAttribute attrToClone = new EntityAttributeImpl();
        attr.setEntityBegin()
        attr.setIsCapitalized(true)
        attr.setIsPunctuationMark(true)
        attr.setEntityType('joe')

        when:
        attr.copyTo(attrToClone)

        then:
        attrToClone.isEntity()
        attrToClone.isCapitalized()
        attrToClone.isPunctuationMark()
        attrToClone.getEntityType() == 'joe'
    }

    def "copyTo from attr with default values and nulls to attr which is fully filled out"(){
        given:
        EntityAttribute attrToClone = new EntityAttributeImpl();
        attrToClone.setEntityBegin()
        attrToClone.setIsCapitalized(true)
        attrToClone.setIsPunctuationMark(true)
        attrToClone.setEntityType('joe')

        when:
        attr.copyTo(attrToClone)

        then:
        !attrToClone.isEntity()
        !attrToClone.isCapitalized()
        !attrToClone.isPunctuationMark()
        attrToClone.getEntityType() == null
    }

    def "clear cleans things up"(){
        given:
        attr.setEntityBegin()
        attr.setIsCapitalized(true)
        attr.setIsPunctuationMark(true)
        attr.setEntityType('joe')

        when:
        attr.clear()

        then:
        !attr.isEntity()
        !attr.isCapitalized()
        !attr.isPunctuationMark()
        attr.getEntityType() == null
    }

    def "default of getEntityType is null"(){
        expect:
        attr.getEntityType() == null
    }

    def "setEntityType with null"(){
        when:
        attr.setEntityType(null)

        then:
        attr.getEntityType() == null
    }

    def "setEntityType with any old string"(){
        when:
        attr.setEntityType('any old string')

        then:
        attr.getEntityType() == 'any old string'
    }

    def "setEntitySpanType"(){
        when:
        attr.setEntitySpanType('j')

        then:
        attr.getEntitySpanType() == 'j'
    }

    def "setEntityUnit"(){
        when:
        attr.setEntityUnit()

        then:
        attr.getEntitySpanType() == EntityAttributeImpl.UNIT
    }

    def "setEntityOutside"(){
        when:
        attr.setEntityBegin()
        attr.setEntityOutside()

        then:
        attr.getEntitySpanType() == EntityAttributeImpl.OUTSIDE
    }

    def "setEntityLast"(){
        when:
        attr.setEntityLast()

        then:
        attr.getEntitySpanType() == EntityAttributeImpl.LAST
    }

    def "setEntityInside"(){
        when:
        attr.setEntityInside()

        then:
        attr.getEntitySpanType() == EntityAttributeImpl.INSIDE
    }

    def "setEntityBegin"(){
        when:
        attr.setEntityBegin()

        then:
        attr.getEntitySpanType() == EntityAttributeImpl.BEGIN
    }

    def "default value of entitySpanType is Other"(){
        expect:
        attr.getEntitySpanType() == EntityAttributeImpl.OUTSIDE
    }

    def "isEntity returns true when anything besides other is set"(){
        when:
        attr.setEntityBegin();

        then:
        attr.isEntity()
    }

    def "default is that isEntity returns false"(){
        expect:
        !attr.isEntity()
    }

    def "isEntity should return false if entity type is set to null"(){
        when:
        attr.setEntityType(null);

        then:
        !attr.isEntity()
    }

    def "isEntity should return false if nothing is set"(){
        expect:
        !attr.isEntity()
    }

    def "isPunctuationMark should return false when set to false"(){
        expect:
        !attr.isPunctuationMark()
    }

    def "isPunctuationMark should return true when set to true"(){
        when:
        attr.setIsPunctuationMark(true)
        then:
        attr.isPunctuationMark()
    }

    def "isCapitalized should return true when set to true"(){
        when:
        attr.setIsCapitalized(true)
        then:
        attr.isCapitalized()
    }

    def "isCapitalized should return false when set to false"(){
        expect:
        !attr.isCapitalized()
    }
}