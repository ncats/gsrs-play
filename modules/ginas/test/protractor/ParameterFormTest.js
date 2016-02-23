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

    this.formElements = {
        formName: 'parameterForm',
        buttonID: 'parameters',
        fields: [{
            model: 'parameter.name',
            type: 'text-input'
        }, {
            model: 'parameter.type',
            type: 'text-input'
        }, {
            model: 'parameter.amount',
            type: 'form-selector'
        }]
    }
};

