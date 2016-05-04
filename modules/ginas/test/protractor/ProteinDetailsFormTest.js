var ProteinDetailsPage = function () {

    this.getPage = function () {

        browser.get(browser.params.url);

    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.login = function() {
        browser.ignoreSynchronization = true;
        browser.switchTo().alert().then(
            function (alert) {
                console.log("there is an alert");
                alert.accept(); },
            function (err) { }
        );
        browser.get("http://localhost:9000/ginas/app");
        element(by.id('login-button')).click();
        expect(browser.getCurrentUrl()).toMatch('/login');

        var uname = element(by.id('username'));
        var paswd = element(by.id('password'));
        var submitButton = element(by.tagName('button'));

        uname.sendKeys('admin');
        paswd.sendKeys('admin');

        expect(uname.getAttribute('value')).toEqual('admin');
        expect(paswd.getAttribute('value')).toEqual('admin');
        submitButton.click();
        expect(browser.getCurrentUrl()).toMatch('/ginas/app');
    };

    this.formElements = {
        formName: 'detailsForm',
        formObj: 'proteinDetails',
        buttonId: 'proteinDetails',
        fields: [{
            model: 'parent-protein.proteinType',
            type: 'dropdown-edit'
        }, {
            model: 'parent-protein.proteinSubType',
            type: 'dropdown-edit'
        }, {
            model: 'parent-protein.sequenceOrigin',
            type: 'dropdown-edit'
        }, {
            model: 'parent-protein.sequenceType',
            type: 'dropdown-edit'
        }]
    }

    this.subForms = {
        formName: 'detailsForm',
        formObj: 'proteinDetails',
        buttonId: 'proteinDetails',
        fields: [{
            model: 'parent.protein.access',
            type: 'form-selector'
        }, {
            model: 'parent.protein.reference',
            type: 'form-selector'
        }]
    }
};

describe('Protein Details form test', function () {

    var proteinDetailsForm = new ProteinDetailsPage();
    beforeEach(function () {
        proteinDetailsForm.login();
        proteinDetailsForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addDetailsForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = proteinDetailsForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addDetailsForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addDetailsForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addDetailsForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addDetailsForm')).isDisplayed()).toBe(false);

    });


    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['proteinDetails-toggle'];
        elements.testInputFields(proteinDetailsForm.formElements, breadcrumb);
    });

     it('should test all subforms', function () {
     var commonElementTests = require('./TestWizardCommonElements.js');
     var elements = new commonElementTests;
     var breadcrumb = ['proteinDetails-toggle'];
     elements.testInputFields(proteinDetailsForm.subForms, breadcrumb);
     });
});


