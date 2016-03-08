var DiverseSourceMaterialsPage = function () {

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
        formName: 'diverseSourceForm',
        formObj: 'parent.structurallyDiverse',
        buttonId: 'sourceMaterials',
        fields: [{
            model: 'structurallyDiverse.sourceMaterialClass',
            type: 'dropdown-edit'
        }, {
            model: 'structurallyDiverse.sourceMaterialType',
            type: 'dropdown-edit'
        }, {
            model: 'structurallyDiverse.sourceMaterialState',
            type: 'dropdown-view-edit'
        }]
    }

    this.subForms = {
        formName: 'diverseSourceForm',
        formObj: 'parent.structurallyDiverse',
        buttonId: 'sourceMaterials',
        fields: [{
            model: 'reference',
            type: 'form-selector'
        }]
    }
};

describe('Diverse Source Materials form test', function () {

    var diverseSMPage = new DiverseSourceMaterialsPage();
    beforeEach(function () {
        diverseSMPage.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addDiverseSourceForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function () {
        var buttonId = diverseSMPage.formElements.buttonId;
        var vis = browser.findElement(By.id('addDiverseSourceForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addDiverseSourceForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addDiverseSourceForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addDiverseSourceForm')).isDisplayed()).toBe(false);

    });


    it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['sourceMaterials-toggle'];
        elements.testInputFields(diverseSMPage.formElements, breadcrumb);
    });

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['sourceMaterials-toggle'];
        elements.testInputFields(diverseSMPage.subForms, breadcrumb);
    });
});


