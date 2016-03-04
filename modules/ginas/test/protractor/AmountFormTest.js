var WizardAmountPage = function () {

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
        formName: 'amountForm',
        formObj: 'amount',
        fields: [{
            model: 'amount.type',
            type: 'dropdown-select'
        },{
            model: 'amount.average',
            type: 'text-input'
        },{
            model: 'amount.low',
            type: 'text-input'
        },{
            model: 'amount.high',
            type: 'text-input'
        },{
            model: 'amount.lowLimit',
            type: 'text-input'
        },{
            model: 'amount.highLimit',
            type: 'text-input'
        },{
            model: 'amount.units',
            type: 'dropdown-select'
        },{
            model: 'amount.nonNumericValue',
            type: 'text-input'
        }
        ]
    }
};
module.exports=WizardAmountPage;

