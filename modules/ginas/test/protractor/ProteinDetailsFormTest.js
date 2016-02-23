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
        formName: 'detailsForm',
        fields: [{
            model: 'parent.protein.proteinType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.protein.proteinSubType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.protein.sequenceOrigin',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.protein.sequenceType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.protein.access',
            type: 'form-selector'
        }, {
            model: 'parent.protein.reference',
            type: 'form-selector'
        }]
    }
};

