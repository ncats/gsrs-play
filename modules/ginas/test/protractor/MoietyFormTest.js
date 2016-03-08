var MoietyPage = function () {

    this.getPage = function () {
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };

//count is currently text(integer), but will need to be changed to be an amount
    this.formElements = {
        formName: 'moietyForm',
        formObj: '',
        buttonId: 'moieties',
        fields: [{
            binding: 'obj.id',
            type: 'binding'
        }, {
            binding: 'obj.count',
            type: 'binding'
        }, {
            model: 'obj.formula',
            type: 'text-input'
        }, {
            model: 'obj.stereochemistry',
            type: 'dropdown-view-edit'
        }, {
            model: 'obj.opticalActivity',
            type: 'dropdown-view-edit'
        }, {
            model: 'obj.atropisomerism',
            type: 'dropdown-view-edit'
        }, {
            binding: 'obj.mwt',
            type: 'binding'
        }, {
            binding: 'obj.definedStereo',
            type: 'binding'
        }, {
            binding: 'obj.stereoCenters',
            type: 'binding'
        }, {
            binding: 'obj.ezCenters',
            type: 'binding'
        }, {
            binding: 'obj.charge',
            type: 'binding'
        }, {
            model: 'obj.stereoComments',
            type: 'form-selector'
        }]
    };
};

