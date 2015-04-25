function tinx_plot(selector, accsInPage) {
    var styleCurveBorder = {"stroke": "rgb(128, 0, 0)", "stroke-width": "1px"};
    var width = $(selector).width() / 1.2;
    var height = width;
    var radius = width * 0.85 / 100;
    var padding = width * 0.2;
    var axisLabelFontSize = 1 * 1.1;
    console.log(axisLabelFontSize);
    d3.json("/idg/tinx", function (json) {

        var nFn = function (d) {
            return d.novelty;
        };
        var iFn = function (d) {
            return d.meanImportance;
        };

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
                .attr("id", function(d) { return "point-"+ d.acc;})
                .attr("class", "point")
                .attr("cx", function (d) {
                    return x(nFn(d));
                })
                .attr("cy", function (d) {
                    return y(iFn(d));
                })
                .attr("r", radius)
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
        var xaxis = d3.svg.axis().scale(x).orient("bottom").ticks(5);
        var yaxis = d3.svg.axis().scale(y).orient("left").ticks(5);
        svg.append("g").attr("class", "axis").attr("transform", "translate(0," + (height) + ")").call(xaxis);
        svg.append("g").attr("class", "axis").attr("transform", "translate(" + (padding) + ",0)").call(yaxis);

        // axis labels
        svg.append("text")
                .attr("transform", "translate(" + (padding + (width - padding) / 2) + "," + (height+padding*0.8) + ")")
                .attr("text-anchor", "middle")
                .text("Novelty").style('font-size', axisLabelFontSize + 'em')
                .style('font-family', 'sans-serif').style("fill", "#000000");
        svg.append("text")
                .attr("transform", "translate(" + (padding*0.2) + "," + ((height+padding) / 2) + ")rotate(-90)")
                .attr("text-anchor", "middle")
                .text("Importance").style('font-size', axisLabelFontSize + 'em')
                .style('font-family', 'sans-serif').style("fill", "#000000");


    });
}