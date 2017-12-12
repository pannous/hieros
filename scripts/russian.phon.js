#!/usr/bin/env node 
require('./extensions.js')
map={
"а":"а", // like "a" in car	"ah"
"б":"b", // like "b" in bat	"beh"
"с":"s", // like "s" in sam	"ehs"
"д":"d", // like "d" in dog	"deh"
"э":"e", // like "e" in pet	"eh"
"е":"ė", // Like "ye" in yet	"yeh"
// "е":"ə", // Like "ye" in yet	"yeh"
// "е":"ye", // Like "ye" in yet	"yeh"
"ф":"ph", // like "f" in fat	"ehf"
// "ф":"f", // like "f" in fat	"ehf"
"г":"g", // like "g" in go	"geh"
"ъ":"h",//"hard Sign	Letter before is hard	"tvyordiy"
"х":"ch", // Like "h" in hello or like in Scottish 'loch' or German 'Bach'	"khah"
"и":"i", // Like "ee" in see	"ee"
"й":"j", // i or Y y	like "y" in boy or toy	"ee"
"к":"k", // like "k" in kitten, "c" in cat.		"kah"
"л":"l", // like "l" in light	"ehl"
"м":"m", // like "m" in mat	"ehm"
"н":"n", // like "n" in no	"ehn"
"о":"o", // stressed  Like "o" in bore
// "о":"a", // unstressed: Like "a" in car	"oh"
"п":"p", // like "p" in pot	"peh"
"р":"r", // like "r" in run (rolled)	"ehr"
"з":"s", // like "z" in zoo	"zeh"
// "з":"ß", // like "z" in zoo	"zeh"
"т":"t", // like "t" in tap	"teh"
"у":"u", // like "oo" in boot	"oo"
"в":"v", // like "v" in van	"veh"
"ь":"",//soft Sign	Letter before is soft	"myagkeey"
// "х":"kh", // Like "h" in hello or like in Scottish 'loch' or German 'Bach'	"khah"
// "з":"z", // like "z" in zoo	"zeh"
// "ц":"z", // Like "ts" in bits	"tseh"
"ц":"tz", // Like "ts" in bits	"tseh"
"ч":"ch", // Like "ch" in chip	"cheh"
"ш":"sh",// sh (hard)", // Like "sh" in shut	"shah"
"щ":"sch",// sh (soft)", // Like "sh" in sheep	"schyah"
"ж":"gh", // Like "s" in measure or pleasure "g" in beige (the colour)	"zheh"
// "ж":"zh", // Like "s" in measure or pleasure "g" in beige (the colour)	"zheh"
"ы":"i", // like "i" in ill	"i"
"ю":"ju", // Like "u" in use or university		"yoo"
// "ю":"yu", // Like "u" in use or university		"yoo"
// "я":"ya", // Like "ya" in yard.	"yah"
// "я":"yi", 
"я":"ia", // Like "ya" in yard.	"yah"
// "я":"y", // Like "ya" in yard.	"yah"
// "я":"a", // Like "ya" in yard.	"yah"
"ё":"yo", // like "yo" in yonder	"yo"
//
"А":"А", // Like "a" in car	"ah"
"Б":"B", // Like "b" in bat	"beh"
"С":"S", // Like "s" in sam	"ehs"
"Д":"D", // Like "d" in dog	"deh"
"Э":"E", // Like "e" in pet	"eh"
"Е":"YE", // Like "ye" in yet	"yeh"
"Ф":"F", // Like "f" in fat	"ehf"
"Г":"G", // Like "g" in go	"geh"
"И":"EE", // Like "ee" in see	"ee"
// "Ъ":"H",//"Hard Sign	Letter before is hard	"tvyordiy"
"Х":"Kh", // Like "h" in hello or like in Scottish 'loch' or German 'Bach'	"khah"
"Й":"I", // i or Y y	like "y" in boy or toy	"ee"
"К":"K", // Like "k" in kitten, "c" in cat.		"kah"
"Л":"L", // Like "l" in light	"ehl"
"М":"M", // Like "m" in mat	"ehm"
"Н":"N", // Like "n" in no	"ehn"
"О":"O", // Stressed  Like "o" in bore
"О":"A", // Unstressed: Like "a" in car	"oh"
"П":"P", // Like "p" in pot	"peh"
"Р":"R", // Like "r" in run (rolled)	"ehr"
"З":"ß", // Like "z" in zoo	"zeh"
"Т":"T", // Like "t" in tap	"teh"
"У":"U", // Like "oo" in boot	"oo"
"В":"V", // Like "v" in van	"veh"
"Ь":"",//Soft Sign	Letter before is soft	"myagkeey"
"Х":"Kh", // Like "h" in hello or like in Scottish 'loch' or German 'Bach'	"khah"
"З":"S", // Like "z" in zoo	"zeh"
"Ц":"TS", // Like "ts" in bits	"tseh"
"Ч":"CH", // Like "ch" in chip	"cheh"
"Ш":"SH",// sh (hard)", // Like "sh" in shut	"shah"
"Щ":"SH",// sh (soft)", // Like "sh" in sheep	"schyah"
"Ж":"Zh", // Like "s" in measure or pleasure "g" in beige (the colour)	"zheh"
"Ы":"I", // Like "i" in ill	"i"
"Ю":"YU", // Like "u" in use or university		"yoo"
"Я":"YA", // Like "ya" in yard.	"yah"
"Ё":"YO" // Like "yo" in yonder	"yo"
 }
args=process.argv
if (args.length < 3) { 
	// ls=read_lines("russian.dic")
	ls=read_lines("russian.german.orig")
}
else {
args.shift()
args.shift()
ls=args
}
for(line of ls){ 
	word=line.trim()
  for(k in map){
    v=map[k]
    word=word.replace("фф","ff")
    word=word.replace("ия","ie")
    // word=word.replace("дл","l")
    // word=word.replace("меж","misch")
    word=word.replace("меж","mix")
    word=word.replace("вая","way")
    word=word.replace("ческое","ische")
    word=word.replace("без","un") // Special rule 1!
    word=word.replace(/$ис/,"ers") // Special rule 1!
    word=word.replace(new RegExp(k,"g"),v)  
    word=word.replace("~f","ph")
    word=word.replace("ij","ic")
    word=word.replace("yu","u")
    word=word.replace("nic","")
    word=word.replace("skic","s")
    word=word.replace("skаy","s")
    word=word.replace("skаia","s")
    word=word.replace(/ma\s*$/,"m")
    word=word.replace("iia","ie")
    word=word.replace("ii","i")
    word=word.replace("iya","ie")
    word=word.replace("nаia","e")
    word=word.replace("nаya","e")
    word=word.replace("nаy","e")
    word=word.replace("nаa","e")
    // word=word.replace("dvo","duo")
    // word=word.replace("nost","*")
    word=word.replace("tziya","tion")
    word=word.replace("tzii","tion")
    word=word.replace("tzie","tion")
    word=word.replace("tzia","tion")
    word=word.replace("tziy","tion")
    word=word.replace("cskic","sch")
    word=word.replace("schkic","isch")
    word=word.replace("ics","sch")
// special :
// äqu
// 
    // word=word.replace("koe","che")
    // word=word.replace("dvo","duo") dvoi zwei : leave!
    // word=word.replace("dvu","duo")
    // word=word.replace("glаs","gloss")
    // word=word.replace("ровка","rung")
    // word=word.replace("рование","rung")


  }
  console.log(line);
  if(word!=line)
  console.log(word);
}
