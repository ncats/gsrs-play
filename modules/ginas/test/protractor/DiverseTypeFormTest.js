var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=structurallyDiverse');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //this form is used to show or hide other form elements
    this.formElements = {
        formName: 'diverseTypeForm',
        fields: [{
            model: 'parent.$$diverseType',
            type: 'radio'
        }]
    }
};

