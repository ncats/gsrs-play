var WizardCommentPage = function () {

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
        formName: 'accessForm',
        fields: [{
            model: 'referenceobj.comments',
            type: 'text-box'
            }
        ]
    };

   /* this.commentPageTests = function(buttonId, model){
        console.log("form selector: " + model);
        this.clickById(buttonId);
        this.clickById(model);

        var wizCommentPage = new WizardCommentPage();
        var formName = wizCommentPage.formElements.formName;
        var buttonId = wizCommentPage.formElements.buttonId;
        var refFormElements = wizCommentPage.formElements.fields;
        var commonElementTests = require('./TestWizardCommonElements.js');
        var elements = new commonElementTests;

        for (var i = 0; i < refFormElements.length; i++) {
            var elementType = refFormElements[i].type;
            var model = refFormElements[i].model;
            wizCommentPage.getPage();
            switch (elementType) {
                case "text-box":
                    elements.testTextArea(buttonId, model);
                    break;
            } //switch
        } //for i
    }*/
};

module.exports=WizardCommentPage;

