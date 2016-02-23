var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=mixture');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    //this one passes an object name to the site selector form, which adds it to the parent at the field//
    this.formElements = {
        formName: 'componentForm',
        fields: [{
            model: 'component.substance',
            type: 'substance-chooser'
        },{
            model: 'component.type',
            type: 'dropdown-select'
        }, {
            model: 'component.role',
            type: 'dropdown-select'
        }, {
            model: 'component.amount',
            type: 'form-selector'
        }, {
            model: 'component.access',
            type: 'form-selector'
        }, {
            model: 'component.reference',
            type: 'form-selector'
        }]
    }
};

