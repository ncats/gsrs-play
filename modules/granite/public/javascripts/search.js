var table;

$(document).ready(function() {
	$('#diseaseSearch').on("click", function(e) {
		getInput();
		
		search(q);
		
	});
});
//gets the user submitted disease
function getInput() {
	q = $('#searchInput').val();
	if (!q || q == null || q.length == 0) {
		return false;
	}

}

function search (q){ 

	theUrl = "crosstalk/api/search?q=" + q
	+ "%20AND%20kind:crosstalk.ncats.models.Grant";
	//console.log(theUrl);
	$.ajax({
		type : 'GET',
		url : theUrl, // This is a URL on your website.
		success : function(res, status, xhr) {
			console.log((res));
				 $('#searchTable').DataTable( {
				"responsive":true,
				"processing": true,
				"order": [[ 1, "asc" ]],
						"pagingType": "full_numbers",
				"jQueryUI": true,
				"data": res.data,
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
		}
		});
		}