var PolymerMonomerPage = function () {

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
        formName: 'monomerForm',
        formObj: 'component',
        buttonId: 'monomers',
        fields: [{
                model: 'component.monomerSubstance',
                type: 'substance-chooser'
            }, {
                model: 'component.type',
                type: 'dropdown-select'
            }]
    }

    this.subForms = {
        formName: 'monomerForm',
        formObj: 'component',
        buttonId: 'monomers',
        fields: [{
         model: 'amount',
         type: 'form-selector'
         }]
    }
};

describe('Polymer Monomer form test', function () {

    var monomerPage = new PolymerMonomerPage();
    beforeEach(function () {
        monomerPage.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addMonomerForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = monomerPage.formElements.buttonId;
        var vis = browser.findElement(By.id('addMonomerForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addMonomerForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addMonomerForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addMonomerForm')).isDisplayed()).toBe(false);

    });

    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['monomers-toggle'];
        elements.testInputFields(monomerPage.formElements, breadcrumb);
    });

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['monomers-toggle'];
        elements.testInputFields(monomerPage.subForms, breadcrumb);
    });
});



