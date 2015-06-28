
package ix.ntd.views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._

import play.api.templates.PlayMagic._
import models._
import controllers._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.api.i18n._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import views.html._
import ix.ntd.models.Reference;
import ix.ntd.models.Treatment;
/**/
object index extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import ix.ntd.models.Patient

Seq[Any](_display_(/*4.2*/main("CURE")/*4.14*/ {_display_(Seq[Any](format.raw/*4.16*/("""
    """),format.raw/*5.5*/("""<div class="container">
        <div class="row">
            <div class="col-md-12">

                    <!-- Nav tabs -->
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation"><a href="#reference" aria-controls="reference" role="tab" data-toggle="tab">
                        Reference</a></li>
                    <li role="presentation" class="active"><a href="#basic" aria-controls="basic" role="tab" data-toggle="tab">
                        Basic</a></li>
                    <li role="presentation"><a href="#patient" aria-controls="patient" role="tab" data-toggle="tab">
                        Patient</a></li>
                    <li role="presentation"><a href="#dis-presentation" aria-controls="dis-presentation" role="tab" data-toggle="tab">
                        Presentation</a></li>
                    <li role="presentation"><a href="#treatment" aria-controls="treatment" role="tab" data-toggle="tab">
                        Treatment</a></li>
                    <li role="presentation"><a href="#outcome" aria-controls="outcome" role="tab" data-toggle="tab">
                        Outcome</a></li>
                    <li role="presentation"><a href="#review" aria-controls="review" role="tab" data-toggle="tab">
                        Review</a></li>
                </ul>

                    <!-- Tab panes -->
                <form class="form-horizontal" name="caseForm" id = "add-record" role="form" ng-submit="ntd.submit()" novalidate>
                    <div class="tab-content">

                        <div role="tabpanel" class="tab-pane" id="reference" ng-controller="ReferenceController as referenceCtrl">
                            <div class = "form-group">
                                <div class="col-md-4">
                                    <div class="input-group">
                                        <label for="pmid">PMID</label>
                                        <input ng-model="regimen.reference.pmid" type="text" class="form-control" id = "pmid" placeholder="PMID" >
                                        <span class="input-group-btn">
                                            <button ng-click="referenceCtrl.populateFromPubmed()" class="btn btn-default" type="button">Populate from pubmed</button>
                                        </span>
                                    </div>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="refType">Reference Type</label>
                                    <div class="input-group">
                                        <label class="radio-inline" >
                                            <input ng-model="regimen.reference.refType" type="radio" id="refTypeReport" name="refType" value="Clinician_Report" />
                                            Clinician Report
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="regimen.reference.refType" type="radio" id="refTypeReference" name="refType" checked="checked" value="Published_Reference" />
                                            Published Reference
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="doi">DOI</label>
                                    <input ng-model="regimen.reference.doi" type="text" class="form-control" name = "doi" id = "doi" placeholder="doi">
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-12">
                                    <label for="title">Article Title</label>
                                    <input ng-model="regimen.reference.title" type="text" class="form-control" name = "title" id = "title" placeholder="Article Title">
                                </div><!-- /input-group -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-12">
                                    <label for="url">Article URL</label>
                                    <input ng-model="regimen.reference.url" type="url" class="form-control" name = "url" id = "url" placeholder="Article URL">
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="refAbstract">Article Abstract</label>
                                    <textarea ng-model="regimen.reference.refAbstract" class="form-control" rows="3" name = "refAbstract" id = "refAbstract" placeholder="Article Abstract"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-2">
                                    <label for="year">Year Published</label>
                                    <select ng-model="regimen.reference.year" class="form-control" name = "year" id = "year">
                                        <option ng-repeat="n in range(1960)">"""),format.raw/*86.78*/("""{"""),format.raw/*86.79*/("""{"""),format.raw/*86.80*/("""n"""),format.raw/*86.81*/("""}"""),format.raw/*86.82*/("""}"""),format.raw/*86.83*/("""</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="articleType">Article Type</label>
                                    <div class="input-group" ng-init="regimen.reference.articleType='Original'">
                                        """),_display_(/*94.42*/for(opt <- Reference.ArticleType.values()) yield /*94.84*/{_display_(Seq[Any](format.raw/*94.85*/("""
                                            """),format.raw/*95.45*/("""<option value ="""),_display_(/*95.61*/opt),format.raw/*95.64*/(""">"""),_display_(/*95.66*/opt),format.raw/*95.69*/("""</option>
                                        """)))}),format.raw/*96.42*/("""

                                        """),format.raw/*98.41*/("""<label class="radio-inline" >
                                            <input ng-model="regimen.reference.typeOfArticle" type="radio" id="articleTypeNone" name="typeOfArticle" value="None" />
                                            None
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="regimen.reference.typeOfArticle" type="radio" id="articleTypeOriginal" name="typeOfArticle" checked="checked" value="Original" />
                                            Original
                                        </label>
                                        <label class="radio-inline" >
                                            <input ng-model="regimen.reference.typeOfArticle" type="radio" id="articleTypeReview" name="typeOfArticle" value="Review" />
                                            Review
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="TreatmentOrPrevention">Treatment/Prevention</label>
                                    <div class="input-group">
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.treatOrPre" type="radio" id="TreatmentOrPreventionNone" name="treatOrPre" value="None" />
                                            None
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.treatOrPre" type="radio" id="TreatmentOrPreventionTreatment" name="treatOrPre" checked="checked" value="Treatment" />
                                            Treatment
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.treatOrPre" type="radio" id="TreatmentOrPreventionPrevention" name="treatOrPre" value="Prevention" />
                                            Prevention
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="refType">Aim of Study</label>
                                    <div class="input-group">
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.aimOfStudy" type="radio" id="AimofStudyNone" name="aimOfStudy" checked="checked" value="None" />
                                            None
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.aimOfStudy" type="radio" id="AimofStudySafety" name="aimOfStudy" value="Safety" />
                                            Safety
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.aimOfStudy" type="radio" id="AimofStudyEffectiveness" name="aimOfStudy" value="Efffectiveness" />
                                            Effectiveness
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model="patient.reference.aimOfStudy" type="radio" id="AimofStudyBoth" name="aimOfStudy" value="Both" />
                                            Safety and Effectiveness
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="typeOfStudy">Type of Study</label>
                                    <select ng-model="patient.reference.typeOfStudy" class="form-control" name = "typeOfStudy" id = "typeOfStudy">
                                        <option value="Case_Report">Case Report</option>
                                        <option value ="Case_Series">Case Series</option>
                                        <option value = "Case_Study">Case Study</option>
                                        <option value = "Observational_Study">Observational Study</option>
                                        <option value="Clinical_Trial">Clinical Trial</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="language">Language</label>
                                    <select ng-model="patient.reference.language" class="form-control" name = "language" id = "language">
                                        <option value="Case_Report">English</option>
                                        <option value ="Case_Series">English</option>
                                        <option value = "Case_Study">English</option>
                                        <option value = "Observational_Study">English</option>
                                        <option value="Clinical_Trial">English</option>
                                        <option value = "Other">English</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="TreatmentOrPrevention">Treatment/Prevention</label>
                                    <div class="input-group">
                                        <label class="checkbox-inline">
                                            <input ng-model="patient.reference.fullTextAvailable" type="checkbox" id="fullTextAvailable" name="fullTextAvailable" value="fullTextAvailable" />
                                            Full Text Available
                                        </label>
                                        <label class="checkbox-inline" ng-show="patient.reference.fullTextAvailable">
                                            <input ng-model="patient.reference.fullTextInRepository" type="checkbox" id="fullTextInRepository" name="fullTextInRepository" value="fullTextInRepository" ng-show="patient.reference.fullTextAvailable"/>
                                            Full text in repository?
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" class="tab-pane active" id="basic">
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="diseaseName">Disease Name</label>
                                    <input ng-model="patient.disease.diseaseName"  type="text" class="form-control" name = "diseaseName" id = "diseaseName" placeholder="Disease Name" required>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="treatmentName">Treatment Name</label>
                                    <input ng-model="patient.treatments.treatment.name" type="text" class="form-control" name = "treatmentName" id = "treatmentName" placeholder="Treatment Name" required>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="outcome">Outcome</label>
                                    <div class="input-group">
                                        <label class="radio-inline">
                                            <input ng-model = "patient.outcome.summary" type="radio" id="outcomeIndeterminate" name="summary" value="Indeterminate" />
                                            Indeterminate
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model = "patient.outcome.summary" type="radio" id="outcomeSuccess" name="summary" value="Success" />
                                            Success
                                        </label>
                                        <label class="radio-inline">
                                            <input ng-model = "patient.outcome.summary" type="radio" id="outcomeFailure" name="summary" value="Failure" />
                                            Failure
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="patient">
                            <div class = "form-group">
                                <div class="col-md-2">
                                    <label for="age">Age</label>
                                    <select ng-model="regimen.patient.age" class="form-control" name = "age" id = "age" >
                                        """),_display_(/*235.42*/for(ag <- Patient.Age.values()) yield /*235.73*/{_display_(Seq[Any](format.raw/*235.74*/("""
                                        """),format.raw/*236.41*/("""<option value ="""),_display_(/*236.57*/ag),format.raw/*236.59*/(""">"""),_display_(/*236.61*/ag/*236.63*/.displayName()),format.raw/*236.77*/("""</option>
                                    """)))}),format.raw/*237.38*/("""
                                    """),format.raw/*238.37*/("""</select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="gender">Gender</label>
                                    <div class="input-group">
                                        """),_display_(/*245.42*/for(gender <- Patient.Gender.values()) yield /*245.80*/{_display_(Seq[Any](format.raw/*245.81*/("""
                                        """),format.raw/*246.41*/("""<label class="radio-inline">
                                            <input ng-model="regimen.patient.gender" type="radio" id="gender"""),_display_(/*247.110*/gender),format.raw/*247.116*/("""" name="gender" value=""""),_display_(/*247.140*/gender),format.raw/*247.146*/(""""/>"""),_display_(/*247.150*/gender),format.raw/*247.156*/("""</label>
                                        """)))}),format.raw/*248.42*/("""
                                    """),format.raw/*249.37*/("""</div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="country">Country Contracted</label>
                                    <select  ng-model="patient.country" class="form-control" name = "country" id = "country">
                                        <option value = "Other">not America</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="condition">Other Conditions</label>
                                    <div class="input-group">
                                        """),_display_(/*264.42*/for(cond<-Patient.Condition.values()) yield /*264.79*/ {_display_(Seq[Any](format.raw/*264.81*/("""
                                            """),format.raw/*265.45*/("""<label class="checkbox-inline">
                                                <input type="checkbox" id="condition"""),_display_(/*266.86*/cond),format.raw/*266.90*/("""" name="condition" value=""""),_display_(/*266.117*/cond),format.raw/*266.121*/(""""/>
                                                """),_display_(/*267.50*/cond/*267.54*/.displayName()),format.raw/*267.68*/("""
                                            """),format.raw/*268.45*/("""</label>
                                        """)))}),format.raw/*269.42*/("""

                                    """),format.raw/*271.37*/("""</div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="strain">Organism/Strain</label>
                                    <select  ng-model="patient.disease.strain" class="form-control" name = "strain" id = "strain">

                                        <option value = "Other">not America</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="stage">Disease Stage</label>
                                    <select  ng-model="patient.disease.stage" class="form-control" name = "stage" id = "stage">

                                        """),format.raw/*288.120*/("""
                                        """),format.raw/*289.78*/("""
                                            """),format.raw/*290.131*/("""
                                        """),format.raw/*291.80*/("""
                                            """),format.raw/*292.121*/("""
                                        """),format.raw/*293.100*/("""
                                        """),format.raw/*294.91*/("""
                                        """),format.raw/*295.83*/("""
                                        """),format.raw/*296.85*/("""
                                        """),format.raw/*297.106*/("""
                                        """),format.raw/*298.104*/("""
                                        """),format.raw/*299.115*/("""
                                        """),format.raw/*300.98*/("""
                                        """),format.raw/*301.83*/("""
                                    """),format.raw/*302.37*/("""</select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="DiseaseLocation">Disease Location</label>
                                    <select ng-model="patient.disease.location" class="form-control" name = "DiseaseLocation" id = "DiseaseLocation">
                                        <option value="Cardiologic">Cardiologic</option>
                                        <option value ="Gastrointestinal">Gastrointestinal</option>
                                        <option value = "Nervous_System">Nervous System</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="DiseaseForm">Disease Form</label>
                                    <select ng-model="patient.disease.diseaseForm" class="form-control" name = "DiseaseForm" id = "DiseaseForm">
                                        <option value="Case_Report">Acute filarial lymphangitis</option>
                                        <option value ="Case_Series">Alveolar</option>
                                        <option value = "Case_Study">Cutaneous</option>
                                        <option value = "Observational_Study">Cystic (unilocular)</option>
                                        <option value="Clinical_Trial">Elephantiasis</option>
                                        <option value = "Hydrocele">Hydrocele</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-3">
                                    <label for="transmission">Transmission</label>
                                    <select ng-model="patient.disease.transmission" class="form-control" name = "transmission" id = "transmission">
                                        <option value="Arthropod">Arthropod-borne</option>
                                        <option value ="Congenital">Congenital</option>
                                        <option value = "Orally-transmitted">Orally-transmitted</option>
                                        <option value = "Transfusion">Transfusion</option>
                                        <option value="Transplant">Transplant</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="ResistanceOrFailures">Drug Resistance/Failures</label>
                                    <textarea ng-model="patient.disease.ResistanceOrFailures" class="form-control" rows="3" name = "ResistanceOrFailures" id = "ResistanceOrFailures"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="patientNotes">Patient Notes</label>
                                    <textarea ng-model="patient.patientNotes" class="form-control" rows="3" name = "patientNotes" id = "patientNotes" ></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="">Concomitant Medications</label>
                                    <textarea ng-model="patient.patientNotes" class="form-control" rows="3" name = "" id = ""></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="dis-presentation">
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="symptoms">Signs and Symptoms</label>
                                    <textarea class="form-control" rows="3" name = "symptoms" id = "symptoms"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="labs">Lab/Microbial</label>
                                    <textarea class="form-control" rows="3" name = "labs" id = "labs"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="imaging">Radiological/Imaging</label>
                                    <textarea class="form-control" rows="3" name = "imaging" id = "imaging"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>

                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="other">Other</label>
                                    <textarea class="form-control" rows="3" name = "other" id = "other"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="treatment">
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="treatmentName">Drug Name</label>
                                    <input ng-model="regimen.treatments.treatment.name" type="text" class="form-control" name = "treatmentName" id = "treatmentName" placeholder=""""),format.raw/*480.179*/("""{"""),format.raw/*480.180*/("""{"""),format.raw/*480.181*/("""regimen.treatments.treatment.name"""),format.raw/*480.214*/("""}"""),format.raw/*480.215*/("""}"""),format.raw/*480.216*/("""">
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-2">
                                    <label for="dose">Dose</label>
                                    <input ng-model="regimen.treatments.treatment.dose" type="text" class="form-control" name = "dose" id = "dose">
                                </div><!-- /.col-lg-2 -->
                                <div class="col-md-2">
                                    <label for="dosageUnit">Dosage Unit</label>
                                    <select ng-model="regimen.treatments.treatment.dosageUnit" class="form-control" name = "dosageUnit" id = "dosageUnit">
                                        """),_display_(/*491.42*/for(opt <- Treatment.DosageUnit.values()) yield /*491.83*/{_display_(Seq[Any](format.raw/*491.84*/("""
                                            """),format.raw/*492.45*/("""<option value ="""),_display_(/*492.61*/opt),format.raw/*492.64*/(""">"""),_display_(/*492.66*/opt/*492.69*/.displayName()),format.raw/*492.83*/("""</option>
                                        """)))}),format.raw/*493.42*/("""
                                    """),format.raw/*494.37*/("""</select>
                                </div><!-- /.col-lg-2 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-2">
                                    <label for="frequency">Frequency</label>
                                    <input ng-model="patient.treatments.treatment.frequency" type="text" class="form-control" name = "frequency" id = "frequency" placeholder="Frequency">
                                </div><!-- /.col-lg-2 -->
                                <div class="col-md-2">
                                    <label for="frequencyUnit">Frequency Unit</label>
                                    <select ng-model="patient.treatments.treatment.frequencyUnit" class="form-control" name = "frequencyUnit" id = "frequencyUnit">
                                    """),_display_(/*505.38*/for(opt <- Treatment.FrequencyUnit.values()) yield /*505.82*/{_display_(Seq[Any](format.raw/*505.83*/("""
                                        """),format.raw/*506.41*/("""<option value ="""),_display_(/*506.57*/opt),format.raw/*506.60*/(""">"""),_display_(/*506.62*/opt),format.raw/*506.65*/("""</option>
                                    """)))}),format.raw/*507.38*/("""
                                    """),format.raw/*508.37*/("""</select>
                                </div><!-- /.col-lg-2-->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-2">
                                    <label for="treatmentDuration">Duration</label>
                                    <input ng-model="patient.treatments.treatment.treatmentDuration" type="text" class="form-control" name = "treatmentDuration" id = "treatmentDuration" placeholder="Duration">
                                </div><!-- /.col-lg-2 -->
                                <div class="col-md-2">
                                    <label for="durationUnit">Duration Unit</label>
                                    <select ng-model="patient.treatments.treatment.durationUnit" class="form-control" name = "durationUnit" id = "durationUnit">
                                    """),_display_(/*519.38*/for(opt <- Treatment.DurationUnit.values()) yield /*519.81*/{_display_(Seq[Any](format.raw/*519.82*/("""
                                        """),format.raw/*520.41*/("""<option value ="""),_display_(/*520.57*/opt),format.raw/*520.60*/(""">"""),_display_(/*520.62*/opt),format.raw/*520.65*/("""</option>
                                    """)))}),format.raw/*521.38*/("""
                                    """),format.raw/*522.37*/("""</select>
                                </div><!-- /.col-lg-2-->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="treatmentNotes">Treatment Notes</label>
                                    <textarea ng-model="patient.treatments.treatment.treatmentNotes" class="form-control" rows="3" name = "treatmentNotes" id = "treatmentNotes"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <button class = "btn btn-primary">Add another Treatment</button>
                            <button class = "btn btn-danger" ng-show="ntd.patient.disease.treatments.size()>1">Remove Treatment</button>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="surgery">Surgery</label>
                                    <div class="input-group">
                                        <label class="checkbox-inline">
                                            <input ng-model="patient.surgery" type="checkbox" id="surgery" name="surgery" value="true" />
                                            Surgery was performed, in addition to medication
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class = "form-group">
                                <div class="col-md-6">
                                    <label for="previousFailure">Previous Failure?</label>
                                    <div class="input-group">
                                        <label class="checkbox-inline">
                                            <input ng-model="patient.previousFailure" type="checkbox" id="previousFailure" name="previousFailure" value="true" />
                                            The patient previously failed treatment for this condition
                                        </label>>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="outcome">
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="clinical">Clinical</label>
                                    <textarea class="form-control" rows="3" name = "symptoms" id = "symptoms"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="labs">Lab/Microbial</label>
                                    <textarea class="form-control" rows="3" name = "labs" id = "labs"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="imaging">Radiological/Imaging</label>
                                    <textarea class="form-control" rows="3" name = "imaging" id = "imaging"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>

                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="other">Other</label>
                                    <textarea class="form-control" rows="3" name = "other" id = "other"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-5">
                                    <label for="symptomFlags">Flags</label>
                                    <select multiple class="form-control" name = "symptomFlags" id = "symptomFlags">
                                        <option value="WHO_Category_I">AFB Smear</option>
                                        <option value ="WHO_Category_II">Biopsy</option>
                                        <option value = "WHO_Category_III">Biopsy (including aspirate)</option>
                                        <option value = "Preulcerative">Blood Sample</option>
                                        <option value="Ulcerative">Blood Smear</option>
                                        <option value = "Acute">Blood Smear identifying microfilariae</option>
                                        <option value="Chronic">Culture</option>
                                        <option value ="Stage1">Eosinophilia measure</option>
                                        <option value = "Stage2">Histopathology/Biopsy</option>
                                        <option value = "AcuteMicrofilaraemia">Immunological Tests</option>
                                        <option value="Worms">Parasite Visualization in Patient Sample</option>
                                        <option value = "Stage2">PCR</option>
                                        <option value = "AcuteMicrofilaraemia">Serological Tests</option>
                                        <option value="Worms">Xenodiagnosis</option>
                                        <option value = "Other">Other</option>
                                    </select>
                                </div><!-- /.col-lg-6 -->
                            </div>
                            <div class = "form-group">
                                <div class="col-md-8">
                                    <label for="adverseEvents">Adverse Events</label>
                                    <textarea class="form-control" rows="3" name = "adverseEvents" id = "adverseEvents"></textarea>
                                </div><!-- /.col-lg-6 -->
                            </div>
                        </div>


                """),format.raw/*679.17*/("""{"""),format.raw/*679.18*/("""{"""),format.raw/*679.19*/("""patient"""),format.raw/*679.26*/("""}"""),format.raw/*679.27*/("""}"""),format.raw/*679.28*/("""
                        """),format.raw/*680.25*/("""<br/>
"""),format.raw/*681.1*/("""{"""),format.raw/*681.2*/("""{"""),format.raw/*681.3*/("""caseForm.$valid"""),format.raw/*681.18*/("""}"""),format.raw/*681.19*/("""}"""),format.raw/*681.20*/("""
                        """),format.raw/*682.25*/("""<div ng-show="caseForm.$valid">
                            <button class = "btn btn-primary">Submit</button>
                        </div>
                        </div>

                    """),format.raw/*687.31*/("""
                """),format.raw/*688.17*/("""</form>
            </div>
        </div>
    </div>

""")))}),format.raw/*693.2*/("""
"""),format.raw/*695.1*/("""

"""))}
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jun 25 16:58:41 EDT 2015
                  SOURCE: /data/workspace/inxight/modules/ntd/app/ix/ntd/views/index.scala.html
                  HASH: 9829e5e63ad67a596c0537711b4388fccb2abf37
                  MATRIX: 896->68|916->80|955->82|986->87|6771->5844|6800->5845|6829->5846|6858->5847|6887->5848|6916->5849|7438->6344|7496->6386|7535->6387|7608->6432|7651->6448|7675->6451|7704->6453|7728->6456|7810->6507|7880->6549|18349->16990|18397->17021|18437->17022|18507->17063|18551->17079|18575->17081|18605->17083|18617->17085|18653->17099|18732->17146|18798->17183|19213->17570|19268->17608|19308->17609|19378->17650|19545->17788|19574->17794|19627->17818|19656->17824|19689->17828|19718->17834|19800->17884|19866->17921|20816->18843|20870->18880|20911->18882|20985->18927|21130->19044|21156->19048|21212->19075|21239->19079|21320->19132|21334->19136|21370->19150|21444->19195|21526->19245|21593->19283|22606->20345|22676->20423|22751->20554|22821->20634|22896->20755|22967->20855|23037->20946|23107->21029|23177->21114|23248->21220|23319->21324|23390->21439|23460->21537|23530->21620|23596->21657|37676->35707|37707->35708|37738->35709|37801->35742|37832->35743|37863->35744|38701->36554|38759->36595|38799->36596|38873->36641|38917->36657|38942->36660|38972->36662|38985->36665|39021->36679|39104->36730|39170->36767|40075->37644|40136->37688|40176->37689|40246->37730|40290->37746|40315->37749|40345->37751|40370->37754|40449->37801|40515->37838|41444->38739|41504->38782|41544->38783|41614->38824|41658->38840|41683->38843|41713->38845|41738->38848|41817->38895|41883->38932|53833->50853|53863->50854|53893->50855|53929->50862|53959->50863|53989->50864|54043->50889|54077->50895|54106->50896|54135->50897|54179->50912|54209->50913|54239->50914|54293->50939|54515->51142|54561->51159|54647->51214|54676->51245
                  LINES: 30->4|30->4|30->4|31->5|112->86|112->86|112->86|112->86|112->86|112->86|120->94|120->94|120->94|121->95|121->95|121->95|121->95|121->95|122->96|124->98|261->235|261->235|261->235|262->236|262->236|262->236|262->236|262->236|262->236|263->237|264->238|271->245|271->245|271->245|272->246|273->247|273->247|273->247|273->247|273->247|273->247|274->248|275->249|290->264|290->264|290->264|291->265|292->266|292->266|292->266|292->266|293->267|293->267|293->267|294->268|295->269|297->271|314->288|315->289|316->290|317->291|318->292|319->293|320->294|321->295|322->296|323->297|324->298|325->299|326->300|327->301|328->302|506->480|506->480|506->480|506->480|506->480|506->480|517->491|517->491|517->491|518->492|518->492|518->492|518->492|518->492|518->492|519->493|520->494|531->505|531->505|531->505|532->506|532->506|532->506|532->506|532->506|533->507|534->508|545->519|545->519|545->519|546->520|546->520|546->520|546->520|546->520|547->521|548->522|705->679|705->679|705->679|705->679|705->679|705->679|706->680|707->681|707->681|707->681|707->681|707->681|707->681|708->682|713->687|714->688|719->693|720->695
                  -- GENERATED --
              */
          