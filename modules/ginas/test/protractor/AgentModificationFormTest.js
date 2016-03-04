var AgentModificationPage = function () {

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
        formName: 'agentModForm',
        formObj: 'modifications',
        buttonId:'agentModifications',
        fields: [{
            model: 'agentMod.agentModificationType',
            type: 'dropdown-select'
        },{
            model: 'agentMod.agentModificationProcess',
            type: 'dropdown-select'
        },{
            model: 'agentMod.agentModificationRole',
            type: 'dropdown-select'
        }]
    }

    this.subForms = {
        formName: 'agentModForm',
        formObj: 'agentMod',
        buttonId:'agentModifications',
        fields: [/*{
            model: 'agentMod.agentSubstance',
            type: 'substance-chooser'
        },*/{
            model: 'amount',
            type: 'form-selector'
        }]
    }
};

describe('AgentModification form test', function () {

    var agentForm = new AgentModificationPage();
    beforeEach(function () {
        agentForm.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addAgentModForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = agentForm.formElements.buttonId;
        var vis = browser.findElement(By.id('addAgentModForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addAgentModForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addAgentModForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addAgentModForm')).isDisplayed()).toBe(false);

    });


    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['agentModifications-toggle'];
        elements.testInputFields(agentForm.formElements, breadcrumb);
    });

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['agentModifications-toggle'];
        elements.testInputFields(agentForm.subForms, breadcrumb);
    });
});

