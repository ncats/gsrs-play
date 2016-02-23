var ProteinWizardPage = function() {

    this.getPage = function () {
        browser.get('/ginas/app/wizard?kind=protein');
    };

    this.clickById = function (name) {
        element(by.id(name)).click();
    };

    this.clickByModel = function (name) {
        element(by.model(name)).click();
    };
}

describe('Wizard Protein', function() {

    it('Check Protein Name Types', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('names');
        proteinWizardPage.clickByModel('name.type');
        expect(element(by.model('name.type')).$('option:checked').getText()).toEqual('Type...');

        element(by.model('name.type')).all(by.id('type')).each(function (element, index) {
            element.getText().then(function (text) {
                var items = text.split('\n');
                console.log(items[5]);
                expect(items.length).toBe(7);
                expect(items[0]).toBe('Type...');
                expect(items[5]).toBe('Systematic Name');
            });
        });
    }, 100000);

    it('Check Protein Agent Modification Types', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('agentModifications');
        proteinWizardPage.clickByModel('agentMod.agentModificationType');
        expect(element(by.model('agentMod.agentModificationType')).$('option:checked').getText()).toEqual('Modification Type...');

        element(by.model('agentMod.agentModificationType')).all(by.id('agentModificationType')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(26);
                expect(modTypes[1]).toBe('ACID TREATMENT');
                expect(modTypes[24]).toBe('SULFATING AGENT');
            });
        });
    });

    it('Check Protein Agent Modification Process', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('agentModifications');
        proteinWizardPage.clickByModel('agentMod.agentModificationProcess');
        expect(element(by.model('agentMod.agentModificationProcess')).$('option:checked').getText()).toEqual('Modification Process...');

        element(by.model('agentMod.agentModificationProcess')).all(by.id('agentModificationProcess')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(53);
                expect(modTypes[1]).toBe('ACETYLATION');
                expect(modTypes[51]).toBe('TREATED');
            });
        });
    });

    it('Check Protein Agent Modification Role', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('agentModifications');
        proteinWizardPage.clickByModel('agentMod.agentModificationRole');
        expect(element(by.model('agentMod.agentModificationRole')).$('option:checked').getText()).toEqual('Modification Role...');

        element(by.model('agentMod.agentModificationRole')).all(by.id('agentModificationRole')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(84);
                expect(modTypes[3]).toBe('ACIDIFICATION');
                expect(modTypes[82]).toBe('TRANSFECTION');
            });
        });
    });

    it('Check Protein Structural Modification Type', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('structuralModifications');
        proteinWizardPage.clickByModel('mod.structuralModificationType');
        expect(element(by.model('mod.structuralModificationType')).$('option:checked').getText()).toEqual('Modification Type...');

        element(by.model('mod.structuralModificationType')).all(by.id('structuralModificationType')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(12);
                expect(modTypes[3]).toBe('Fragment');
                expect(modTypes[10]).toBe('RADIOLABEL');
            });
        });
    });

    it('Check Protein Structural Location Type', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('structuralModifications');
        proteinWizardPage.clickByModel('mod.locationType');
        expect(element(by.model('mod.locationType')).$('option:checked').getText()).toEqual('Location Type...');

        element(by.model('mod.locationType')).all(by.id('locationType')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(9);
                expect(modTypes[3]).toBe('C-TERMINUS');
                expect(modTypes[7]).toBe('UNKNOWN');
            });
        });
    });

    it('Check Protein Structural Extent', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('structuralModifications');
        proteinWizardPage.clickByModel('mod.extent');
        expect(element(by.model('mod.extent')).$('option:checked').getText()).toEqual('Extent...');

        element(by.model('mod.extent')).all(by.id('extent')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[1]);
                expect(modTypes.length).toBe(4);
                expect(modTypes[2]).toBe('Partial');
                expect(modTypes[3]).toBe('Other');
            });
        });
    });


    it('Check Protein Relationships Type', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('relationships');
        proteinWizardPage.clickByModel('relationship.type');
        expect(element(by.model('relationship.type')).$('option:checked').getText()).toEqual('Type...');
        element(by.model('relationship.type')).all(by.id('type')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(58);
                expect(modTypes[2]).toBe('ACTIVE MOIETY');
                expect(modTypes[56]).toBe('TRANSPORTER->SUBSTRATE');
            });
        });
    });

    it('Check Protein Relationships Interaction', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('relationships');
        proteinWizardPage.clickByModel('relationship.interactionType');
        expect(element(by.model('relationship.interactionType')).$('option:checked').getText()).toEqual('Interaction...');
        element(by.model('relationship.interactionType')).all(by.id('interactionType')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(29);
                expect(modTypes[2]).toBe('AGONIST');
                expect(modTypes[27]).toBe('WEIGHT PERCENT');
            });
        });
    });

    it('Check Protein Relationships Qualification', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('relationships');
        proteinWizardPage.clickByModel('relationship.qualification');
        expect(element(by.model('relationship.qualification')).$('option:checked').getText()).toEqual('Qualification...');
        element(by.model('relationship.qualification')).all(by.id('qualification')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(16);
                expect(modTypes[2]).toBe('Consultation');
                expect(modTypes[14]).toBe('Unidentified');
            });
        });
    });


    it('Check Protein Properties Name', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('properties');
        proteinWizardPage.clickByModel('property.name');
        expect(element(by.model('property.name')).$('option:checked').getText()).toEqual('Name...');
        element(by.model('property.name')).all(by.id('name')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[5]);
                expect(modTypes.length).toBe(25);
                expect(modTypes[3]).toBe('ACTIVITY');
                expect(modTypes[23]).toBe('VISCOSITY:KINEMATIC');
            });
        });
    });

    it('Check Protein Properties Type', function () {
        var proteinWizardPage = new ProteinWizardPage();
        proteinWizardPage.getPage();
        proteinWizardPage.clickById('properties');
        proteinWizardPage.clickByModel('property.type');
        expect(element(by.model('property.type')).$('option:checked').getText()).toEqual('Type...');
        element(by.model('property.type')).all(by.id('type')).each(function (element, index) {
            element.getText().then(function (text) {
                var modTypes = text.split('\n');
                console.log(modTypes[4]);
                expect(modTypes.length).toBe(5);
                expect(modTypes[1]).toBe('CHEMICAL');
                expect(modTypes[3]).toBe('PHYSICAL');
            });
        });
    });



});




