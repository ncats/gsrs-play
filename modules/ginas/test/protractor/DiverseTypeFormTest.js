var DiverseTypePage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //this form is used to show or hide other form elements
    this.formElements = {
        formName: 'diverseTypeForm',
        buttonId: 'diverseType',
        fields: [{
            model: 'parent.$$diverseType',
            type: 'radio'
        }]
    }
};

describe('Diverse Type form test', function () {

    var diverseTypePage = new DiverseTypePage();
    beforeEach(function () {
        diverseTypePage.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addDiverseTypeForm')).isDisplayed();
        expect(vis).toBe(true)
    });

    it('should test form toggling', function () {
        var buttonId = diverseTypePage.formElements.buttonId;
        var vis = browser.findElement(By.id('addDiverseTypeForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addDiverseTypeForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addDiverseTypeForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addDiverseTypeForm')).isDisplayed()).toBe(true);

    });

    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['diverseType-toggle'];
        elements.testInputFields(diverseTypePage.formElements, breadcrumb);
    });
});

