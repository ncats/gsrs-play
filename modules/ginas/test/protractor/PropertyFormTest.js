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
        formName: 'propertyForm',
        fields: [{
            model: 'property.name',
            type: 'dropdown-select'
        }, {
            model: 'property.type',
            type: 'dropdown-select'
        }, {
            model: 'property.amount',
            type: 'form-selector'
        }, {
            model: 'property.parameter',
            type: 'form-selector'
        }, {
            model: 'property.defining',
            type: 'check-box'
        }, {
            model: 'property.access',
            type: 'form-selector'
        }, {
            model: 'property.reference',
            type: 'form-selector'
        }]
    }
};

