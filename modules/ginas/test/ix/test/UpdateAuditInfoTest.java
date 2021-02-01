package ix.test;

import ix.AbstractGinasServerTest;
import ix.AbstractGinasTest;
import ix.core.util.RunOnly;
import ix.core.util.TimeTraveller;
import ix.core.util.TimeUtil;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class UpdateAuditInfoTest extends AbstractGinasServerTest{


    @Rule
    public TimeTraveller timeTraveller = new TimeTraveller();

    @Test
    public void initialSaveSavesLastEditedAndUserInfo(){

        LocalDate localDate = LocalDate.of(1955, 11, 12);
        Date date =TimeUtil.toDate(localDate);
        timeTraveller.travelTo(localDate);

        Substance substance = new SubstanceBuilder()
                                    .addName("name1")
                                    .generateNewUUID()
                                    .build();
        GinasTestServer.User admin = ts.getAdmin();

        try(RestSession session =ts.newRestSession(admin)){
            SubstanceAPI api = session.newSubstanceAPI();
            api.submitSubstance(substance);

            Substance fetched = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());

            assertEquals(admin.getUserName(), fetched.createdBy.username);
            assertEquals(admin.getUserName(), fetched.lastEditedBy.username);
            assertEquals(date, fetched.lastEdited);
            assertEquals(date, fetched.created);


            Name name = fetched.names.get(0);
            assertEquals(admin.getUserName(), name.createdBy.username);
            assertEquals(admin.getUserName(), name.lastEditedBy.username);
            assertEquals(date, name.lastEdited);
            assertEquals(date, name.created);

        }

    }

    @Test
    public void initialSaveSavesLastEditedAndUserInfo2Names(){

        LocalDate localDate = LocalDate.of(1955, 11, 12);
        Date date =TimeUtil.toDate(localDate);
        timeTraveller.travelTo(localDate);

        Substance substance = new SubstanceBuilder()
                .addName("name1")
                .addName("name2")
                .generateNewUUID()
                .build();
        GinasTestServer.User admin = ts.getAdmin();

        try(RestSession session =ts.newRestSession(admin)){
            SubstanceAPI api = session.newSubstanceAPI();
            api.submitSubstance(substance);

            Substance fetched = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());

            assertEquals(admin.getUserName(), fetched.createdBy.username);
            assertEquals(admin.getUserName(), fetched.lastEditedBy.username);
            assertEquals(date, fetched.lastEdited);
            assertEquals(date, fetched.created);


            Name name = fetched.names.get(0);
            assertEquals(admin.getUserName(), name.createdBy.username);
            assertEquals(admin.getUserName(), name.lastEditedBy.username);
            assertEquals(date, name.lastEdited);
            assertEquals(date, name.created);

            Name name2 = fetched.names.get(1);
            assertEquals(admin.getUserName(), name2.createdBy.username);
            assertEquals(admin.getUserName(), name2.lastEditedBy.username);
            assertEquals(date, name2.lastEdited);
            assertEquals(date, name2.created);

        }

    }

    @Test
    public void updateNameUpdatesBothparentSubstanceAndName(){
        LocalDate localDate = LocalDate.of(1955, 11, 12);
        Date date =TimeUtil.toDate(localDate);
        timeTraveller.travelTo(localDate);

        Substance substance = new SubstanceBuilder()
                .addName("name1")
                .generateNewUUID()
                .build();
        GinasTestServer.User admin = ts.getAdmin();

        try(RestSession session =ts.newRestSession(admin)){
            SubstanceAPI api = session.newSubstanceAPI();
            api.submitSubstance(substance);

            Substance fetched = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());

            SubstanceBuilder edit = new SubstanceBuilder(fetched);

            edit.modifyNames( names ->{
                names.get(0).addLanguage("newLang");
            });

            timeTraveller.jumpAhead(1, TimeUnit.DAYS);

            Date newDate = TimeUtil.getCurrentDate();

            api.updateSubstanceJson(edit.buildJson());

            Substance fetched2 = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());

            assertEquals(admin.getUserName(), fetched2.createdBy.username);
            assertEquals(date, fetched2.created);

            assertEquals(admin.getUserName(), fetched2.lastEditedBy.username);
            assertEquals(newDate, fetched2.lastEdited);



            Name name = fetched2.names.get(0);
            assertEquals(admin.getUserName(), name.createdBy.username);
            assertEquals(admin.getUserName(), name.lastEditedBy.username);
            assertEquals(newDate, name.lastEdited);
            assertEquals(date, name.created);

        }
    }

    @Test
    public void addNewNameDoesNotUpdateOldName(){
        LocalDate localDate = LocalDate.of(1955, 11, 12);
        Date date =TimeUtil.toDate(localDate);
        timeTraveller.travelTo(localDate);

        Substance substance = new SubstanceBuilder()
                .addName("name1")
                .generateNewUUID()
                .build();
        GinasTestServer.User admin = ts.getAdmin();

        try(RestSession session =ts.newRestSession(admin)){
            SubstanceAPI api = session.newSubstanceAPI();
            api.submitSubstance(substance);

            Substance fetched = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());

            SubstanceBuilder edit = new SubstanceBuilder(fetched);

            edit.addName("name2");

            timeTraveller.jumpAhead(1, TimeUnit.DAYS);

            Date newDate = TimeUtil.getCurrentDate();

            api.updateSubstanceJson(edit.buildJson());

            Substance fetched2 = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());

            assertEquals(admin.getUserName(), fetched2.createdBy.username);
            assertEquals(date, fetched2.created);

            assertEquals(admin.getUserName(), fetched2.lastEditedBy.username);
            assertEquals(newDate, fetched2.lastEdited);

            assertTrue(newDate.after(date));

            Name oldName = fetched2.names.stream().filter(n-> n.name.equals("name1")).findFirst().orElse(null);


            assertEquals(admin.getUserName(), oldName.createdBy.username);
            assertEquals(admin.getUserName(), oldName.lastEditedBy.username);
            assertEquals(date, oldName.lastEdited);
            assertEquals(date, oldName.created);


            Name newName = fetched2.names.stream().filter(n-> n.name.equals("name2")).findFirst().orElse(null);
            assertEquals(admin.getUserName(), newName.createdBy.username);
            assertEquals(admin.getUserName(), newName.lastEditedBy.username);
            assertEquals(newDate, newName.lastEdited);
            assertEquals(newDate, newName.created);

        }
    }

    @Test
    public void update1NameNameDoesNotUpdateOtherName(){
        LocalDate localDate = LocalDate.of(1955, 11, 12);
        Date date =TimeUtil.toDate(localDate);
        timeTraveller.travelTo(localDate);

        Substance substance = new SubstanceBuilder()
                .addName("name1")
                .addName("name2")
                .generateNewUUID()
                .build();
        GinasTestServer.User admin = ts.getAdmin();

        try(RestSession session =ts.newRestSession(admin)){
            SubstanceAPI api = session.newSubstanceAPI();
            api.submitSubstance(substance);

            Substance fetched = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());
            System.out.println("fetched ="+fetched);
            SubstanceBuilder edit = new SubstanceBuilder(fetched);

            edit.modifyNames( names ->{
                names.stream().filter(n-> n.name.equals("name2")).findFirst().get().addLanguage("newLang");
            });

            timeTraveller.jumpAhead(1, TimeUnit.DAYS);

            Date newDate = TimeUtil.getCurrentDate();

            api.updateSubstanceJson(edit.buildJson());

            Substance fetched2 = api.fetchSubstanceObjectByUuid(substance.getUuid().toString());
            System.out.println("fetched2 ="+fetched2);
            assertEquals(admin.getUserName(), fetched2.createdBy.username);
            assertEquals(date, fetched2.created);

            assertEquals(admin.getUserName(), fetched2.lastEditedBy.username);
            assertEquals(newDate, fetched2.lastEdited);

            assertTrue(newDate.after(date));

            Name oldName = fetched2.names.stream().filter(n-> n.name.equals("name1")).findFirst().orElse(null);


            assertEquals(admin.getUserName(), oldName.createdBy.username);
            assertEquals(admin.getUserName(), oldName.lastEditedBy.username);
            assertEquals(date, oldName.lastEdited);
            assertEquals(date, oldName.created);


            Name newName = fetched2.names.stream().filter(n-> n.name.equals("name2")).findFirst().orElse(null);
            assertEquals(admin.getUserName(), newName.createdBy.username);
            assertEquals(admin.getUserName(), newName.lastEditedBy.username);
            assertEquals(newDate, newName.lastEdited);
            assertEquals(date, newName.created);

        }
    }
}
