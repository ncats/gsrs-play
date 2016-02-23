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

    //this form has a lot of parsing involved


    this.formElements = {
        formName: 'siteStringForm',
        fields: [{
            model: 'referenceobj.$$displayString',
            type: 'text-input'
        }
        ]
    }
};

