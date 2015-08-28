
/**
 *   Author: Tyler Peryea
 *   Snapshot overlay -- useful for taking screenshots
 *   (e.g. error reports, capturing image for image processing)
 *
 *
 */

/**
 *   getScreenshotArea
 *
 *   callback will receive an area object with this structure:
 *       {x:0,y:0,height:10,width:10,zoom:1}
 *       (zoom captures the current zoomed state of the window)
 *
 *   titlet is the title string
 *   showHints is boolean to show hint messages
 *   hints is an array of 3 hint messages to show
 *
 */
    function getScreenshotArea(callback, titlet, showHints, hints) {
    var title = titlet;
    if (title === undefined) {
        title = "Area Select";
    }
    var initc = "";
    var startc = "";
    var endc = "";
    var tc = "";
    var snapListen = false;
    var resizing = false;
    var fullDone = false;
    var dragSelection = true;
    var selType = "moving";
    var overlays = [];
    var myrect;
    var mycoord;
    var nid;
    var note1;
    var note2;
    var help;
    var fadeStart = !showHints;
    var fadeNotes = function() {
        if (!fadeStart) {
            fadeStart = true;
            var timerID;
            ntime = new Date();
            timerID = setInterval(function() {
                var ctime = (new Date() - ntime) / 20;
                if (ctime / 10 > 1) {
                    clearInterval(timerID);
                    note1.style.display = "none";
                    note2.style.display = "none";
                    note3.style.display = "none";
                    note1.style.opacity = 0;
                    note2.style.opacity = 0;
                    note3.style.opacity = 0;
                    help.style.display = "block";
                }
                note1.style.opacity = 1 - Math.min(ctime / 10, 1);
                note2.style.opacity = 1 - Math.min(ctime / 10, 1);
                note3.style.opacity = 1 - Math.min(ctime / 10, 1);
            }, 30);
        }

    };
    var fadeyElm = function(elm, cback) {
        ntime = new Date();
        var nid3 = setInterval(function() {
            var ctime = (new Date() - ntime) / 50;
            var amp = 100 / (Math.pow(ctime, 1.6));
            if (amp < 2) {
                clearInterval(nid3);
                ntime = new Date();
                cback();
            }
            var marg = Math.floor(amp * Math.sin(ctime));
            elm.style.marginRight = marg + "px";
            elm.style.marginLeft = marg + "px";
            elm.style.opacity = Math.min(ctime / 10, 1);
        }, 30);
    };
    var showNotes = function() {
        fadeStart = false;
        note1.style.display = "block";
        note2.style.display = "block";
        note3.style.display = "block";
        note1.style.opacity = 0;
        note2.style.opacity = 0;
        note3.style.opacity = 0;
        help.style.display = "none";
        fadeyElm(note2, function() {
            fadeyElm(note1, function() {
                fadeyElm(note3, function() {

                });
            });
        });
    };

    var unloadfunc = function(torem) {
        fullDone = true;
        startc = undefined;
        snapListen = false;
        var elm1 = myrect;
        var elm2 = mycoord;
        var elm3 = overlays[0];
        var elm4 = overlays[1];
        var elm5 = overlays[2];
        var elm6 = overlays[3];

        elm1.parentNode.removeChild(elm1);
        elm2.parentNode.removeChild(elm2);
        elm3.parentNode.removeChild(elm3);
        elm4.parentNode.removeChild(elm4);
        elm5.parentNode.removeChild(elm5);
        elm6.parentNode.removeChild(elm6);



        document.body.removeEventListener('click', torem);
        document.body.removeEventListener('keyup', torem);
    };
    //Drag area should probably be default
    dragSelection = true;
    var eventAdded = function(e) {
        if (e.keyCode == 27) {
            e.preventDefault();
            unloadfunc(arguments.callee);
        }
    };
    document.body.addEventListener("keyup", eventAdded);
    var getPoint = function(e) {
        x = e.clientX;
        y = e.clientY;
        tc = {
            x: x,
            y: y
        };
        clearInterval(nid);
        fadeNotes();
        return tc;
    };

    var positionWindow = function(tc, oc, commit) {
        var w = tc.x - oc.x;
        var h = tc.y - oc.y;

        overlays[0].style.width = oc.x + "px";
        overlays[0].style.height = (oc.y + h) + "px";

        overlays[1].style.width = "125%";
        overlays[1].style.height = oc.y + "px";
        overlays[1].style.top = "0px";
        overlays[1].style.left = oc.x + "px";

        overlays[2].style.width = "125%";
        overlays[2].style.height = "125%";
        overlays[2].style.top = oc.y + "px";
        overlays[2].style.left = (oc.x + w) + "px";

        overlays[3].style.width = (oc.x + w) + "px";
        overlays[3].style.height = "125%";
        overlays[3].style.top = (oc.y + h) + "px";
        overlays[3].style.left = "0px";

        myrect.style.top = oc.y + "px";
        myrect.style.left = oc.x + "px";

        myrect.style.width = w + "px";
        myrect.style.height = h + "px";

        if (commit) {
            startc = {};
            startc.x = oc.x;
            startc.y = oc.y;
            endc = {};
            endc.x = tc.x;
            endc.y = tc.y;
        }
        return oc.x + "," + oc.y + "-" + tc.x + "," + tc.y;
    };
    var setArea = function(oc) {
        var tc = {
            x: oc.x2,
            y: oc.y2
        };
        var oc2 = {
            x: oc.x,
            y: oc.y
        };
        positionWindow(tc, oc2, true);
    };
    var nudgeWindow = function(dx, dy) {
        console.log("nudging");
        var oc = {};
        var tc = {};
        oc.x = startc.x + (dx);
        oc.y = startc.y + (dy);
        tc.x = endc.x + (dx);
        tc.y = endc.y + (dy);
        positionWindow(tc, oc, true);
    };
    document.body.addEventListener("mousemove", function myFunction(e) {
        if (snapListen) {
            //console.log("resizing");
            tc = getPoint(e);
            var minWidth = 15;
            var minHeight = 15;
            //copy object
            var oc = JSON.parse(JSON.stringify(startc));


            if (selType == "moving") {
                var mx = tc.x - initc.x;
                var my = tc.y - initc.y;
                oc.x = startc.x + (mx);
                oc.y = startc.y + (my);
                tc.x = endc.x + (mx);
                tc.y = endc.y + (my);
            } else {
                if (tc.x < oc.x + minWidth) {
                    tc.x = oc.x + minWidth;
                }
                if (tc.y < oc.y + minHeight) {
                    tc.y = oc.y + minHeight;
                }
            }
            mycoord.style.top = (tc.y + window.pageYOffset - 13) + "px";
            mycoord.style.left = (tc.x + window.pageXOffset - 13) + "px";
            if (startc !== undefined) {
                coor = positionWindow(tc, oc);
            } else {

                coor = tc.x + "," + tc.y;

            }

            document.getElementById("coordText").innerHTML = coor;
        } else {
            console.log("no listen");
            if (fullDone) {
                document.body.removeEventListener('mousemove', arguments.callee);
            }
        }
        ////console.log(coor);
    }, false);

    var selectionEvent = function(e) {
        if (snapListen) {
            tc = getPoint(e);
            if (startc === undefined) {
                startc = tc;
                myrect.style.top = (tc.y + window.pageYOffset) + "px";
                myrect.style.left = (tc.x + window.pageXOffset) + "px";
            } else {
                if (resizing) {
                    resizing = false;
                } else {
                    if (selType == "moving") {
                        var mx = tc.x - initc.x;
                        var my = tc.y - initc.y;
                        startc.x = startc.x + (mx);
                        startc.y = startc.y + (my);
                        endc.x = endc.x + (mx);
                        endc.y = endc.y + (my);
                    } else if (selType != "confirm") {
                        endc = tc;
                    }
                    var rect = {
                        x: startc.x,
                        y: startc.y,
                        height: (endc.y - startc.y),
                        width: (endc.x - startc.x)
                    };
                    snapListen = false;
                    selType = "done";

                    if (fullDone) {
                        unloadfunc(arguments.callee);
                        setTimeout(function() {
                            rect.zoom = window.devicePixelRatio;
                            callback(rect);
                        }, 10);
                    }
                }

            }
        } else {

        }
    };
    if (!dragSelection) {
        document.body.addEventListener("click", selectionEvent, false);
    } else {
        document.body.addEventListener("mouseup", selectionEvent, false);
        document.body.addEventListener("mousedown", selectionEvent, false);
    }
    var tut = {};
    var okbutton = "<button class = 'btn btn-default' style='bottom: -35px; right: 0px; position:absolute;' id='confirmSelect'>Process Screenshot <i class='fa fa-camera'></i></button>";
    var cancelbutton = "<button type='button' class='close' id='cancelSelect'  aria-label='Close' style='z-index: 2999999;position: absolute;right: 5px;'><span aria-hidden='true'>&times;</span></button>";
//    var cancelbutton = "<button class = 'btn btn-default' style='position: absolute;top: -31px;right: 0px;z-index: 2999999;display: inline-block;line-height: normal;white-space: nowrap;vertical-align: baseline;text-align: center;cursor: pointer;padding: 6px;color: white;border-radius: 4px;text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);background: rgb(223, 0, 0);border: 0 rgba(0,0,0,0);font-size: 10pt;font-weight: bold;' id='cancelSelect'>X</button>";
    var resizeDiv4 = "<div id='botrightresize' style='cursor: se-resize;position: absolute;bottom: -16px;right: -16px;width: 32px;height: 32px;background-image: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAA3klEQVR42mMwNjZm1dLSYlNRUWEH0SDs6enJTowYSC8DiBEaGspZWFjI6ePjwwWiQXxixBISHDjABpCjGcRPSEiAGECOZhQDSNHMzMx8mpeX9/K5c+ek4AaQYjNIs6qq6qsHDx5YoRhArLOPHDkiBdK8fv16AfoEIrKfQWIsLCxgPsglRAUisp9BYjD+y5cvLYkKRGQ/w/ggzfPnzxcgKxAJpkSgn0/h8jNSmFxBCQNi/QxSB+IDM9Trp0+fWmENRHx+BqkD8UGa4WEAypIODg4cubm57CABGJs4MU92AHZ4N7/7k4lSAAAAAElFTkSuQmCC\");background-repeat: no-repeat;'></div>";
    var styleov2 = 'border:none;cursor:crosshair;width:0;height:0;position:absolute;top:0px;left:0px;background:rgba(0,0,0,0.25);font-size:20pt;color:white;z-index:999999 !important;text-align:center;position:fixed;';
    tut = document.createElement("DIV");
    tut.setAttribute("style", styleov2);
    tut.id = "overlay-1";
    overlays[0] = tut;
    document.body.appendChild(tut);

    tut = document.createElement("DIV");
    tut.setAttribute("style", styleov2);
    tut.id = "overlay-2";
    overlays[1] = tut;
    document.body.appendChild(tut);

    tut = document.createElement("DIV");
    tut.setAttribute("style", styleov2);
    tut.id = "overlay-3";
    overlays[2] = tut;
    document.body.appendChild(tut);

    tut = document.createElement("DIV");
    tut.setAttribute("style", styleov2);
    tut.id = "overlay-4";
    overlays[3] = tut;
    document.body.appendChild(tut);

    var econt = document.createElement("DIV");
    myrect = document.createElement("DIV");
    myrect.id = "myrect";
    myrect.setAttribute("style", "margin:0;font-size: 12pt;-webkit-box-sizing: initial;background:none;cursor:move;position:fixed;border:1px dashed black;top:0px;left:0px;width:0px;height:0px");
    myrect.innerHTML = resizeDiv4 + okbutton + cancelbutton;

    if(hints!==undefined){
        note1 = document.createElement("DIV");
        note1.setAttribute("style", "opacity:0;width: 350px;    color: rgb(255, 255, 255);text-align:center;    right: -360px;    position: absolute;    background: rgba(0, 0, 0, 0.71);    padding: 5px;    font-family: sans-serif;    font-size: 13pt;    bottom: 0;    margin: 0;");


        note1.innerHTML = hints[1]; //"2. Resize the box to surround the full structure";
        myrect.appendChild(note1);
        note2 = document.createElement("DIV");
        note2.setAttribute("style", "opacity:0;width: 350px;    color: rgb(255, 255, 255);text-align:center;    left: -360px;    position: absolute;    background: rgba(0, 0, 0, 0.71);    padding: 5px;    font-family: sans-serif;    font-size: 13pt;    top: 0;    margin: 0;");
        note2.innerHTML = hints[0]; //"1. Move the box to the chemical structure";
        myrect.appendChild(note2);
        note3 = document.createElement("DIV");
        note3.setAttribute("style", "opacity:0;width: 120px;    color: rgb(255, 255, 255);text-align:center;    left: -130px;    position: absolute;    background: rgba(0, 0, 0, 0.71);    padding: 5px;    font-family: sans-serif;    font-size: 13pt;    bottom: 0;    margin: 0;");
        note3.innerHTML = hints[2]; //"3. Click 'process'";
        myrect.appendChild(note3);

        help = document.createElement("BUTTON");
        help.setAttribute("style", "display: block;width: 25px;color: white;right: -25px;position: absolute;padding: 6px;font-family: sans-serif;font-size: 10pt;top: 0px;margin: 0px;line-height: normal;white-space: nowrap;vertical-align: baseline;text-align: center;cursor: pointer;border-top-left-radius: 4px;border-top-right-radius: 4px;border-bottom-right-radius: 4px;border-bottom-left-radius: 4px;text-shadow: rgba(0, 0, 0, 0.2) 0px 1px 1px;border: 0px rgba(0, 0, 0, 0);font-weight: bold;background: rgba(0, 0, 0, 0.4);");
        help.innerHTML = "?";
        myrect.appendChild(help);
    }


    mycoord = document.createElement("DIV");
    mycoord.id = "mycoord";
    mycoord.setAttribute("style", "overflow:hidden;padding:0px;margin:0px;position:absolute;opacity:0.0;top:0px;left:0px;width:1px;height:1px");
    mycoord.innerHTML = "<div id='coordText'></div>";
    econt.appendChild(myrect);
    econt.appendChild(mycoord);
    document.body.appendChild(econt);

    myrect.style.zIndex = 999999;
    mycoord.style.zIndex = 1999999;
    myrect.addEventListener("mousedown", function(e) {
        if (selType != "resizing") {
            snapListen = true;
            resizing = true;
            initc = getPoint(e);
            selType = "moving";
            e.preventDefault();
            return false;
        }
    }, false);
    document.getElementById("confirmSelect").style.zIndex = 2999999;
    document.getElementById("confirmSelect").addEventListener("click", function(e) {
        snapListen = true;
        fullDone = true;
        resizing = false;
        selType = "confirm";
        selectionEvent(e);
    }, false);
    document.getElementById("cancelSelect").style.zIndex = 2999999;
    document.getElementById("cancelSelect").addEventListener("click", function(e) {
        unloadfunc(eventAdded);
    }, false);
    document.getElementById("botrightresize").addEventListener("mousedown", function(e) {
        endc = undefined;
        e.preventDefault();
        snapListen = true;
        resizing = true;
        selType = "resizing";
        console.log("down");
        return false;
    }, false);

    var wstart = window.innerWidth / 2;
    var hstart = window.innerHeight / 2;

    setArea({
        x: wstart - 100,
        y: hstart - 100,
        x2: wstart + 100,
        y2: hstart + 100
    });
    var istart = JSON.parse(JSON.stringify(startc));
    var iend = JSON.parse(JSON.stringify(endc));
    var ntime = new Date();


    nid = setInterval(function() {
        var ctime = (new Date() - ntime) / 50;
        var amp = 100 / (Math.pow(ctime, 1.6));
        if (amp < 2) {
            clearInterval(nid);
            if (showHints) {
                showNotes();
            }
        }
        var npos1 = {
            x: (istart.x),
            y: (istart.y + amp * Math.sin(ctime))
        };
        var npos2 = {
            x: (iend.x),
            y: (iend.y + amp * Math.sin(ctime))
        };
        positionWindow(npos2, npos1, true);
    }, 30);

    help.addEventListener("click", function(e) {
        showNotes();
    }, false);

}

function getScreenshot(callback){
    getScreenshotArea(function(area){
        html2canvas(document.body, {
            onrendered: function(canvas) {
                console.log("image rendered");
                var econt=document.createElement("CANVAS");
                console.log(econt);
                econt.width=area.width;
                econt.height=area.height;
                //document.body.appendChild(econt);
                var canv=econt.getContext('2d');
                console.log(canv);
                canv.drawImage(canvas,-area.x,-area.y);
                $('#report').modal();
                $('#thumb')
                    .attr('src', econt.toDataURL())
                    .width(150)
                    .height(200);
                $('.file-button').hide();
//                callback(econt.toDataURL());

            }
        });
    },"Screenshot selection tool");
}
