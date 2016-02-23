var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=polymer');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'sruForm',
        fields: [{
            binding: 'obj.id',
            type: 'binding'
        }, {
            model: 'obj.label',
            type: 'text-input'
        }, {
            model: 'obj.type',
            type: 'dropdown-view-edit'
        }, {
            model: 'parent.nucleicAcid.sequenceOrigin',
            type: 'dropdown-view-edit'
        }, {
            model: 'obj.amount',
            type: 'form-selector'
        },{
            model: 'obj._displayConnectivity',
            type: 'text-input'
        }]
    }
};

