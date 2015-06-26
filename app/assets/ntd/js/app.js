/**
 * Created by sheilstk on 6/24/15.
 */

(function () {
    var ntdApp = angular.module('ntd', []);

    ntdApp.controller('NtdController', function ($scope, $http) {
        $scope.regimen= {};
        var url = '/ntd/index';
        this.submit = function(){
           var data = $scope.regimen;

            console.log(url);
            console.log(data);
            data= JSON.stringify(data);
            $http.post(url, data);
        };

//var date =  Date.now();
//    $scope.number =date.prototype.getFullYear();

    $scope.range = function(min, step){
        step = step || 1;
        var input = [];
        for (var i = 2015; i >= min; i -= step) input.push(i);
        return input;
    };

    ntdApp.controller('TreatmentController',function(){
        this.addTreatment = function(treatment){
            patient.disease.treatments.push(treatment);
            this.treatment= {};
        };
    });
    });
    ntdApp.controller('ReferenceController',function($scope, $http){
        this.populateFromPubmed= function() {
            var pmid = $scope.patient.reference.pmid;
            console.log(pmid);
            var url = 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=json&rettype=abstract&id=' + pmid;
            console.log(url);
            $http.get(url).success(function (data) {
                console.log(data);
                $scope.patient.reference.title= data.getElement('title');
            });
        };
    });



    //var patient = {
    //    age: null,
    //    gender: null,
    //    condition: null,
    //    country: null,
    //    surgery: false,
    //    previousFailure: false,
    //    disease: disease,
    //    reference: reference,
    //    outcome: outcome,
    //    patientNotes: null
    //};
    //
    //var disease = {
    //    stage: null,
    //    strain: null,
    //    diseaseLocation: null,
    //    diseaseForm: null,
    //    transmission: null,
    //    resistanceOrFailures: null,
    //    treatments: []
    //};
    //
    //var reference = {
    //    year: null,
    //    pmid: null,
    //    doi: null,
    //    title: null,
    //    url: null,
    //    refAbstract: null,
    //    refType: null,
    //    typeOfArticle: null,
    //    typeOfStudy: null,
    //    aimOfStudy: null,
    //    treatOrPre: null,
    //    language: null,
    //    fullTextAvailable: null,
    //    fullTextInRepository: null
    //};
    //
    //var outcome = {
    //    summary: null,
    //    clinical: [],
    //    microbial: [],
    //    imaging: [],
    //    adverseEvents: [],
    //    followUp: null,
    //    relapse: false
    //};
    //
    //var treatment = {
    //    treatmentName: null,
    //    dose: null,
    //    treatmentDuration: null,
    //    frequency: null,
    //    regimenID: null,
    //    dosageUnit: null,
    //    frequencyUnit: null,
    //    durationUnit: null,
    //    route: null,
    //    treatmentNotes: null
    //};
})();