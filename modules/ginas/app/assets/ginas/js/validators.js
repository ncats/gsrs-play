//USAGE
//
//var valid=codeSystemValidators.validate("CAS","50-00-0");
// OR
//var valid=codeSystemValidators.getValidatorFor("CAS")("50-00-0");
//

var codeSystemValidators={};
codeSystemValidators.defaultValidator = function(codeSystem){
	console.log("No validator for:" + codeSystem + ", using default");
	return function(code){
		//TODO: make more robust?
		return true;
	}
}
codeSystemValidators.makeKey = function(codeSystem){
	return "VAL" + codeSystem.replace(/[ .-]/g,"");
}
codeSystemValidators.getValidatorFor = function(codeSystem){
	var f= codeSystemValidators[codeSystemValidators.makeKey(codeSystem)];
	if(!f){
		return codeSystemValidators.defaultValidator(codeSystem);
	}
	return f;
}
codeSystemValidators.putValidatorFor = function(codeSystem, fun){
	codeSystemValidators[codeSystemValidators.makeKey(codeSystem)]=fun;
}
codeSystemValidators.validate = function(codeSystem, code){
	return codeSystemValidators.getValidatorFor(codeSystem)(code);
}


//**********************************************
//Add validators for specific code systems here
//**********************************************
codeSystemValidators.putValidatorFor("CAS",function(cas){
	var c = (cas.replace(/[-| ]/g,"")-0)+"";

	if(c === "NaN"){
		return false;
	}else{
		var p=1;
		var sum=0;
		for(var i=c.length-2;i>=0;i--){
			sum+=c[i]*p;
			p++;
		}
		var check=sum%10;
		if(check === (c[c.length-1]-0)){
			return true;
		}
	}
	return false;
});

