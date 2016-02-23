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

    //this one passes an object name to the site selector form, which adds it to the parent at the field//
    this.formElements = {
        formName: 'disulfideLinksForm',
        buttonId: 'disulfideLinks',
        fields: [{
            model: 'disulfideLink',
            type: 'form-selector'
        }]
    }
};

