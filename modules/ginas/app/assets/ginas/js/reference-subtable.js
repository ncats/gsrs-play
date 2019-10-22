function addRef(uuid,button,table){
    var tableCols = {'classifications':4,'names':4,'identifiers':5,'notes':2};
    var row = "#"+table+"-row-"+uuid;
    var ref = "#"+table+"-ref-"+uuid;
    var toAdd = "<tr id = "+table+"-"+uuid+"refSub><td colspan = '"+tableCols[table]+"' style = 'padding-left:20px'>"+$(ref).html()+"</td></tr>";
    //$(button).toggleClass("clicked");
    if( $("#"+table+"-"+uuid+"refSub").length ){
        $("#"+table+"-"+uuid+"refSub").remove();
        $(button).text('view');
    }else{
        $(row).after(toAdd);
        $(button).text('hide');
    }


}

function addDetails(uuid,button,table) {
    var tableCols = {'classifications': 4, 'names': 4, 'identifiers': 5, 'notes': 2};
    var row = "#" + table + "-row-" + uuid;
    var det = "#" + table + "-det-" + uuid;
    var toAdd = "<tr id = " + table + "-" + uuid + "detailsSub><td colspan = '" + tableCols[table] + "' style = 'padding-left:20px'>" + $(det).html() + "</td></tr>";
    if ($("#" + table + "-" + uuid+"detailsSub").length) {
        $("#" + table + "-" + uuid+"detailsSub").remove();
        $(button).text('view');
    }else{
        $(row).after(toAdd);
        $(button).text('hide');
    }

}

