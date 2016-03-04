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
        formObj: 'reference',
        fields: [{
            model: 'ref.citation',
            type: 'text-input'
        }, {
            model: 'ref.docType',
            type: 'dropdown-select'
        }, {
            model: 'ref.tags',
            type: 'multi-select'
        }, /*{
            model: 'ref.url',
            type: 'text-input'
        },*/{
            model: 'reference.publicDomain',
            type: 'check-box'
        }]
    }

    this.subForms = {
        formName: 'refOnlyForm',
        buttonId: 'references',
        formObj: 'ref',
        fields: [{
            model: 'access',
            type: 'form-selector'
        } /*,{
         binding: 'ref.uploadedFile',
         type: 'binding'
         }*/]
    }
};


describe('reference form test', function () {

    var refForm = new ReferenceForm();
    beforeEach(function () {
        refForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addRefOnlyForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = refForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addRefOnlyForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addRefOnlyForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addRefOnlyForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addRefOnlyForm')).isDisplayed()).toBe(false);

    });


    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['references-toggle'];
        elements.testInputFields(refForm.formElements, breadcrumb);
    });

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['references-toggle'];
        elements.testInputFields(refForm.subForms, breadcrumb);
    });
});
