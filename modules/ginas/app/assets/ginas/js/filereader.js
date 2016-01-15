var FileContentsGrabber = {
    getFileContents: function (callback) {
        var b = this;
        this.grabFile(function (file) {
            b.importFile(file, callback);
        });
    },
    grabFile: function (callback) {
        //should always work, theoretically

        var ip = document.createElement("INPUT");
        ip.setAttribute("style", "display:none");
        ip.type = "file";
        ip.name = "file";
        ip.accept = "chemical/*";
        document.body.appendChild(ip);
        ip.click();
        ip.addEventListener('change', function (evt) {
            document.body.removeChild(ip);
            callback(evt.target.files[0]);
        }, false);
    },
    importFile: function (afile, callback) {
        //This may only work if filereader is supported


        var reader = new FileReader();
        reader.onerror = function (e) {
            alert("Error reading file");
        };
        reader.onprogress = function (e) {

        };
        reader.onabort = function (e) {
            alert('File read cancelled');
        };
        reader.onloadstart = function (e) {

        };
        reader.onload = function (e) {
            callback(e.target.result);
        };

        reader.readAsText(afile);
    },
    append: function (target) {
        this.getFileContents(function (txt) {
            $('#' + target).append(txt);
            $('#' + target).trigger("input");
        });
    },
    set: function (target, cback) {
        this.getFileContents(function (txt) {
            $('#' + target).val(txt);
            $('#' + target).trigger("input");
            if(cback){
                cback();
            }else{
                    $('#' + target + "-monitor").val("set" + Math.random());
                    $('#' + target + "-monitor").trigger("input");
            }
            
        });
    },
    moldata: function (target) {
        var mol = $('#' + target).append(txt);
        $('#' + target).trigger("input");
    }
};


function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();

        reader.onload = function (e) {
            $('#thumb')
                .attr('src', e.target.result)
                .width(150)
                .height(200);
        };

        reader.readAsDataURL(input.files[0]);
    }
}
function other(value) {
    if (value == "Other") {
        $('#def-group').show();
    }
    else {
        $('#def-group').hide();
    }
}
function molimport(moldata) {
    console.log(moldata);
    mol.setMolfile(moldata);
}



