var StructureFormPage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //sketcher will probably need its own set of tests
//not toggleable

    this.formElements = {
        formName: 'structureForm',
        formObj: 'parent.structure',
        buttonId: 'structure',
        fields: [{
            model: '"parent-structure.-formula',
            type: 'text-input'
        }, {
            model: 'parent.stereoChemistry',
            type: 'dropdown-edit'
        }, {
            model: 'parent-structure.opticalActivity',
            type: 'dropdown-edit'
        }, {
            model: 'parent-structure.atropisomerism',
            type: 'dropdown-edit'
        }]
    }

    this.subForms = {
        formName: 'structureForm',
        formObj: 'parent.structure',
        buttonId: 'structure',
        fields: [{
            model: 'access',
            type: 'form-selector'
        },{
            model: 'reference',
            type: 'form-selector'
        },{
            binding: 'parent.structure.mwt',
            type: 'binding'
        }, {
            binding: 'parent.structure.definedStereo',
            type: 'binding'
        }, {
            binding: 'parent.structure.stereoCenters',
            type: 'binding'
        }, {
            binding: 'parent.structure.ezCenters',
            type: 'binding'
        }, {
            binding: 'parent.structure.charge',
            type: 'binding'
        }/*, {
            model: 'comments',
            type: 'form-selector'                   //fix stereo comments id
        }*/]
    }
};

describe('Structure form test', function () {

    var structureFormPage = new StructureFormPage();
    beforeEach(function () {
        structureFormPage.getPage();
    });

    it('should see if form is visible', function () {
        var vis = browser.findElement(By.id('addStructureForm')).isDisplayed();
        expect(vis).toBe(true)
    });

    it('should test form toggling', function () {
        var buttonId = structureFormPage.formElements.buttonId;
        var vis = browser.findElement(By.id('addStructureForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId + "-toggle"));
        expect(browser.findElement(By.id('addStructureForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addStructureForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addStructureForm')).isDisplayed()).toBe(true);

    });


   it('should test all basic form elements', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['structure-toggle'];
        elements.testInputFields(structureFormPage.formElements, breadcrumb);
    });

    it('should test all subforms', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb = ['structure-toggle'];
        elements.testInputFields(structureFormPage.subForms, breadcrumb);
    });
});


