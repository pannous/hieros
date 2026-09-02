require('/me/dev/js/extensions.js')()
file="hebrew.dict.txt"
hebrews={A:"א",B:"ב",C:"כ",D:"ד",E:"ה",F:"ו",G:"ג",H:"ה",Ḫ:"ח",J:"י",I:"י",CH:"ך",K:"כ",L:"ל",M:"ם",m:"מ",N:"נ",n:"ן",O:"ו",P:"ף",p:"פ",Q:"ק",R:"ר",S:"ש",ß:"צ",T:"ת",TH:"ט",U:"ו",V:"ב",W:"ו",X:"ס",Y:"ע",e:"ע",Z:"ז",TH:"ט",SH:"ש",SCH:"ש",Ei:'"',ei:"יי","`":"","'":""}
// ץ?
function hebrew(text) {
	if(!text)return ""
		text=text.replace("sh","ש")
		text=text.replace("th","ט")
		text=text.replace("ph","ף")
		text=text.replace("tz","צ")
		text=text.replace("uw","ו")
		text=text.replace("iy","י")
		text=text.replace("'","")
		text=text.replace("`","ע")

ok=text.map(c=>hebrews[c.upper()]||c).join("")
ok2=ok.replace('י',"")
return ok2
}

for(line of lines(file)){
row=line.split("\t")
if(row[0].match(/^\d\d\d+/)){
console.log(row[0],row[1],hebrew(row[1]),row[2])
	}else 
	console.log(line)
}