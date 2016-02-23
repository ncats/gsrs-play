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

    //these are set only once, but ideally:
    //subunits will highlight after update(requires subunit redraw)
    //remove highlight if site removed
    //restrict to specific types of sites
    this.formElements = {
        formName: 'glycosylationForm',
        fields: [{
            model: 'parent.protein.glycosylation.glycosylationType',
            type: 'dropdown-select'
        },{
            model: 'parent.protein.glycosylation.CGlycosylationSites',
            type: 'form-selector'
        },{
            model: 'parent.protein.glycosylation.NGlycosylationSites',
            type: 'form-selector'
        },{
            model: 'parent.protein.glycosylation.OGlycosylationSites',
            type: 'form-selector'
        }]
    }
};

