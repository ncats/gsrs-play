var WizardCommonElements = function () {

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.testTextInput = function (buttonId, model, pageUrl) {
        browser.get(pageUrl);
        console.log("text-input-model: " + model);
        console.log("text-input-buttonId: " + buttonId);
        var elementId = model.split(".")[1];
        console.log("text-input-elementId: " + elementId);
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
        //expect(element(by.model(model)).$('option:checked').getText()).toEqual('Type...');
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
};
module.exports = WizardCommonElements;