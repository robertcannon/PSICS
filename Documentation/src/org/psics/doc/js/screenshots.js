
window.onload = myinit;


var ssdiv = null;
var ssimg = null;

function myinit() {
	for (var i = 1; i <= 9; i++) {
		document.getElementById("ss" + i).onmouseup = ssShow(i);
	}

	var lpos = getPosX(document.getElementById("ss1"));

	ssdiv = document.createElement("div");
	ssimg = document.createElement("img");
	ssdiv.appendChild(ssimg);
	ssdiv.style.position = "absolute";
	ssdiv.style.display = "none";
	ssdiv.style.width = "auto";
	ssdiv.style.marginLeft = -(lpos - 40);
	ssdiv.style.height = "auto";
	ssdiv.style.padding = "4px";
	ssdiv.style.backgroundColor = "#f0f0f0";
	ssdiv.style.border = "2px solid #606060";
	ssdiv.style.zIndex = "100";

}

function hideBig() {
	ssdiv.onmouseup = null;
	if (ssdiv.parentNode) {
		ssdiv.parentNode.removeChild(ssdiv);
	}
	ssimg.src = "http://images.textensor.com/wideloading.gif";
	ssdiv.style.display = "none";
}

function showBig(n) {
	ssimg.src = "shot" + n + ".png";
	var ctr = document.getElementById("ctr" + n);
	ssdiv.onmouseup = hideBig;
	ctr.insertBefore(ssdiv, ctr.firstChild);
	ssdiv.style.display = "block";
	var lpos = getPosX(document.getElementById("ss1"));
	ssdiv.style.marginLeft = -(lpos - 40);
}



function ssShow(ind) {
	var myidx = ind;
	function showFn() {
		hideBig();
		setTimeout(reallyShow, 50);
	}
	function reallyShow() {
		showBig(myidx);
	}
	return showFn;
}


 function getPosX(obj) {
    var curleft = 0;
    if (obj.offsetParent) {
        while (obj.offsetParent) {
        curleft += obj.offsetLeft;
        obj = obj.offsetParent;
        }
    } else if (obj.x) {
        curleft += obj.x;
    }
    return Number(curleft);
}



