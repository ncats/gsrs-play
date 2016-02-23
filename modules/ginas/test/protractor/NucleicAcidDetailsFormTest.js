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
        formName: 'naDetailsForm',
        buttonID: 'nucleicAcidClassification',
        fields: [{
            model: 'parent.nucleicAcid.nucleicAcidType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.nucleicAcid.nucleicAcidSubType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.nucleicAcid.sequenceOrigin',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.nucleicAcid.sequenceType',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.nucleicAcid.access',
            type: 'form-selector'
        }, {
            model: 'parent.nucleicAcid.reference',
            type: 'form-selector'
        }]
    }
};

