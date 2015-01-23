$(document).ready(function() {
	$("#chartSelect").selectmenu();
	var filters = parseFilters(filterList);
	drawChart(filters[index]);
	$("#chartSelect").on("selectmenuchange", function( event, ui ) {
			index = ui.item.index;
			drawChart(filters[index]);
		});
	
	console.log(search);
	$.extend($.expr[":"], {
		"containsIN": function(elem, i, match, array) {
		return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] || "").toLowerCase()) >= 0;
		}
		});
	
	
	var search = $(location).attr('search');
	if(search !==""){
		search = search.split('?q=');
		search = search[1].split('&')[0];
		search = search.split('%2C+');
		console.log(search);
		if(search.length > 1){
//			search = search.replace("%22","");
			search = search[1].replace("%22", "").replace("+", " ")+' '+ search[0].replace("%22", "").replace("+", " ");
			console.log(search);
		}else{
			search = decodeURI(search[0]);
		}
		
//			
//		search = search[1].split('&')[0];
////		search = decodeURI(search);
//		
//		search = search.split('%2C+');
//		search = search[1].replace("+", " ")+ ' '+ search[0].replace("+", " ");
//		}
//			search = search[1].split('&')[0];
//		}
		$('tbody',  '#publications').highlight(search);

		
		$("div:containsIN('"+search+"')").removeClass('hidden');
		
	}
	
	
	
	
	});

function getLabel(index){
	var facetName;
	switch(index){
	case 0:
		facetName = "Program";
		break;
	case 1:
		facetName = "Journal+Year+Published";
		break;
	case 2:
		facetName = "Author";
		break;
	case 3:
		facetName = "Category";
		break;
	case 4:
		facetName = "MeSH";
		break;
	case 5:
		facetName = "Journal";
		break;
	}
	return facetName;
}

  
function drawChart(facets){
	var labels =[];
	var counts =[];
	var checked =[];
//	console.log(facets);
	for(var i=0; i<facets.length; i++){
		labels.push(facets[i].displayName);
		counts.push(facets[i].count);
		if(facets[i].checked){
			checked.push(i);
		}
	}
	//console.log(checked);
	var max = Math.max.apply(Math, counts);
	var chart =	$("#history-bar").highcharts({
		credits: {
			enabled: false
		},
		legend: {
			enabled: false
		},
		chart: {
			height: 300,
			renderTo: 'container',
			type: 'column',
			zoomType:'x'
		},
		title: {
			text: null
		},
		xAxis: {
			categories: labels,
			labels:{
				overflow: 'justify',
				rotation: 45,
				useHTML: true,
				formatter: function() {
					return '<div class="labelctrl">'+decodeURI(this.value)+'</div>';}
			}
		},
		yAxis: {
			max: max,
			title: {
				text: 'Publication Count'
			}
		},
		plotOptions: {
			column: {
				grouping: false,
				shadow: false,
				states: {
					hover: {
						color: '#cf2b2e'
					}
				}
			},
			series: {
				allowPointSelect: false,
				cursor: 'pointer',
				states: {
					select: {
						color: '#cf2b2e'
					}
				},
				point: {
					events: {
						click: function() {
							var facet;
							var search = $(location).attr('search');
							if(search ===""){
								facet = "?facet="+getLabel(index) + '/' + this.category;

							}else{
								facet = "&facet="+getLabel(index) + '/' + this.category;
							}
							location.href += facet;
						}
					}
				}
			}
		},
		series: [{
			name: 'Publications',
			color: '#1f355c',
			data: counts
		}]
	});
	var chart2 = $("#history-bar").highcharts();
	for(var j in checked){
		chart2.series[0].data[checked[j]].select(true,true);		
}

}

function parseFilters(filters){
	var chartFilters = [];
	var programList = [];
	var journalYearList = [];
	var authorList = [];
	var categoryList = [];
	var meshList = [];
	var journalList =[];
	for (var f in filters){
	//	console.log(filters[f]);
		var name = filters[f].name;
	//	console.log(name);
		var filter = {};
		if(name == "Program"){
			filter = filters[f];
			programList.push(filter);
		}
		if (name == "Journal+Year+Published"){
			filter = filters[f];
				journalYearList.push(filter);
			}
		if (name == "Author"){
			filter = filters[f];
				authorList.push(filter);
			}
		if (name == "Category"){
			filter = filters[f];
				categoryList.push(filter);
			}
		if (name == "MeSH"){
			filter = filters[f];
				meshList.push(filter);
			}
		if (name == "Journal"){
			filter = filters[f];
			journalList.push(filter);
		}
	}
	chartFilters.push(programList);
	chartFilters.push(journalYearList);
	chartFilters.push(authorList);
	chartFilters.push(categoryList);
	chartFilters.push(meshList);
	chartFilters.push(journalList);
return chartFilters;
}