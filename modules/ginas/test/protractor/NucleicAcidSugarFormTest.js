var ProteinWizardPage = function () {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=structurallyDiverse');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
//the 'getAllSites()' variable is the return of a method call, so I'm not sure how to get it by binding
    //also has applyAll() button

    this.formElements = {
        formName: 'sugarForm',
        fields: [{
            binding: 'getAllSites()',
            type: 'binding'
        }, {
            model: 'sugar.sugar',
            type: 'dropdown-select'
        },  {
            model: 'sugar.sites',
            type: 'form-selector'
        }]
    }
};

