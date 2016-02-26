
var WizardCommonElements = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.testTextInput = function (model) {
        console.log("text-input-model: " + model);
     //   console.log("text-input-buttonId: " + buttonId);
        var elementId = model.split(".")[1];
        console.log("text-input-elementId: " + elementId);
      //  this.clickById(buttonId);
        var userInput = element(by.id(elementId));
        userInput.clear().sendKeys('testing');
         expect(userInput.getAttribute('value')).toEqual('testing');
    };

    this.testTextArea = function(model) {
        console.log("text-area-model: " + model);
    //    console.log("text-area-buttonId: " + buttonId);
        var elementId = model.split(".")[1];
        console.log("text-area-elementId: " + elementId);
        var userInput = element(by.id(elementId));
        userInput.clear().sendKeys('testing');
        expect(userInput.getAttribute('value')).toEqual('testing');
    };

    this.testDropdownSelectInput = function (model) {
        console.log("drop-down-select: " +model);
       // this.clickById(buttonId);
        this.clickByModel(model);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        element(by.model(model)).all(by.id(elementId)).each(function (element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log(items.length);
                console.log(items[1] + " : " + items[items.length-2]);
                expect(items.length).toBeGreaterThan(0);
                expect(items[items.length - 1]).toBe('Other');
            });
        });
    };

    this.testDropdownSelectEdit = function (model) {
        console.log("drop-down-edit: " +model);
       // this.clickById(buttonId);
      //  this.clickByModel(model);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
        var elem = browser.findElement(By.id(elementId));
        elem.click();
            elem.getText().then(function (text) {
                var items = text.split('\n');
                console.log(items);
                console.log(items.length);
                console.log(items[1] + " : " + items[items.length-2]);
                expect(items.length).toBeGreaterThan(0);
                expect(items[items.length - 1]).toBe('Other');
            });
    };

    this.testMultiSelectInput = function (model) {
        console.log("multi-select model: " +model);
       // console.log("multi-select buttonId: " + buttonId );
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
      //  this.clickById(buttonId);
        element(by.id(elementId)).click();
        //element(by.model(model)).click().then(function(){
        var elem = browser.findElement(by.model(model));
          //  element(by.model(model)).all(by.css('.suggestion-list')).each(function (element, index) {
                elem.getText().then(function (text) {
                    console.log("tttttt" + text);
                    var items = text.split(' ');
                    console.log(items.length);
                    expect(items.length).toBeGreaterThan(0);
                });
          //  });
        //});
    };

    this.testCheckBoxInput = function (model) {
        console.log("checkbox: " + model);
        var elementId = model.split(".")[1];
        console.log("elementId: " + elementId);
     //   this.clickById(buttonId);
        var chekBox = element(by.id(elementId));
        if(!chekBox.isSelected()){
            console.log("not checked");
            chekBox.click();
        }
        expect(chekBox.isSelected()).toBe(false);
    };

    this.testBinding = function(model){
        console.log("test binding "  + model);
       // expect(element(by.exactBinding(model)).isPresent()).toBe(true);
    };


    this.testModelBinding = function (model) {
            console.log(model);
        var ret = element(by.exactBinding(model));
            return ret.getText();
    };

    this.testInputFields = function(elements, parentbutton, button){
       // var buttonId = wizardNamePage.formElements.buttonID;
       // var formElements = wizardNamePage.formElements.fields;
        var fields = elements.fields;
        var refElementTests = require('./ReferenceFormTest.js');
        var refPage = new refElementTests;
        var accessElementTests = require('./AccessFormTest.js');
        var accessPage = new accessElementTests;
        for (var i = 0; i < fields.length; i++) {
            var elementType = fields[i].type;
            var model = fields[i].model;
                this.getPage();
            if(elements.buttonId != undefined) {
                var toggleButton = browser.findElement(By.id(elements.buttonId +'-toggle'));
                console.log("toggle " + elements.buttonId);
            toggleButton.click();
            }
            if(parentbutton) {
                console.log(parentbutton);
                var parentButton = browser.findElement(By.id(parentbutton+'-toggle'));
                parentButton.click().then(function(){
                var toggleButton = browser.findElement(By.id(button));
            toggleButton.click();
                });
            }
            switch (elementType) {
                case "text-input":
                    this.testTextInput(model);
                    break;
                case "dropdown-select":
                    this.testDropdownSelectInput(model);
                    break;
                case "dropdown-edit":
                    this.testDropdownSelectEdit(model);
                    break;
                case "multi-select":
                    this.testMultiSelectInput(model);
                    break;
                case "check-box":
                    this.testCheckBoxInput( model);
                    break;
                case "binding":
                    console.log(model);
                    this.testBinding(model);
                    break;
                case "form-selector":
                    if(model == 'reference') {
                        var newbtn = elements.formObj +"-reference";
                        var parentbtn = elements.buttonId;
                        this.testInputFields(refPage.formElements, parentbtn, newbtn);
                    } else if(model == 'access'){
                        var newbtn = elements.formObj +"-access";
                        console.log(newbtn);
                        this.testInputFields(accessPage.formElements, newbtn);
                    }

                    break;
            } //switch
        } //for i
    }


};
module.exports = WizardCommonElements;