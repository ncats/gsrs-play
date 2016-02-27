var WizardReferencePage = function () {

    this.getPage = function(){
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
///reference apply needs its own functionality check
    //upload file needs separate testing
    //this form is not clooapsible, it is tolggled by the form selector directive

    this.formElements = {
        formName: 'refForm',
       // buttonId: 'references',
        fields: [{
            model: 'reference.citation',
            type: 'text-input'
        }, {
            model: 'reference.docType',
            type: 'dropdown-select'
        }, {
            model: 'ref.tags',
            type: 'multi-select'
        }/* {
            model: 'ref.url',
            type: 'text-input'
        }, {
            model: 'ref.access',
            type: 'form-selector'
        }, {
            binding: 'ref.uploadedFile',
            type: 'binding'
        }*/]
    };

    this.refPageTests = function(buttonId, model){
        console.log("form selector: " + model);
        this.clickById(buttonId);
        this.clickById(model);

        var wizardRefPage = new WizardReferencePage();
        var formName = wizardRefPage.formElements.formName;
        var buttonId = wizardRefPage.formElements.buttonId;
        var refFormElements = wizardRefPage.formElements.fields;
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;

        for (var i = 0; i < refFormElements.length; i++) {
            var elementType = refFormElements[i].type;
            var model = refFormElements[i].model;
            wizardRefPage.getPage();
            switch (elementType) {
                case "text-input":
                    elements.testTextInput(buttonId, model);
                    break;
                case "dropdown-select":
                    elements.testDropdownSelectInput(buttonId, model);
                    break;
                case "multi-select":
                    elements.testMultiSelectInput(buttonId, model);
                    break;
                case "check-box":
                    elements.testCheckBoxInput(buttonId, model);
                    break;
            } //switch
        } //for i

    }
};

module.exports = WizardReferencePage;