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
        formName: 'physicalParameterForm',
        buttonID: 'physicalParameter',
        fields: [{
            model: 'physicalParameter.parameterName',
            type: 'text-input'
        }, {
            model: 'physicalParameter.amount',
            type: 'form-selector'
        }]
    }
};

