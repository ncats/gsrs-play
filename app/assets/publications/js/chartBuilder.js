jQuery(document).ready(function ($) {
	index ="Program";
	$("#chartSelect").selectmenu();
	var filters = parseFilters(filterList);
//	console.log(filters);
	drawChart(filters.Program);
	$("#chartSelect").on("selectmenuchange", function( event, ui ) {
	//	console.log(ui);
		index = ui.item.label;
		if(index =="Journal Year Published"){
			index = "JournalYear";
		} 
		if(index =="Keyword"){
			index = "MeSH";
		}
		console.log(index);
		drawChart(filters[index]);
	});

//	HIGHLIGHTING//	
	$.extend($.expr[":"], {
		"containsIN": function(elem, i, match, array) {
			return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] || "").toLowerCase()) >= 0;
		}
	});


	var search = $(location).attr('search');
	if(search !=="" && search !="undefined"){
		search = search.split('?q=');
		if(search.length > 1){
			search = search[1].split('&')[0];
		}
		if(search.length > 1){
			search = search.split('%2C+');
		}
		if(search.length > 1){
			search = search[1].replace("%22", "").replace("+", " ")+' '+ search[0].replace("%22", "").replace("+", " ");
		}else{
			search = decodeURI(search[0]);
		}
		$('tbody',  '#publications').highlight(search);
		$("div:containsIN('"+search+"')").removeClass('hidden');

	}

});
//END HIGHLIGHTING//	


function drawChart(facets){
	var labels =[];
	var counts =[];
	var checked =[];
//	console.log(facets);
	for(var i in facets){
		labels.push(facets[i].displayName);
		counts.push(facets[i].count);
		if(facets[i].checked){
			checked.push(i);
		}
	}
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
						color: '#642F6C'
					}
				}
			},
			series: {
				allowPointSelect: false,
				cursor: 'pointer',
				states: {
					select: {
						color: '#642F6C'
					}
				},
				point: {
					events: {
						click: function() {
//							console.log(index);
							var facet;
							var search = $(location).attr('search');
							if(!this.selected){
								if(search ===""){
									facet = "?facet="+index + '/' + this.category;
								}else{
									facet = "&facet="+index + '/' + this.category;
								}
							location.href += facet;

							}else{
								var url =  $(location).attr('href');
//								console.log(search);
								if(checked.length == 1){
//									console.log( $(location).attr('origin')+$(location).attr('pathname'));
									location.href = $(location).attr('origin')+$(location).attr('pathname');
								}else{
									if(this.index === 0){
									facet = "facet="+index + '/' + this.category + '&';
								}else{
									facet = "&facet="+index + '/' + this.category;
								}
//								console.log(facet);
								search = url.replace((facet),"");
//								console.log(search);
								location.href= search;
								}
							}
						}
					}
				}
			}
		},
		series: [{
			name: 'Publications',
			color: '#2b5b6d',
			data: counts
		}]
	});
	var chart2 = $("#history-bar").highcharts();
	for(var j in checked){
		chart2.series[0].data[checked[j]].select(true,true);		
	}

}

function parseFilters(filters){
//	console.log(filters);
	var chartFilters = {};
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
	chartFilters.Program =programList;
	chartFilters.JournalYear =journalYearList;
	chartFilters.Author= authorList;
	chartFilters.Category = categoryList;
	chartFilters.MeSH= meshList;
	chartFilters.Journal = journalList;
	return chartFilters;
}