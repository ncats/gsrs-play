var PropertyForm = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'propertyForm',
        buttonId: 'properties',
        fields: [{
            model: 'property.name',
            type: 'dropdown-select'
        }, {
            model: 'property.type',
            type: 'dropdown-select'
/*        }, {
            model: 'property.amount',
            type: 'form-selector'
        }, {
            model: 'property.parameter',
            type: 'form-selector'*/
        }, {
            model: 'property.defining',
            type: 'check-box'
/*        }, {
            model: 'property.access',
            type: 'form-selector'
        }, {
            model: 'property.reference',
            type: 'form-selector'*/
        }]
    }
};

describe ('property form tests', function() {

    var propertyForm = new PropertyForm();
    beforeEach(function() {
        propertyForm.getPage();
    });

    it('tests all form elements are loaded', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var buttonId = propertyForm.formElements.buttonId;
        var formElements = propertyForm.formElements.fields;
        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            propertyForm.getPage();
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

