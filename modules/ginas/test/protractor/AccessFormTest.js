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

    //button name is custom binding
    this.formElements = {
        formName: 'accessForm',
        buttonId:'{{name}}{{type}}',
        fields: [{
            model: 'referenceobj.access',
            type: 'dropdown-select'
        }
        ]
    }
};

