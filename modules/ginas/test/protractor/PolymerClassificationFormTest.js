var PolymerClassificationPage = function () {

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
        formName: 'classificationForm',
        formObj: 'parent.polymer.classification',
        buttonId: 'polymerClassification',
        fields: [{
            model: 'parent-polymer-classification.polymerClass',
            type: 'dropdown-edit'
        }/*, {
            model: 'parent-polymer-classification.polymerSubclass', //issue with model
            type: 'multi-edit'
        }*/, {
            model: 'parent-polymer-classification.polymerGeometry',
            type: 'dropdown-edit'
        }, {
            model: 'parent-polymer-classification.sourceType',
            type: 'dropdown-edit'
        }]
    }

    this.subForms = {
        formName: 'classificationForm',
        formObj: 'parent.polymer',
        buttonId: 'polymerClassification',
        fields: [/*{
            model: 'parent.polymer.classification.parentSubstance',
            type: 'substance-chooser'
        }, */{
            model: 'access',
            type: 'form-selector'
        }, {
            model: 'reference',
            type: 'form-selector'
        }]
    }
};

describe('Polymer Classification form test', function () {

    var polymerClassPage = new PolymerClassificationPage();
    beforeEach(function () {
        polymerClassPage.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addPolymerClassificationForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = polymerClassPage.formElements.buttonId;
        var vis = browser.findElement(By.id('addPolymerClassificationForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addPolymerClassificationForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addPolymerClassificationForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addPolymerClassificationForm')).isDisplayed()).toBe(false);

    });

    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['polymerClassification-toggle'];
        elements.testInputFields(polymerClassPage.formElements, breadcrumb);
    });

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['polymerClassification-toggle'];
        elements.testInputFields(polymerClassPage.subForms, breadcrumb);
    });
});



