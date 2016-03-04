var StructuralModificationPage = function () {

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
        formName: 'strucModForm',
        buttonId: 'structuralModifications',
        formObj:'modifications',
        fields: [{
            model: 'mod.structuralModificationType',
            type: 'dropdown-select'
        },{
            model: 'mod.locationType',
            type: 'dropdown-select'
        }, {
            model: 'mod.extent',
            type: 'dropdown-select'
        }]
    }

    this.subForms = {
        formName: 'strucModForm',
        buttonId: 'structuralModifications',
        formObj:'mod',
        fields: [/*{
            model: 'mod.molecularFragment',
            type: 'substance-chooser'
        },{
            model: 'site',
            type: 'form-selector'
        },*/{
            model: 'amount',
            type: 'form-selector'
        }]
    }
};

describe('StructuralModification form test', function () {

    var strucModForm = new StructuralModificationPage();
    beforeEach(function () {
        strucModForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addStrucModForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = strucModForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addStrucModForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addStrucModForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addStrucModForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addStrucModForm')).isDisplayed()).toBe(false);

    });


   /* it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['structuralModifications-toggle'];
        elements.testInputFields(strucModForm.formElements, breadcrumb);
    });*/

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['structuralModifications-toggle'];
        elements.testInputFields(strucModForm.subForms, breadcrumb);
    });
});


