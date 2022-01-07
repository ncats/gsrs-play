package ix.ginas.models.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import ix.core.models.Group;
import ix.core.models.Keyword;
import ix.core.util.EntityUtils;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for working with inverted relationships.
 *
 * Created by katzelda on 4/17/18.
 */
public final class RelationshipUtil {
    /*
    The actual reason for this class is these methods used to be inside Relationship itself
    but when we had to add more properties to the inverted relationship which
    involved cloning it really messed up ebean and we got JVM verify errors when the classloader
    tried to load the ebean enhanced classes.

    The easiest solution was to factor out this code so ebean wouldn't touch it.

    Error is provided below for historical reference:

    Caused by: java.lang.VerifyError: Bad type on operand stack
Exception Details:
  Location:
    ix/ginas/models/v1/Relationship.fetchInverseRelationship()Lix/ginas/models/v1/Relationship; @285: invokevirtual
  Reason:
    Type 'ix/ginas/models/v1/Amount' (current frame, stack[0]) is not assignable to 'ix/ginas/models/v1/Relationship'
  Current Frame:
    bci: @285
    flags: { }
    locals: { 'ix/ginas/models/v1/Relationship', 'ix/ginas/models/v1/Relationship', '[Ljava/lang/String;', 'java/util/LinkedHashSet', 'com/fasterxml/jackson/databind/JsonNode', top, top, 'ix/ginas/models/v1/Relationship', 'ix/ginas/models/v1/Amount' }
    stack: { 'ix/ginas/models/v1/Amount', null }
  Bytecode:
    0x0000000: 2ab6 0096 9a00 31bb 00c7 59bb 0063 59b7
    0x0000010: 0064 12c9 b600 6c2a 3a07 013a 0819 07b6
    0x0000020: 005b 3a08 1908 b600 6c12 cbb6 006c b600
    0x0000030: 73b7 00cd bfbb 0002 59b7 00ce 4cb2 00b3
    0x0000040: 2a3a 0701 3a08 1907 b600 5b3a 0819 08b6
    0x0000050: 00b8 4d2b bb00 6359 b700 642c 0432 b600
    0x0000060: 6c12 1eb6 006c 2c03 32b6 006c b600 733a
    0x0000070: 083a 0719 0719 08b6 00d1 2b2a b600 d5b6
    0x0000080: 00d9 bb00 db59 b700 dc4e 2ab6 00df b900
    0x0000090: e501 003a 0419 04b9 00ea 0100 9900 2b19
    0x00000a0: 04b9 00ee 0100 c000 f03a 052d 1905 b800
    0x00000b0: f4b6 00f7 b900 fa02 0057 a700 0a3a 0619
    0x00000c0: 06b6 00fd a7ff d12a 3a07 013a 0819 07b6
    0x00000d0: 0101 3a08 1908 b800 f4b6 0105 3a04 b201
    0x00000e0: 0b19 04b6 010e b601 132b 2a3a 0701 3a08
    0x00000f0: 1907 b601 013a 0819 08b8 00f4 b600 f7c0
    0x0000100: 0115 3a08 3a07 1907 1908 b601 192b 3a07
    0x0000110: 013a 0819 07b6 0101 3a08 1908 01b6 011d
    0x0000120: a700 0a3a 0519 05b6 00fd 2bb0
  Exception Handler Table:
    bci [171, 186] => handler: 189
    bci [233, 288] => handler: 291
  Stackmap Table:
    same_frame(@53)
    full_frame(@149,{Object[#2],Object[#2],Object[#187],Object[#219],Object[#231],Top,Top,Object[#2],Object[#93]},{})
    full_frame(@189,{Object[#2],Object[#2],Object[#187],Object[#219],Object[#231],Object[#240],Top,Object[#2],Object[#93]},{Object[#197]})
    same_frame(@196)
    full_frame(@199,{Object[#2],Object[#2],Object[#187],Object[#219],Object[#231],Top,Top,Object[#2],Object[#93]},{})
    full_frame(@291,{Object[#2],Object[#2],Object[#187],Object[#219],Object[#269],Top,Top,Object[#2],Object[#277]},{Object[#197]})
    same_frame(@298)

        at java.lang.Class.forName0(Native Method) ~[na:1.8.0_121]
        at java.lang.Class.forName(Class.java:348) ~[na:1.8.0_121]
        at play.db.ebean.EbeanPlugin.onStart(EbeanPlugin.java:79) ~[play-java-ebean_2.11-2.3.10.jar:2.3.10]
        ... 23 common frames omitted



     */

    private static final String RELATIONSHIP_INV_CONST = "->";
    private static final Pattern RELATIONSHIP_SPLIT_REGEX = Pattern.compile(RELATIONSHIP_INV_CONST);

    public static  String getDisplayType(Relationship r){
        if(r.type.contains(RELATIONSHIP_INV_CONST)){
            String[] split = r.type.split(RELATIONSHIP_INV_CONST);
            return split[0] + " (" +split[1] +")";
        }
        return r.type;
    }

    public static  String standardizeType(Relationship r){
        if(r.type.contains(RELATIONSHIP_INV_CONST)){
            String[] split = r.type.split(RELATIONSHIP_INV_CONST);
            return split[0].trim() + "->" +split[1].trim();
        }
        return r.type.trim();
    }
    /**
     * Returns true if this relationship is invertible
     * @return
     */
    public static boolean isAutomaticInvertible(Relationship r){
        if(r.type==null){
            return false;
        }
        //Explicitly ignore alternative relationships
        if(r.type.equals(Substance.ALTERNATE_SUBSTANCE_REL) || r.type.equals(Substance.PRIMARY_SUBSTANCE_REL)){
            return false;
        }

        //Test if the related substance is the same as its owner (self-referencing)
        if(r.relatedSubstance.isReferencingSubstance(r.fetchOwner())){
            return false;
        }

        String[] types=RELATIONSHIP_SPLIT_REGEX.split(r.type);


        return types.length>=2;
    }


    public static Relationship createInverseRelationshipFor(Relationship other){
        if(!other.isAutomaticInvertible()){
            throw new IllegalStateException("Relationship :" + other.type + " is not invertable");
        }
        Relationship r=new Relationship();
        r.type=reverseRelationship(other.type);
        Set<Group> groups = new LinkedHashSet<>();
        for(Group g : other.getAccess()){
            groups.add(g);
        }
        r.setAccess(groups);
        //new String so ebean sees it's a new object
        //just in case...
        if(other.comments !=null) {
            r.comments = new String(other.comments);
        }

        if(other.amount !=null) {
            try {
                r.amount = EntityUtils.EntityWrapper.of(other.amount).getClone();
                r.amount.uuid = null;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        //GSRS-684 copy over qualification and interactionType
        if(other.qualification !=null){
            //new String so ebean sees it's a new object
            //just in case...
            r.qualification = new String(other.qualification);
        }
        if(other.interactionType !=null){
            //new String so ebean sees it's a new object
            //just in case...
            r.interactionType = new String(other.interactionType);
        }

        //GSRS-982 mediator
        if(other.mediatorSubstance !=null){
            r.mediatorSubstance = other.mediatorSubstance.copyWithNullUUID();
        }
        return r;
    }
    
    public static String reverseRelationship(String type){
    	String[] types=RELATIONSHIP_SPLIT_REGEX.split(type);
        return types[1].trim() + RELATIONSHIP_INV_CONST + types[0].trim();
    }
}
