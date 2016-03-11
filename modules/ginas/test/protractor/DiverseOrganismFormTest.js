var DiverseOraganismPage = function () {

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
        formName: 'diverseDetailsForm',
        buttonId: 'organismDetails',
        fields: [{
            model: 'structurallyDiverse.organismFamily',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.organismGenus',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.organismSpecies',
            type: 'dropdown-view-edit'
        }, {
            model: 'structurallyDiverse.organismAuthor',
            type: 'dropdown-view-edit'
        }]
    }
};

