$(document).ready(function () {
  /* Table initialisation */

    var table = $('#grant-table')
    .on('xhr.dt', function ( e, settings, json ) {
       console.log("hi");
             // Note no return - manipulate the data directly in the JSON object.
    } )
        .DataTable( {
    	"responsive":true,
    	"processing": true,
     "order": [[ 1, "asc" ]],
      "serverSide": true,
      "pagingType": "full_numbers",
      "jQueryUI": true,
      "infoFiltered": "(_MAX_ ms)",
      drawCallback: function() {
          $("#grant-table_filter").remove();
        },
      "ajax": "crosstalk/grants" ,
      "columns": [
                   {"data": "fullProjectNum",
          	    "render": function ( data, type, full, meta ) {
          	    	console.log(data);
          	      return '<a href="crosstalk/report/'+data+'">'+data+'</a>';
          	    }
                  }, 
                  
                  { "data": "projectTitle" },
                  { "data": "icName" },
                  { "data": "edInstType" },
                  { "data": "programOfficerName" },
                  { "data": "totalCost" }                 

                  ]
            
    });
    var table2 = $('#investigator-table').DataTable( {
    	"responsive":true,
    	"processing": true,
     "order": [[ 1, "asc" ]],
      "serverSide": true,
      "pagingType": "full_numbers",
      "jQueryUI": true,
      "ajax": "crosstalk/investigators" ,
      "language": {
          "infoFiltered": "(_MAX_ ms)",
        },
        "columns": [
                    {
                        "class":          "details-control",
                        "orderable":      true,
                        "data":           null,
                        "defaultContent": ""
                    },
                    
                    { "data": "name" },
                    { "data": "piId" },
                    { "data": "role" },
                    { "data": "organization.name" },
                    { "data": "organization.department" }
                            
                    ]
        
      });
   
    var table3 = $('#organization-table').DataTable( {
    	"responsive":true,
    	"processing": true,
     "order": [[ 1, "asc" ]],
      "serverSide": true,
      "pagingType": "full_numbers",
      "jQueryUI": true,
      "ajax": "crosstalk/organizations",
        "columns": [
                    {
                        "class":          "details-control",
                        "orderable":      false,
                        "data":           null,
                        "defaultContent": ""
                    },
                    
                    { "data": "duns" },
                    { "data": "name" },
                    { "data": "department" },
                    { "data": "city" },
                    { "data": "state" },
                    { "data": "zipcode" },
                    { "data": "country" },
                    { "data": "fips" }
                            
                    ]	
      });
    var table4 = $('#keyword-table').DataTable( {
    	"responsive":true,
    	"processing": true,
     "order": [[ 0, "asc" ]],
      "serverSide": true,
      "pagingType": "full_numbers",
      "jQueryUI": true,
      "ajax": "crosstalk/keywords" ,
        "columns": [
                    {
                        "class":          "details-control",
                        "orderable":      true,
                        "data":           null,
                        "defaultContent": ""
                    },
                    
                    { "data": "term" },
                   
                    
                            
                    ]
        
      });
    var popen;
    // Add event listener for opening and closing details
    $('#grant-table tbody').on('click', 'td.details-control', function () {
    	console.log("sdgsgsf");
        var tr = $(this).closest('tr');
        var row = table.row( tr );

 		
 		//$(".shown").removeClass("shown");
        if ( row.child.isShown() ) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
            popen=undefined;
        }
        else {
            // Open this row
            row.child( format(row.data()) ).show();
            if(popen){
            	popen.removeClass('shown');
            	table.row(popen).child.hide();
            }
            tr.addClass('shown');
            popen=tr;
        }
    });
  });

function getKeywords(d){
    //console.log(JSON.stringify(d));	
	var keys="";
	
   // var projectTermsArr = [d];
    for(var i=0; i<d.length; i++){
    	if(i!=0){
    		keys+=", "; 
    	}
	var proj= d[i];
	keys+= proj.term;
    }
	console.log(keys);

	return keys;
}
//}
    /* Formatting function for row details - modify as you need */
    function format ( d ) {
    	var awardNoticeDate  = moment(d.awardNoticeDate).format('MMM DD, YYYY');
    	var budgetStartDate = moment(d.budgetStart).format('MMM DD, YYYY');
    	var budgetEndDate = moment(d.budgetEnd).format('MMM DD, YYYY');
    console.log(JSON.stringify(d.projectTerms));	
var keywords = getKeywords(d.projectTerms);


        return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">'+
            '<tr>'+
                '<td>Abstract:</td>'+ '</tr>'+ '<tr>'+
                '<td class = "phr">'+d.projectAbstract+'</td>'+
            '</tr>'+
            '<tr>'+
            '<td>Keywords:</td>'+ '</tr>'+ '<tr>'+
            '<td>'+keywords+'</td>'+
        '</tr>'+'<tr>'+
        '<td>Award Notice Date:</td>'+'<td>Budget Start:</td>'+'<td>Budget End:</td>'+'<td>Fiscal Year:</td>'+'<td>Arra Funded:</td>'+'<td>Subproject ID:</td>'+ '</tr>'+ '<tr>'+
        '<td>'+awardNoticeDate+'</td>'+'<td>'+budgetStartDate+'</td>'+'<td>'+budgetEndDate+'</td>'+'<td>'+d.fiscalYear+'</td>'+'<td>'+d.isArraFunded+'</td>'+'<td>'+d.subprojectID+'</td>'+
    '</tr>'+'<tr>'+
    '<td>Application Type:</td>'+'<td>Activity:</td>'+'<td>Administering IC:</td>'+'<td>CFDA Code:</td>'+'<td>FOA number:</td>'+ '</tr>'+ '<tr>'+
    '<td>'+d.applicationType+'</td>'+'<td>'+d.activity+'</td>'+'<td>'+d.administeringIc+'</td>'+'<td>'+d.cfdaCode+'</td>'+'<td>'+d.foaNumber+'</td>'+
    '</tr>'+
        '</table>';
        table.fnDraw();
    }
