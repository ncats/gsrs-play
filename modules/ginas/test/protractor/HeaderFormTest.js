var HeaderForm = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

//anything with '$$' in fron of the field name should get stripped out with the angular.toJson function
    //2 variables are passed in, so we probably want to check with binding
    //no button to toggle
    this.formElements = {
        formName: 'headerForm',
        formObj:  'parent',
        fields: [{
            model: 'formType',
            type: 'binding'
        },{
            model: 'name',
            type: 'binding'
        },{
            model: 'parent.definitionType',
            type: 'dropdown-edit'
/*        },{
/!*            model: 'parent.$$relatedSubstance',
            type: 'substance-selector'*/
/*        },{
            model: 'access',
            type: 'form-selector'*/
        }]
    }
};

describe ('header form test', function() {

    var headerForm = new HeaderForm();

    beforeEach(function() {
        headerForm.getPage();
    });

    it('should check if form is edit or new', function(){
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var kind = browser.params.baseUrl;
        expect(elements.testModelBinding('formType')).toEqual('Registering new');
    });

    it('should check the substance class of the form', function(){
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var kind = browser.params.baseUrl;
        expect(elements.testModelBinding('name')).toEqual(kind);
    });

    it('should test if elements loaded', function(){
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        elements.testInputFields(headerForm.formElements, []);
    });
});


