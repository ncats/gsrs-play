var NuclicAcidClassificationPage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

    this.formElements = {
        formName: 'naDetailsForm',
        formObj: '',
        buttonId: 'nucleicAcidClassification',
        fields: [{
            model: 'parent.nucleicAcid.nucleicAcidType',
            type: 'dropdown-edit'
        }, {
            model: 'parent.nucleicAcid.nucleicAcidSubType',
            type: 'dropdown-edit'
        }, {
            model: 'parent.nucleicAcid.sequenceOrigin',
            type: 'dropdown-edit'
        }, {
            model: 'parent.nucleicAcid.sequenceType',
            type: 'dropdown-edit'
        }]
    }

    this.subForms = {
        formName: 'naDetailsForm',
        formObj: '',
        buttonId: 'nucleicAcidClassification',
        fields: [{
            model: 'parent.nucleicAcid.access',
            type: 'form-selector'
        }, {
            model: 'parent.nucleicAcid.reference',
            type: 'form-selector'
        }]
    }
};

