var WizardNamePage = function () {

 /*   this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=structurallyDiverse');
    };*/

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
        pageUrl: '/ginas/app/wizard?kind=chemical',
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
            model: 'name.reference',
            type: 'form-selector'
        }/*, {
         model: 'name.access',
         type: 'form-selector'
         }, {
            model: 'name.domains',
            type: 'multi-select'
        }, {
            model: 'name.nameOrgs',
            type: 'form-selector'
        }, {
            model: 'name.nameJurisdiction',
            type: 'multi-select'
        }*/
        ]
    }
};

describe ('name form', function() {

    it('name form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var wizardNamePage = new WizardNamePage();
        var pageUrl = wizardNamePage.formElements.pageUrl;
        var formName = wizardNamePage.formElements.formName;
        var buttonId = wizardNamePage.formElements.buttonId;
        var formElements = wizardNamePage.formElements.fields;


        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            switch (elementType) {
                case "text-input":
                    elements.testTextInput(buttonId, model, pageUrl);
                    break;
                case "dropdown-select":
                    elements.testDropdownSelectInput(buttonId, model, pageUrl);
                    break;
                case "multi-select":
                    elements.testMultiSelectInput(buttonId, model, pageUrl);
                    break;
                case "check-box":
                    elements.testCheckBoxInput(buttonId, model, pageUrl);
                    break;
                case "form-selector":
                    elements.testReferencesInput(buttonId, model, pageUrl);
                    break;
            } //switch
        } //for i
    });
});

