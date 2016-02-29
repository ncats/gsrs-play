var CodeForm = function () {
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
        formName: 'codeForm',
        buttonId:'codes',
        formObj:'code',
        fields: [{
            model: 'code.codeSystem',
            type: 'dropdown-select'
        }, {
            model: 'code.type',
            type: 'dropdown-select'
        }, {
            model: 'code.code',
            type: 'text-input'
        }, {
            model: 'code.url',
            type: 'text-input'
        }, {
            model: 'comments',
            type: 'form-selector'
        }, {
            model: 'access',
            type: 'form-selector'
        }, {
            model: 'reference',
            type: 'form-selector'
        }]
    }
};

describe ('Code form test', function() {

    var codeForm = new CodeForm();
    beforeEach(function() {
        codeForm.getPage();
    });

    it('should see if form is visible', function(){
        var vis = browser.findElement(By.id('addCodeForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function(){
        var buttonId = codeForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addCodeForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId+"-toggle"));
        expect(browser.findElement(By.id('addCodeForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addCodeForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addCodeForm')).isDisplayed()).toBe(false);

    });


    it('code form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb =['codes-toggle'];
        elements.testInputFields(codeForm.formElements, breadcrumb);
    });
});


