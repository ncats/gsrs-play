var WizardAccessPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=chemical');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //button name is custom binding
    this.formElements = {
        formName: 'accessForm',
        buttonId:'access',
        fields: [{
            model: 'referenceobj.access',
            type: 'multi-select'
        }
        ]
    }

    this.accessPageTests = function(buttonId, model){
        console.log("form selector: " + model);
        this.clickById(buttonId);
        this.clickById(model);

        var wizardAccessPage = new WizardAccessPage();
        var formName = wizardAccessPage.formElements.formName;
        var buttonId = wizardAccessPage.formElements.buttonId;
        var accessFormElements = wizardAccessPage.formElements.fields;
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;

        for (var i = 0; i < accessFormElements.length; i++) {
            var elementType = accessFormElements[i].type;
            var model = accessFormElements[i].model;
            wizardAccessPage.getPage();
            switch (elementType) {
                case "multi-select":
                    elements.testMultiSelectInput(buttonId, model);
                    break;
            } //switch
        } //for i
    }
};
module.exports = WizardAccessPage;
