var RelationshipForm = function () {

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
        buttonId: 'relationships',
        formObj: 'relationship',
        fields: [{
            model: 'relationship.type',
            type: 'dropdown-select'
        },{
            model: 'relationship.interactionType',
            type: 'dropdown-select'
        }
        ]
    };

    this.subForms = {
        formName: 'relationshipForm',
        buttonID: 'relationships',
        formObj: 'relationship',
        fields: [{
 /*           model: 'relationship.relatedSubstance',
            type: 'substance-chooser'
        },{
            model: 'relationship.agentSubstance',
            type: 'substance-chooser'
        },{*/
/*            model: 'access',
            type: 'form-selector'
        }, {*/
            model: 'reference',
            type: 'form-selector'
/*        }, {
            model: 'comments',
            type: 'form-selector'*/
        }
        ]
    }
};

describe ('Relationship form', function() {
    var relationshipForm = new RelationshipForm();
    beforeEach(function () {
        relationshipForm.getPage();
    });
/*
    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addRelationshipForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = relationshipForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addRelationshipForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addRelationshipForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addRelationshipForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addRelationshipForm')).isDisplayed()).toBe(false);

    });

    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['relationships-toggle'];
        elements.testInputFields(relationshipForm.formElements, breadcrumb);
    });*/

    it('should test subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['relationships-toggle'];
        elements.testInputFields(relationshipForm.subForms, breadcrumb);
    });
});

