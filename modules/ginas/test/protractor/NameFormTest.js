var WizardNamePage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
//future tests include:
    //validation on duplicates
    //testing of name resolver function
    //required fields
    

    this.formElements = {
       // pageUrl: '/ginas/app/wizard?kind=chemical',
        formName: 'nameForm',
        buttonId: 'names',
        formObj:'name',
        fields: [{
            model: 'name.name',
            type: 'text-input'
        }, {
            model: 'name.type',
            type: 'dropdown-select'
        }, {
            model: 'name.languages',
            type: 'multi-select'
        }, {
            model: 'name.displayName',
            type: 'check-box'
        }, {
            model: 'name.preferred',
            type: 'check-box'
        }, {
            model: 'reference',
            type: 'form-selector'
        }, {
            model: 'access',
            type: 'form-selector'
        }/*,{
            model: 'name.domains',
            type: 'multi-select'
        }, {
            model: 'name.nameJurisdiction',
            type: 'multi-select'
        }, {
            model: 'name.nameOrgs',
            type: 'form-selector'
        }*/
        ]
    }
};

describe ('name form test', function() {

    var wizardNamePage = new WizardNamePage();
    beforeEach(function() {
        wizardNamePage.getPage();
    });

    it('should see if form is visible', function(){
        var vis = browser.findElement(By.id('addNameForm')).isDisplayed();
        expect(vis).toBe(false)
    });

    it('should test form toggling', function(){
        var buttonId = wizardNamePage.formElements.buttonId;
        var vis = browser.findElement(By.id('addNameForm')).isDisplayed();
        var button = browser.findElement(By.id(buttonId+"-toggle"));
        expect(browser.findElement(By.id('addNameForm')).isDisplayed()).toBe(false);
        button.click();
        expect(browser.findElement(By.id('addNameForm')).isDisplayed()).toBe(true);
        button.click();
        expect(browser.findElement(By.id('addNameForm')).isDisplayed()).toBe(false);

    });


    it('name form tests', function () {
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;
        var breadcrumb =['names-toggle'];
         elements.testInputFields(wizardNamePage.formElements, breadcrumb);
    });
});


