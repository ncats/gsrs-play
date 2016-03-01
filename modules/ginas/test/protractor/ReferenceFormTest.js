var WizardReferencePage = function () {

    this.getPage = function(){
        browser.get(browser.params.url);
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
///reference apply needs its own functionality check
    //upload file needs separate testing
    //this form is not clooapsible, it is tolggled by the form selector directive

    this.formElements = {
        formName: 'refForm',
       // buttonId: 'references',
        fields: [{
            model: 'reference.citation',
            type: 'text-input'
        }, {
            model: 'reference.docType',
            type: 'dropdown-select'
        }, {
            model: 'ref.tags',
            type: 'multi-select'
        }, {
            model: 'ref.url',
            type: 'text-input'
        },/* {
            model: 'ref.access',
            type: 'form-selector'
        }, {
            binding: 'ref.uploadedFile',
            type: 'binding'
        }*/]
    };
};

module.exports = WizardReferencePage;