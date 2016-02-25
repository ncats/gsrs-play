var WizardNamePage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=structurallyDiverse');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
//future tests include:
    //validation on duplicates
    //testing of name resolver function
    //required fields
    

    this.formElements = {
       // pageUrl: '/ginas/app/wizard?kind=chemical',
        formName: 'nameForm',
        buttonID: 'names',
        fields: [{
            model: 'name.name',
            type: 'text-input'
        }, {
            model: 'name.type',
            type: 'dropdown-select'
        }, {
            model: 'name.languages',
            type: 'multi-select'
        }, {
            model: 'name.displayName',
            type: 'check-box'
        }, {
            model: 'name.preferred',
            type: 'check-box'
        }, {
            model: 'name-reference',
            type: 'form-selector'
        }/*, {
            model: 'name.domains',
            type: 'multi-select'
        }, {
            model: 'name.nameJurisdiction',
            type: 'multi-select'
        }, {
            model: 'name.access',
            type: 'form-selector'
        }, {
            model: 'name.nameOrgs',
            type: 'form-selector'
        }*/
        ]
    }
};

describe ('name form', function() {

    var wizardNamePage = new WizardNamePage();
    beforeEach(function() {
        wizardNamePage.getPage();
    });

    it('name form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var buttonId = wizardNamePage.formElements.buttonID;
        var formElements = wizardNamePage.formElements.fields;
        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            wizardNamePage.getPage();
            switch (elementType) {
                case "text-input":
                    elements.testTextInput(buttonId, model);
                    break;
                case "dropdown-select":
                    elements.testDropdownSelectInput(buttonId, model);
                    break;
                case "multi-select":
                   // elements.testMultiSelectInput(buttonId, model);
                    break;
                case "check-box":
                   // elements.testCheckBoxInput(buttonId, model);
                    break;
                case "form-selector":
                   // refPage.refPageTests(buttonId, model);
                    break;
            } //switch
        } //for i
    });
});


