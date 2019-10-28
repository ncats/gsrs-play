package ix.ginas.Prototype;

import gov.nih.ncats.molwitch.Chemical;
import ix.core.chem.PolymerDecode;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
public class PolymerDecodeTest {

    @Test
    public void sruWithAstrickShouldBeCountedAsGarbageNotEndGroup() throws IOException {
        String mol = "\n" +
                "   JSDraw210231917082D\n" +
                "\n" +
                "  3  2  0  0  0  0            999 V2000\n" +
                "   13.1040   -7.1702    0.0000 *   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "   14.4550   -6.3902    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "   15.8060   -7.1702    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                "  1  2  1  0  0  0  0\n" +
                "  2  3  1  0  0  0  0\n" +
                "M  STY  1   1 SRU\n" +
                "M  SAL   1  1   2\n" +
                "M  SBL   1  2   1   2\n" +
                "M  SMT   1 A\n" +
                "M  SDI   1  4   14.0920   -7.6382   14.0920   -4.7262\n" +
                "M  SDI   1  4   14.8720   -4.7262   14.8720   -7.6382\n" +
                "M  END";

        Set<PolymerDecode.StructuralUnit> srus = PolymerDecode.DecomposePolymerSU(Chemical.parse(mol));
        assertEquals(3, srus.size());
        Map<String, List<PolymerDecode.StructuralUnit>> map = srus.stream().collect(Collectors.groupingBy(s-> s.type));

        assertEquals(3, map.size());
        assertEquals(1, map.get("SRU-BLOCK").size());
        assertEquals(1, map.get("GARBAGE").size());
        assertEquals(1, map.get("END_GROUP").size());

        assertEquals(Arrays.asList(1), map.get("SRU-BLOCK").get(0).amap);
        assertEquals(Arrays.asList(0), map.get("GARBAGE").get(0).amap);
        assertEquals(Arrays.asList(2), map.get("END_GROUP").get(0).amap);

//        System.out.println("number of strucrual units = " + srus.size());
//        for(PolymerDecode.StructuralUnit unit : srus){
//            System.out.println(unit.amap);
//            System.out.println(unit.type);
//            System.out.println(unit.structure);
//        }
    }


}
