package ix.core.models;

import ix.core.util.LogUtil;
import ix.utils.Util;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by katzelda on 2/7/19.
 */
public class DefinitionalElements {
    private final List<DefinitionalElement> elements = new ArrayList<>();

    private Map<String, List<DefinitionalElement>> elementMap = new HashMap<>();
    public DefinitionalElements(List<DefinitionalElement> elements){
        for(DefinitionalElement e : elements){
            if(e !=null){
                this.elements.add(e);

                elementMap.computeIfAbsent(e.getKey(), new Function<String, List<DefinitionalElement>>(){
                    public List<DefinitionalElement> apply(String k){
                        return new ArrayList<>();
                    }
                }).add(e);
            }
        }
    }

    public List<DefinitionalElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public byte[] getDefinitionalHash(){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for(DefinitionalElement e: elements){
                digest.update(e.getDefinitionalString().getBytes(Charset.defaultCharset()));
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            //this shouldn't happen...
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "DefinitionalElements{" +
                "elements=" + elements +
                '}';
    }

    public List<DefinitionalElementDiff> diff(DefinitionalElements other) {

        LogUtil.trace(new Supplier<String>() {
            @Override
            public String get() {
                return String.format("starting in DefinitionalElements.diff. total in current %d; total in other %d ",
                        elementMap.size(), other.elementMap.size());
            }
        });

        List<DefinitionalElementDiff> diffs = new ArrayList<>();
        HashSet<String> newKeys = new HashSet<>(elementMap.keySet());
        newKeys.removeAll(other.elementMap.keySet());
        if(!newKeys.isEmpty()){
            for(String k : newKeys) {
                play.Logger.trace("looking at new key " + k);
                for(DefinitionalElement newValue : elementMap.get(k)) {
                    diffs.add(new DefinitionalElementDiff(DefinitionalElementDiff.OP.ADD, null, newValue));
                }
            }
        } else {
            play.Logger.trace("newKeys is empty");
        }
        HashSet<String> oldKeys = new HashSet<>(other.elementMap.keySet());
        oldKeys.removeAll(elementMap.keySet());
        if(!oldKeys.isEmpty()){
            for(String k : oldKeys) {
                LogUtil.trace(new Supplier<String>(){
                public String get() {
                    return "looking at old key " + k;
                }
            });
                List<DefinitionalElement> definitionalElements = other.elementMap.get(k);
                if(definitionalElements ==null){
                    continue;
                }
                for(DefinitionalElement oldValue : definitionalElements) {
                    diffs.add(new DefinitionalElementDiff(DefinitionalElementDiff.OP.REMOVED,oldValue, null));
                }
            }
        } else {
            play.Logger.trace("oldKeys is empty");
        }

        //ok we got the easy ones out of the way
        //now to check if the things with the same keys are changed
        HashSet<String> sameKeys = new HashSet<>(elementMap.keySet());
        sameKeys.retainAll(other.elementMap.keySet());
        for(String k : sameKeys){
            List<DefinitionalElement> currentElements = elementMap.get(k);
            List<DefinitionalElement> oldElements = other.elementMap.get(k);
            if(currentElements.size()==1 && oldElements.size()==1){
                if(!currentElements.get(0).getDefinitionalString().equals(oldElements.get(0).getDefinitionalString())){
                    diffs.add(new DefinitionalElementDiff(DefinitionalElementDiff.OP.CHANGED,oldElements.get(0), currentElements.get(0)));

                }
            }else {
                for(DefinitionalElement element : currentElements) {
                    String definitionalString = element.getDefinitionalString();

                    boolean hasMatch =false;

                    for(DefinitionalElement oe : oldElements){
                        if(oe.getDefinitionalString().equals(definitionalString)){
                            hasMatch=true;
                            break;
                        }
                    }

                    if( !hasMatch){
                        diffs.add(new DefinitionalElementDiff(DefinitionalElementDiff.OP.ADD, null, element));
                    }
                }

                for(DefinitionalElement oldElement : oldElements ) {
                    String definitionalString = oldElement.getDefinitionalString();

                    boolean hasMatch =false;
                    for(DefinitionalElement ce : currentElements){
                        if(definitionalString.equals(ce.getDefinitionalString())){
                            hasMatch=true;
                            break;
                        }
                    }

                    if(!hasMatch) {
                        diffs.add(new DefinitionalElementDiff(DefinitionalElementDiff.OP.REMOVED, oldElement, null));
                    }
            }
        }
        }
        return diffs;

    }

    public List<String> getDefinitionalHashLayers(){
        //sort key+values so that order of addition wouldn't matter
        List<DefinitionalElement> elms = new ArrayList<>(elements);
        Collections.sort(elms);
        List<String> layers = new ArrayList<>();


        int MAX_LAYERS= 2;
        for(int i=1;i<=MAX_LAYERS;i++){
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                for(DefinitionalElement e: elms){
                    if(e.getLayer()<=i){
                        digest.update(e.getDefinitionalString().getBytes(Charset.defaultCharset()));
                    }
                }
                layers.add(encodeString(digest.digest()));
            } catch (NoSuchAlgorithmException e) {
//                System.out.println("NoSuchAlgorithmException: " + e.getMessage());
                //this shouldn't happen...
                throw new IllegalStateException(e);
            }
        }
        return layers;
    }

    public static String encodeString(byte[] value) {
        return Util.toHex(value);
    }

    public static class DefinitionalElementDiff{
        public enum OP{
            ADD,
            REMOVED,
            CHANGED
        }

        private OP op;
        private DefinitionalElement oldValue, newValue;

        public DefinitionalElementDiff(OP op, DefinitionalElement oldValue, DefinitionalElement newValue) {
            this.op = op;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public OP getOp() {
            return op;
        }

        public DefinitionalElement getOldValue() {
            return oldValue;
        }

        public DefinitionalElement getNewValue() {
            return newValue;
        }

        @Override
        public String toString() {
            switch(op){
                case CHANGED:
                    return "Changed " + oldValue + " became " + newValue;
                case ADD:
                    return "Added " + newValue;
                default: return "removed " + oldValue;
            }

        }
    }
}
