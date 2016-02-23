var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=nucleicAcid');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'monomerForm',
        buttonID: 'monomers',
        fields: [{
                model: 'component.monomerSubstance',
                type: 'substance-chooser'
            }, {
                model: 'component.type',
                type: 'dropdown-select'
        }, {
            model: 'component.amount',
            type: 'form-selector'
        }]
    }
};

