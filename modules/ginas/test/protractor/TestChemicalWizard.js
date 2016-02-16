/*describe('wizard chemical', function() {
    beforeEach(function() {
        browser.get('http://localhost:9000/ginas/app/wizard?kind=chemical');
    });

    it('should have a title', function() {
        expect(browser.getTitle()).toEqual('Register');
    });

    it('should click names', function(){
        element(by.id('names')).click();
        element(by.model('name.type')).click();
        expect(element(by.model('name.type')).$('option:checked').getText()).toEqual('Type...');

        element(by.model('name.type')).all(by.id('type')).each(function(element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log( items[5]);
                expect(items.length).toBe(7);
                expect(items[0]).toBe('Type...');
                expect(items[5]).toBe('Systematic Name');
            });
        });


    });

});*/

var ChemicalWizardPage = function() {

    this.getPage = function () {
       // browser.get('http://localhost:9000/ginas/app/wizard?kind=chemical');
        browser.get('/ginas/app/wizard?kind=chemical');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
}

describe('Wizard Chemical', function() {

    it('Check the Name Types', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('names');
        chemicalWizardPage.clickByModel('name.type');
        expect(element(by.model('name.type')).$('option:checked').getText()).toEqual('Type...');

        element(by.model('name.type')).all(by.id('type')).each(function(element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log( items[5]);
                expect(items.length).toBe(7);
                expect(items[0]).toBe('Type...');
                expect(items[5]).toBe('Systematic Name');
            });
        });
    }, 100000);

    it('Check the Code Systems', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('codes');

        element(by.id('codes')).click().then(function(){
            expect(element(by.model('code.codeSystem')).$('option:checked').getText()).toEqual('Code System...');
        });

        element(by.model('code.codeSystem')).all(by.id('codeSystem')).each(function(element, index) {
            element.getText().then(function (text) {
                var codes = text.split('\n');
                console.log( codes[5]);
                expect(codes.length).toBe(40);
                expect(codes[0]).toBe('Code System...');
                expect(codes[1]).toBe('"Food Contact Substance Notif, (FCN No.)"');
                expect(codes[38]).toBe('WIKIPEDIA');
                expect(codes[39]).toBe('Other');
            });
        });
    });

    it('Check the Code Types', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('codes');

        element(by.id('codes')).click().then(function(){
            expect(element(by.model('code.type')).$('option:checked').getText()).toEqual('Type...');
        });

        element(by.model('code.type')).all(by.id('type')).each(function(element, index) {
            element.getText().then(function (text) {
                var codeTypes = text.split('\n');
                console.log( codeTypes[5]);
                expect(codeTypes.length).toBe(9);
                expect(codeTypes[0]).toBe('Type...');
                expect(codeTypes[1]).toBe('ALTERNATIVE');
                expect(codeTypes[7]).toBe('SUPERCEDED');
                expect(codeTypes[8]).toBe('Other');
            });
        });
    });

    it('Check the Relationship Types', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('relationships');

        element(by.model('relationship.type')).all(by.id('type')).each(function(element, index) {
            element.getText().then(function (text) {
                console.log(text);
                var codeTypes = text.split('\n');
                console.log( codeTypes[5]);
                expect(codeTypes.length).toBe(58);
                expect(codeTypes[0]).toBe('Type...');
                expect(codeTypes[1]).toBe('ACTIVE ISOMER->PARENT');
                expect(codeTypes[56]).toBe('TRANSPORTER->SUBSTRATE');
                expect(codeTypes[57]).toBe('Other');
            });
        });
    });

    it('Check the Relationship Interaction', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('relationships');

        element(by.model('relationship.interactionType')).all(by.id('interactionType')).each(function(element, index) {
            element.getText().then(function (text) {
                var interacTypes = text.split('\n');
                console.log( interacTypes[5]);
                expect(interacTypes.length).toBe(29);
                expect(interacTypes[0]).toBe('Interaction...');
                expect(interacTypes[1]).toBe('ACTIVATOR');
                expect(interacTypes[27]).toBe('WEIGHT PERCENT');
                expect(interacTypes[28]).toBe('Other');
            });
        });
    });

    it('Check the Relationship Qualification', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('relationships');

        element(by.model('relationship.qualification')).all(by.id('qualification')).each(function(element, index) {
            element.getText().then(function (text) {
                var interacTypes = text.split('\n');
                console.log( interacTypes[5]);
                expect(interacTypes.length).toBe(16);
                expect(interacTypes[0]).toBe('Qualification...');
                expect(interacTypes[1]).toBe('Batch Data');
                expect(interacTypes[14]).toBe('Unidentified');
                expect(interacTypes[15]).toBe('Other');
            });
        });
    });


    it('Check the Property Names', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('properties');

        element(by.model('property.name')).all(by.id('name')).each(function(element, index) {
            element.getText().then(function (text) {
                var propertyNames = text.split('\n');
                console.log( propertyNames[5]);
                expect(propertyNames.length).toBe(25);
                expect(propertyNames[0]).toBe('Name...');
                expect(propertyNames[1]).toBe('ABSORBING POWER');
                expect(propertyNames[23]).toBe('VISCOSITY:KINEMATIC');
                expect(propertyNames[24]).toBe('Other');
            });
        });
    });

    it('Check the Property Types', function() {
        var chemicalWizardPage = new ChemicalWizardPage();
        chemicalWizardPage.getPage();
        chemicalWizardPage.clickById('properties');

        element(by.model('property.type')).all(by.id('type')).each(function(element, index) {
            element.getText().then(function (text) {
                var propertyTypes = text.split('\n');
                console.log( propertyTypes[2]);
                expect(propertyTypes.length).toBe(5);
                expect(propertyTypes[0]).toBe('Type...');
                expect(propertyTypes[1]).toBe('CHEMICAL');
                expect(propertyTypes[3]).toBe('PHYSICAL');
                expect(propertyTypes[4]).toBe('Other');
            });
        });
    });


});
