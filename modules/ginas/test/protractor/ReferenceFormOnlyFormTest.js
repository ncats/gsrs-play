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

    //same as the reference form but without the apply functionality. this is the one that appears on the bottom of the page
    this.formElements = {
        formName: 'refOnlyForm',
        fields: [{
            model: 'ref.citation',
            type: 'text-input'
        }, {
            model: 'ref.docType',
            type: 'dropdown-select'
        }, {
            model: 'ref.tags',
            type: 'multi-select'
        }, {
            model: 'ref.url',
            type: 'text-input'
        }, {
            model: 'ref.access',
            type: 'form-selector'
        }, {
            binding: 'ref.uploadedFile',
            type: 'binding'
        }]
    }
};

