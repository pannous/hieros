function log(x) {
	console.log(x);
	suggestion.innerHTML = x
}
var clicked=0
function selectClick(event) {
	clicked=true
	group = event.target.innerText
	suggestion.innerHTML = group;//+" "+Gardiner_Map[group]+" "
	if (group)// && !
		checkGardiner(group)
}

function insertClick(event) {
	// glyph=event.target.innerHTML.replace(/\w*/,"")
	glyph = event.target.innerText
	if (glyph.length > 10)  suggestion.innerHTML = ""// Heather selected
	else {
		glyph = glyph.replace(/\w*/, "")
		// glyph=event.target.text.replace(/\w*/,"")
		taxt.value = taxt.value.replace(/[A-Z]+/, " ") + glyph
		taxt.focus()
	}

}

// $("finder").autocomplete(autocompletions);
function checkGardiner(glyph0) {
	try {
		if (glyph0.length > 50) {
			suggestion.innerHTML = ""// Heather selected
			return
		}
		glyph = glyph0.replace(/[^a-zA-Z\d\t\w\s]/g, "")
		glyph = glyph.trim().toUpperCase()
		if (glyph.length < 1) return;
		suggest = "<table><tr>" + glyph+ " "+(Gardiner_Map[glyph]||"");//""
		count = 0
		var sign1=""
		for (a in Gardiner_Map) {
			qi = new RegExp("^" + glyph + "\\d", "i")//
			matches = a.match(qi) || a == q || a.startsWith(glyph)
			// matches=(a+"").startsWith(glyph)
			if (matches && count++ < 180) {
				sign=Gardiner_Map[a]
				sign1=sign1||sign
				console.log(a+":"+ sign);
				suggest += "<td  title="+a+ ">" + a.replace(/[A-Z]+/, "") + (sign||"") + "</td>"
				if (count % 8 == 0)suggest += "</tr><tr>"
			}
		}
		suggest += "</tr></table>"
		uniq = finder.value.match(/ $/) 
		uniq =uniq || glyph0.match(/ $/) && count==1
		// console.log(suggest);
		if (uniq) {
			if (Gardiner_Map[glyph] || sign1) {
				finder.value = finder.value.replace(/\w+/, "")
				taxt.value += Gardiner_Map[glyph] || sign1
				suggestion.innerHTML = ""
			} else{
					// if(clicked)taxt.value +=glyph
					suggestion.innerHTML = ""
					clicked=0
			}
		} else {
			suggestion.innerHTML = suggest
		}
	} catch (x) {
		log(x)
	}
}


function postText(h, text) {
	try {
		var http = new XMLHttpRequest();
		http.open("GET", "/add?h=" + h + "&x=" + text, true);
		http.send();
	} catch (x) {
		log(x)
	}
}