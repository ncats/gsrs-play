var ReferenceForm = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //same as the reference form but without the apply functionality. this is the one that appears on the bottom of the page
    this.formElements = {
        formName: 'refOnlyForm',
        buttonId: 'references',
        fields: [{
            model: 'ref.citation',
            type: 'text-input'
        }, {
            model: 'ref.docType',
            type: 'dropdown-select'
        }, {
            model: 'ref.tags',
            type: 'multi-select'
        }, {
            model: 'ref.url',
            type: 'text-input'
/*        }, {
            model: 'ref.access',
            type: 'form-selector'
        }, {
            binding: 'ref.uploadedFile',
            type: 'binding'*/
        }]
    }
};

describe ('reference form tests', function() {

    var referenceForm = new ReferenceForm();
    beforeEach(function() {
        referenceForm.getPage();
    });

    it('tests all form elements are loaded', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var buttonId = referenceForm.formElements.buttonId;
        var formElements = referenceForm.formElements.fields;
        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            referenceForm.getPage();
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

