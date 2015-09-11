function highlightTargetTable(elem) {
    var acc = $(elem).attr("id").split("-")[1];
    $("#row-" + acc).addClass("active");
}
function unhighlightTargetTable(elem) {
    var acc = $(elem).attr("id").split("-")[1];
    $("#row-" + acc).removeClass("active");
}


function _tinx_target_plot(json, selector) {

    var nFn = function (d) {
        return json.novelty;
    };
    var iFn = function (d) {
        return d.imp;
    };

    var styleCurveBorder = {"stroke": "rgb(128, 0, 0)", "stroke-width": "1px"};
    var width = $(selector).width() / 1.2;
    var height = width / 1.618;
    var radius = 1.1 * width / 100;
    var padding = width * 0.1;
    var axisLabelFontSize = 1 * 1.1;


    var y = d3.scale.log().domain(d3.extent(json.importances, iFn).reverse()).range([padding, height]).nice();
    var x = d3.scale.log().domain([json.novelty, json.novelty]).range([padding, width]).nice();

    var svg = d3.select(selector).append("svg:svg")
        .attr("width", width + padding)
        .attr("height", height + padding)
        .append("g");

    var div = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    // points
    svg.selectAll("circles")
        .data(json.importances).enter()
        .append("a").attr("xlink:href", function (d) {
            return "/idg/diseases/" + d.doid;
        }).attr("target", "_blank")
        .append("svg:circle")
        .attr("id", function (d) {
            return "point-" + d.doid;
        })
        .attr("class", "point")
        .attr("cx", function (d) {
            return x(nFn(d));
        })
        .attr("cy", function (d) {
            return y(iFn(d));
        })
        .attr("r", radius)
        //.on("mouseover", function (d) {
        //    div.transition()
        //        .duration(200)
        //        .style("opacity", .9);
        //    div .html(d.doid + "<br/>")
        //        .style("left", (d3.event.pageX) + "px")
        //        .style("top", (d3.event.pageY - 28) + "px");
        //})
        //.on("mouseout", function () {
        //    div.transition()
        //        .duration(500)
        //        .style("opacity", 0);
        //});
        .append("svg:title").text(function (d) {
            return d.doid;
        });

    // TODO get better tooltips for the point
    //$('svg circle').tipsy({
    //    gravity: 'w',
    //    html: true,
    //    title: function() {
    //        var d = this.__data__;
    //        return '<span>'+ d.doid+'</span>';
    //    }
    //});

    // axes
    var xaxis = d3.svg.axis().scale(x).orient("bottom").ticks(0);
    var yaxis = d3.svg.axis().scale(y).orient("left").ticks(0);
    svg.append("g").attr("class", "axis").attr("transform", "translate(0," + (height) + ")").call(xaxis);
    svg.append("g").attr("class", "axis").attr("transform", "translate(" + (padding) + ",0)").call(yaxis);

    // axis labels
    svg.append("text")
        .attr("transform", "translate(" + (padding + (width - padding) / 2) + "," + (height + padding * 0.8) + ")")
        .attr("text-anchor", "middle")
        .text("Novelty").style('font-size', axisLabelFontSize + 'em')
        .style('font-family', 'sans-serif').style("fill", "#000000");
    svg.append("text")
        .attr("transform", "translate(" + (padding * 0.3) + "," + ((height + padding) / 2) + ")rotate(-90)")
        .attr("text-anchor", "middle")
        .text("Importance").style('font-size', axisLabelFontSize + 'em')
        .style('font-family', 'sans-serif').style("fill", "#000000");

}

function _tinx_plot(json, selector, accsInPage, mouseOverFn, mouseOutFn) {
    var styleCurveBorder = {"stroke": "rgb(128, 0, 0)", "stroke-width": "1px"};
    var width = $(selector).width() / 1.2;
    var height = width;
    var radius = 1.1 * width / 100;
    var padding = width * 0.2;
    var axisLabelFontSize = 1 * 1.1;


    var y = d3.scale.log().domain(d3.extent(json, iFn).reverse()).range([padding, height]).nice();
    var x = d3.scale.log().domain(d3.extent(json, nFn)).range([padding, width]).nice();

    var svg = d3.select(selector).append("svg:svg")
            .attr("width", width + padding)
            .attr("height", height + padding)
            .append("g");

    // points
    svg.selectAll("circles")
            .data(json).enter()
            .append("a").attr("xlink:href", function (d) {
                return "/idg/targets/" + d.acc;
            }).attr("target", "_blank")
            .append("svg:circle")
            .attr("id", function (d) {
                return "point-" + d.acc;
            })
            .attr("class", "point")
            .attr("cx", function (d) {
                return x(nFn(d));
            })
            .attr("cy", function (d) {
                return y(iFn(d));
            })
            .attr("r", radius)
            .on("mouseover", function () {
                return mouseOverFn(this);
            })
            .on("mouseout", function () {
                return mouseOutFn(this);
            })
            .style("z-index", function (d) {
                if ($.inArray(d.acc, accsInPage) != -1) {
                    return 100;
                } else return 1;
            })
            .style("fill", function (d) {
                if ($.inArray(d.acc, accsInPage) != -1) {
                    return "red";
                } else return "black";
            })
            .append("svg:title").text(function (d) {
                return d.acc;
            });

    // axes
    var xaxis = d3.svg.axis().scale(x).orient("bottom").ticks(0);
    var yaxis = d3.svg.axis().scale(y).orient("left").ticks(0);
    svg.append("g").attr("class", "axis").attr("transform", "translate(0," + (height) + ")").call(xaxis);
    svg.append("g").attr("class", "axis").attr("transform", "translate(" + (padding) + ",0)").call(yaxis);

    // axis labels
    svg.append("text")
            .attr("transform", "translate(" + (padding + (width - padding) / 2) + "," + (height + padding * 0.8) + ")")
            .attr("text-anchor", "middle")
            .text("Novelty").style('font-size', axisLabelFontSize + 'em')
            .style('font-family', 'sans-serif').style("fill", "#000000");
    svg.append("text")
            .attr("transform", "translate(" + (padding * 0.2) + "," + ((height + padding) / 2) + ")rotate(-90)")
            .attr("text-anchor", "middle")
            .text("Importance").style('font-size', axisLabelFontSize + 'em')
            .style('font-family', 'sans-serif').style("fill", "#000000");

}

function tinx_target_plot(selector, acc) {
    d3.json("/idg/tinx/target/"+acc, function(json) {
        console.log(json);
        _tinx_target_plot(json, selector);
    });
}