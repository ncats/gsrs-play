function addRef(uuid,button,table){
    var tableCols = {'classifications':4,'names':4,'identifiers':5,'notes':2};
    var row = "#"+table+"-row-"+uuid;
    var ref = "#"+table+"-ref-"+uuid;
    var toAdd = "<tr id = "+table+"-"+uuid+"><td colspan = '"+tableCols[table]+"' style = 'padding-left:20px'>"+$(ref).html()+"</td></tr>";
    //$(button).toggleClass("clicked");

    if( $("#"+table+"-"+uuid).length ){
        $("#"+table+"-"+uuid).remove();
        $(button).text('view');
    }else{
        $(row).after(toAdd);
        $(button).text('hide');
    }

}

