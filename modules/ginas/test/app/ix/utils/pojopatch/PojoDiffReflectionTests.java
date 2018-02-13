package app.ix.utils.pojopatch;

import ix.ginas.models.utils.JSONEntity;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import org.junit.Test;
import static org.junit.Assert.*;
public class PojoDiffReflectionTests {
    @Test
    public void getterFindsPrivateBooleanFieldCorrectly() throws Exception{
        BooleanFieldBug a = new BooleanFieldBug();

        a.setDefining(true);


        BooleanFieldBug b = new BooleanFieldBug();

        b.setDefining(false);

        PojoPatch patch =  PojoDiff.getDiff(a,b);

        System.out.println(patch.getChanges());

        patch.apply(a);

        assertFalse(a.isDefining());

    }

    public static class BooleanFieldBug{
        @JSONEntity(title = "Defining")
        private Boolean defining;

        public Boolean isDefining() {
            return defining;
        }

        public void setDefining(Boolean defining) {
            this.defining = defining;
        }
    }
}
