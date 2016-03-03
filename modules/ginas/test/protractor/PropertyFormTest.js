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
        formObj: 'property',
        fields: [{
            model: 'property.name',
            type: 'dropdown-select'
        },{
            model: 'property.type',
            type: 'dropdown-select'
        }, {
            model: 'property.defining',
            type: 'check-box'
        }]
    };

    this.subForms = {
        formName: 'propertyForm',
        buttonId: 'properties',
        formObj: 'property',
        fields: [/*{
            model: 'amount',
            type: 'form-selector'
        }, {
            model: 'parameter',
            type: 'form-selector'
        },*/ {
            model: 'access',
            type: 'form-selector'
        }, {
            model: 'reference',
            type: 'form-selector'
        }]
    }

};

describe ('property form', function() {
    var propertyForm = new PropertyForm();
    beforeEach(function () {
        propertyForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addPropertyForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = propertyForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addPropertyForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addPropertyForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addPropertyForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addPropertyForm')).isDisplayed()).toBe(false);

    });

    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['properties-toggle'];
        elements.testInputFields(propertyForm.formElements, breadcrumb);
    });

    it('should test subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['properties-toggle'];
        elements.testInputFields(propertyForm.subForms, breadcrumb);
    });
});