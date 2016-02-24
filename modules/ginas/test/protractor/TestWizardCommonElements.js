var WizardCommonElements = function () {

   /* this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=structurallyDiverse');
    };*/

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.forms = {
        pageUrl: '/ginas/app/wizard?kind=chemical',
        formName: 'nameForm',
        buttonId: 'names',
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
        },{
            model: 'name.preferred',
            type: 'check-box'
        },{
            model: 'name.reference',
            type: 'form-selector'
        }]
    };

    this.testTextInput = function (buttonId, model, pageUrl) {
        browser.get(pageUrl);
        console.log("text-input: " + model);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        this.clickById(buttonId);
       // browser.findElement(By.id('name')).sendKeys('testing');
        var userType = element(by.id(elementId));
         userType.clear().sendKeys('testing');
         expect(userType.getAttribute('value')).toEqual('testing');
    };

    this.testDropdownSelectInput = function (buttonId, model, pageUrl) {
        browser.get(pageUrl);
        console.log("drop-down-select: " +model);
        this.clickById(buttonId);
        this.clickByModel(model);
        expect(element(by.model(model)).$('option:checked').getText()).toEqual('Type...');
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        element(by.model(model)).all(by.id(elementId)).each(function (element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log(items.length);
                expect(items.length).toBeGreaterThan(0);
                expect(items[items.length - 1]).toBe('Other');
            });
        });
    };

    this.testMultiSelectInput = function (buttonId, model, pageUrl) {
        browser.get(pageUrl);
        console.log("multi-select " +model);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        this.clickById(buttonId);
        this.clickByModel(model);
        element(by.id(elementId)).click();
        element(by.css('.tags')).click();
        element(by.model('newTag.text')).click();
        element(by.model(model)).all(by.css('.suggestion-list')).each(function (element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log(items.length);
                expect(items.length).toBeGreaterThan(0);
            });
        });
    };

    this.testCheckBoxInput = function (buttonId, model, pageUrl) {
        console.log("checkbox: " + model);
        browser.get(pageUrl);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        this.clickById(buttonId);
        var chekBox = element(by.id(elementId));
        if(!chekBox.isSelected()){
            console.log("not checked");
            chekBox.click();
        }
        expect(chekBox.isSelected()).toBe(false);
    };

   /* this.testFormSelector = function (buttonId, model, pageUrl) {
        console.log("form selector: " + model);
        browser.get(pageUrl);
        var elementId = model.split(".")[1];
        this.clickById(buttonId);
      /!*  var chekBox = element(by.id(elementId));
        if(!chekBox.isSelected()){
            console.log("not checked");
            chekBox.click();
        }
        expect(chekBox.isSelected()).toBe(false);*!/
    };*/

    this.testReferencesInput = function (){

    }

};
module.exports = WizardCommonElements;

/*describe('Wizard Common', function () {

      it('wizard common elements', function () {
        var wizardCommonElements = new WizardCommonElements();
        var pageUrl = wizardCommonElements.forms.pageUrl;
        var formName = wizardCommonElements.forms.formName;
        var buttonId = wizardCommonElements.forms.buttonId;
        var formElements = wizardCommonElements.forms.fields;


        for (var i = 0; i < formElements.length; i++) {
            var elementType = formElements[i].type;
            var model = formElements[i].model;
            switch (elementType) {
                case "text-input":
                    wizardCommonElements.testTextInput(buttonId, model, pageUrl);
                    break;
                case "dropdown-select":
                    wizardCommonElements.testDropdownSelectInput(buttonId, model, pageUrl);
                    break;
                case "multi-select":
                    wizardCommonElements.testMultiSelectInput(buttonId, model, pageUrl);
                    break;
                case "check-box":
                    wizardCommonElements.testCheckBoxInput(buttonId, model, pageUrl);
                    break;
                case "form-selector":
                    wizardCommonElements.testFormSelector(buttonId, model, pageUrl);
                    break;
            } //switch
        } //for i
    });
}); //describe*/
