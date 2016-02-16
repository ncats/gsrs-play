/*

function getDisplayFromCV(domain, value) {
    for (var i in window.CV_REQUEST.content) {
        if (window.CV_REQUEST.content[i].domain === domain) {
            var terms = window.CV_REQUEST.content[i].terms;
            for (var t in terms) {
                if (terms[t].value === value) {
                    return terms[t].display;
                }
            }
        }
    }
    return value;
}

function getCVListForDomain(domain) {
    for (var i in window.CV_REQUEST.content) {
        if (window.CV_REQUEST.content[i].domain === domain) {
            var terms = window.CV_REQUEST.content[i].terms;
            return terms;
        }
    }
    return [{value:"NULL",display:"NO VALUES"}];
}

function vocabsetup(cv) {
    window.CV_REQUEST = cv;
    console.log("finished");
}*/


function submitq(qinput) {
    if (qinput.value.indexOf("\"") < 0 && qinput.value.indexOf("*") < 0 && qinput.value.indexOf(":") < 0 && qinput.value.indexOf(" AND ") < 0 && qinput.value.indexOf(" OR ") < 0) {
        qinput.value = "\"" + qinput.value + "\"";
    }
    return true;
}


window.SDFFields = {};
