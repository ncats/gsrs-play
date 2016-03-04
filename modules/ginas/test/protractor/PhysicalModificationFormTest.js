var PhysicalModificationPage = function () {

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
        formName: 'physicalModForm',
        formObj:  'PhysicalModification',
        buttonId: 'physicalModifications',
        fields: [{
            model: 'physicalModification.physicalModificationRole',
            type: 'dropdown-select'
        }/*, {
            model: 'Parameter',
            type: 'form-selector'
        }*/]
    }
};

describe('Physical Modification form test', function () {

    var physModForm = new PhysicalModificationPage();
    beforeEach(function () {
        physModForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addPhysicalModForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = physModForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addPhysicalModForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addPhysicalModForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addPhysicalModForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addPhysicalModForm')).isDisplayed()).toBe(false);

    });


     it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['physicalModifications-toggle'];
        elements.testInputFields(physModForm.formElements, breadcrumb);
    });

   /* it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['physicalModifications-toggle'];
        elements.testInputFields(physModForm.subForms, breadcrumb);
    });*/
});

