var substringMatcher = function (strs) {
    return function findMatches(q, cb) {
        var matches, substrRegex;

        // an array that will be populated with substring matches
        matches = [];

        // regex used to determine if a string contains the substring `q`
        substrRegex = new RegExp(q, 'i');

        // iterate through the pool of strings and for any string that
        // contains the substring `q`, add it to the `matches` array
        $.each(strs, function (i, str) {
            if (substrRegex.test(str)) {
                matches.push({value:str});
            }
        });
        cb(matches);
    };
};

function addEntitiesToShoppingCart(entities, etype, folderName, url) {
    var accs = _.map(entities, function (x) {
        var o = {};
        o.type = etype;
        o.entity = x;
        return (o);
    });
    console.log("Will add following entities to " + folderName + ": " + JSON.stringify(accs));
    $.ajax({
        url: url,
        type: "POST",
        data: {'json': JSON.stringify(accs), 'folder': folderName},
        success: function (dat, status, xhr) {
            $("#sc-count").html(dat.total_entries);
        },
        error: function(x) {
            console.log(x);
        }
    });
}