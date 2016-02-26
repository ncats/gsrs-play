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
        fields: [{
            binding: 'formType',
            type: 'binding'
        },{
            binding: 'name',
            type: 'binding'
        },{
            model: 'parent.definitionType',
            type: 'dropdown-select'
        },{
            model: 'parent.$$relatedSubstance',
            type: 'form-selector'
        },{
            model: 'parent.access',
            type: 'form-selector'
        }]
    }
};

describe ('header form test', function() {

    var headerForm = new HeaderForm();

    beforeEach(function() {
        headerForm.getPage();
    });



    it('should check the substance class of the form', function(){
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var kind = browser.params.baseUrl;
        console.log(kind);
        var el1 = elements.testModelBinding('formType');
        console.log(el1);
        var text = ((el1).getText());
        console.log("text +" + text);
        expect(text.toEqual('Registering new'));
/*        var el2 = elements.testModelBinding('name');
        expect(el2.toEqual(kind));*/
    });

    /*it('name form tests', function () {
        var buttonId = headerForm.formElements.buttonID;
      //  var formElements = headerForm.formElements.fields;

        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        var accessElementTests = require('./AccessFormTest.js');
        var accessPage = new accessElementTests;

        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            headerForm.getPage();
            switch (elementType) {
                case "dropdown-select":
                    elements.testDropdownSelectInput(buttonId, model);
                    break;
                case "binding":
                    elements.testModelBinding(buttonId, model);
                    break;
                case "form-selector":
                    if(model == 'name-reference') {
                        refPage.refPageTests(buttonId, model);
                    } else if(model == 'name-access'){
                        accessPage.accessPageTests(buttonId, model);
                    }

                    break;
            } //switch
        } //for i
    });*/
});


