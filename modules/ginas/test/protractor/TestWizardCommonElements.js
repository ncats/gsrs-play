var WizardCommonElements = function () {

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.testTextInput = function (buttonId, model) {
        console.log("text-input-model: " + model);
        console.log("text-input-buttonId: " + buttonId);
        var elementId = model.split(".")[1];
        console.log("text-input-elementId: " + elementId);
        this.clickById(buttonId);
        var userInput = element(by.id(elementId));
        userInput.clear().sendKeys('testing');
         expect(userInput.getAttribute('value')).toEqual('testing');
    };

    this.testTextArea = function(buttonId, model) {
        console.log("text-area-model: " + model);
        console.log("text-area-buttonId: " + buttonId);
        var elementId = model.split(".")[1];
        console.log("text-area-elementId: " + elementId);
        var userInput = element(by.id(elementId));
        userInput.clear().sendKeys('testing');
        expect(userInput.getAttribute('value')).toEqual('testing');
    }

    this.testDropdownSelectInput = function (buttonId, model) {
        console.log("drop-down-select: " +model);
        this.clickById(buttonId);
        this.clickByModel(model);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        element(by.model(model)).all(by.id(elementId)).each(function (element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log(items.length);
                console.log(items[1] + " : " + items[items.length-2])
                expect(items.length).toBeGreaterThan(0);
                expect(items[items.length - 1]).toBe('Other');
            });
        });
    };

    this.testMultiSelectInput = function (buttonId, model) {
        console.log("multi-select model: " +model);
        console.log("multi-select buttonId: " + buttonId );
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        this.clickById(buttonId);
        element(by.id(elementId)).click();
        //element(by.model(model)).click().then(function(){
            element(by.model(model)).all(by.css('.suggestion-list')).each(function (element, index) {
                element.getText().then(function (text) {
                    var items = text.split('\n');
                    console.log(items.length);
                    expect(items.length).toBeGreaterThan(0);
                });
            });
        //});
    };

    this.testCheckBoxInput = function (buttonId, model) {
        console.log("checkbox: " + model);
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