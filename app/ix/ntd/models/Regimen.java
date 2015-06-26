package ix.ntd.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sheilstk on 6/25/15.
 */
public class Regimen {

    public Disease disease;
    public List<Treatment> treatments = new ArrayList<Treatment>();

    public Outcome outcome;

    public Patient patient;

    public Reference reference;

    public Regimen(){}
}
