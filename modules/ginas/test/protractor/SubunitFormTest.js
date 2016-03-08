var SubunitsPage = function () {

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
        formName: 'subunitForm',
        buttonId: 'subunits',
        fields: [{

            model: 'subunit.sequence',
            type: 'text-box'
        }
        ]
    }
};
describe('Subunits form test', function () {

    var subunitsPage = new SubunitsPage();
    beforeEach(function () {
        subunitsPage.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addSubunitForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = subunitsPage.formElements.buttonId;
        var vis = browser.findElement(By.id('addSubunitForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addSubunitForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addSubunitForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addSubunitForm')).isDisplayed()).toBe(false);

    });


    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['subunits-toggle'];
        elements.testInputFields(subunitsPage.formElements, breadcrumb);
    });
});
