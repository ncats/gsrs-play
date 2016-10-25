package ix.ginas.secondarymodels;

import play.db.ebean.Model;
import javax.persistence.*;

/**
 * Created by mandavag on 10/11/16.
 */

@Entity
@Table(name="ix_ginas_testtable")
@Inheritance

public class TestTable extends Model{

    public  int id;

    public String name;

    public TestTable(){
        id = 123;
        name= "lets test";
    }


}