/* it('Check Protein Codes System', function () {
 var proteinWizardPage = new ProteinWizardPage();
 proteinWizardPage.getPage();
 proteinWizardPage.clickById('codes');
 proteinWizardPage.clickByModel('code.codeSystem');
 expect(element(by.model('code.codeSystem')).$('option:checked').getText()).toEqual('Code System...');

 element(by.model('code.codeSystem')).all(by.id('codeSystem')).each(function (element, index) {
 element.getText().then(function (text) {
 var modTypes = text.split('\n');
 console.log(modTypes[22]);
 expect(modTypes.length).toBe(22);
 expect(modTypes[2]).toBe('AIDS');
 expect(modTypes[3]).toBe('WIKIPEDIA');
 });
 });
 });

 it('Check Protein Reference Document Type', function () {
 var proteinWizardPage = new ProteinWizardPage();
 proteinWizardPage.getPage();
 proteinWizardPage.clickById('references');
 proteinWizardPage.clickByModel('ref.docType');
 expect(element(by.model('ref.docType')).$('option:checked').getText()).toEqual('Document Type...');
 element(by.model('ref.docType')).all(by.id('docType')).each(function (element, index) {
 element.getText().then(function (text) {
 var modTypes = text.split('\n');
 console.log(modTypes[5]);
 expect(modTypes.length).toBe(5);
 expect(modTypes[2]).toBe('BLA');
 expect(modTypes[3]).toBe('WIKIPEDIA');
 });
 });
 });


 */
