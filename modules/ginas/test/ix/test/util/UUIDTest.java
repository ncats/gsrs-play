package ix.test.util;

import ix.utils.UUIDUtil;
import ix.utils.Util;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by katzelda on 9/7/16.
 */
public class UUIDTest {


    @Test
    public void isUUID(){
        List<String> ids = Arrays.asList(
                "98bd1ab8-8469-910a-6686-5c178039dd5a",
                "c452c457-9f21-a335-7e4d-9c212bfe807f",
                "c452c457-9f21-a335-7e4d-9c212bfe807f",
                "0a7ef659-dddb-213a-08aa-1b04c686c0ec"
        );

        for(String id : ids){
            assertTrue(id, UUIDUtil.isUUID(id));
        }
    }

    @Test
    public void isNOTUUID(){
        List<String> ids = Arrays.asList(
                "CC(C)(N)Cc1ccccc1",
                "COC(=O)[C@H]1[C@@H]2CC[C@H](C[C@@H]1OC(=O)c3ccccc3)N2C",
                "c452c4579f21a3357e4d9c212bfe807f",
                "foo"
        );

        for(String id : ids){
            assertFalse(id, UUIDUtil.isUUID(id));
        }
    }
}
