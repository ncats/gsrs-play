var WizardRelationshipPage = function () {

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
        formName: 'relationshipForm',
        buttonID: 'relationships',
        formObj: 'relationship',
        fields: [{
            model: 'relationship.relatedSubstance',
            type: 'substance-chooser'
        },{
            model: 'relationship.type',
            type: 'dropdown-select'
        },{
            model: 'relationship.interactionType',
            type: 'dropdown-select'
        },{
            model: 'relationship.agentSubstance',
            type: 'substance-chooser'
        },{
            model: 'access',
            type: 'form-selector'
        }, {
            model: 'reference',
            type: 'form-selector'
        }
        ]
    }
};

describe ('Relationship form', function() {

    var WizRelPage = new WizardRelationshipPage();
    it('Relationship form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb =['relationships-toggle'];
        elements.testInputFields(WizRelPage.formElements, breadcrumb);
       /* var buttonId = WizRelPage.formElements.buttonID;
        var formElements = WizRelPage.formElements.fields;

        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        var accessElementTests = require('./AccessFormTest.js');
        var accessPage = new accessElementTests;

        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            WizRelPage.getPage();
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
                case "form-selector":
                    if(model == 'relationship-reference') {
                        refPage.refPageTests(buttonId, model);
                    } else if(model == 'relationship-access'){
                        accessPage.accessPageTests(buttonId, model);
                    }

                    break;
            } //switch
        } //for i*/
    });
});

