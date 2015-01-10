$(document).ready(function () {
    var entities = new Bloodhound({
        datumTokenizer: function(d) {
            return Bloodhound.tokenizers.whitespace(d.key);
        },
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        remote: '/api/suggest/Entity?q=%QUERY',
    });
    
    var mesh = new Bloodhound({
        datumTokenizer: function(d) {
            return d.key.split(/\//);
        },
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        remote: '/api/suggest/MeSH?q=%QUERY',
    });
    entities.initialize();
    mesh.initialize();
    
    $('.typeahead').typeahead({
        hint: true,
        highlight: true,
        minLength: 2
    }, {
        name: 'Target',
        displayKey: 'key',
        source: entities.ttAdapter()
    }, {
        name: 'MeSH',
        displayKey: 'key',
        source: mesh.ttAdapter()
    });

    $('.typeahead').on("typeahead:selected", function (evt, val, d) {
        console.log('typeahead selected: '+val);

        for (var f in val) {
            console.log(f+' '+val[f]);
        }
        if (val.key.indexOf('/') > 0) {
            /* this is mesh term, so don't quote */
        }
        else {
            $('.typeahead').typeahead('val', '"'+val.key+'"');
        }
    });
    
    $('.typeahead').on("typeahead:closed", function (evt) {
        console.log('typeahead closed!');
    });
});
