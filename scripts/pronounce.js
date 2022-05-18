#!/usr/bin/env node
exec=require('child_process').execSync
const runes = require('runes') // for split chars…
require('/Users/me/dev/js/extensions.js')()

maps={
	deutsch:{A:"A",B:"B",C:"C",D:"D",E:"E",F:"F",G:"G",H:"H",I:"I",J:"J",K:"K",L:"L",M:"M",N:"N",O:"O",P:"P",Q:"Q",R:"R",S:"S",T:"T",U:"U",V:"V",W:"W",X:"X",Y:"Y",Z:"Z","TH":"TH",SH:"SH","ß":"ß",GH:"GH",TSH:"TSH",TZ:"TZ","DH":"DH",DZ:"DZ",DJ:"DJ",PS:"PS",EI:"EI"},
arabic:{A:"ا",AO:"أ",B:"ب",C:"چ",D:"د",bdι:"ض",ιE:"ه",e:"ە",EA:"ة",UA:"ؤ",AH:"ة",ė:"ې",ee:"يي",F:"ف",G:"ق",Gh:"ﺝ",h:"ح",H:"ح",KH:"خ",I:"إ",i:"ِ",_i:"ّ",H:"ي",JA:"جو",K:"ك",Ł:"ل",M:"م",N:"ن",on:"ی",OU:"و",Æ:"ع",pP:"قq?",PH:"ف",phth:"ث",FTH:"ϑ",Q:"ق",R:"ر",Ra:"پ",S:"س",s:"ص",SH:"ش",sch:"ش",PTι:"ت",U:"و'",V:"ڤ",W:"ﻭ'",X:"—",Y:"ي",Z:"ﺯ",z:"ز",TH:"ظ",Æ:"ع",DH:"ذ",_:"رِ",__:"ِ",DZι:"ج",vd:"ظ",Z:"ذ","TH":"ط",a_:"ښ",Uh:"ئ",uH:"ۇ",gar:"غ",oñ:"آ",_h:"ک",a:"َ",ah:"ى"},// M as mN:"م" ι
assamese:{k:'ক', kh:'খ', ɡ:'গ', ɡh:'ঘ', ng:'ঙ', s:'চ', s:'ছ', z:'জ', z:'ঝ', n:'ঞ', t:'ট', thh:'ঠ', d:'ড', dh:'ঢ', n:'ণ', tt:'ত', tth:'থ', d:'দ', dhh:'ধ', n:'ন', p:'প', ph:'ফ', b:'ব', bh:'ভ', m:'ম', z:'য', rh:'ৰ', l:'ল', bh:'ৱ', s:'শ', ss:'ষ', ś:'স', h:'হ', jh:'ক্', hr:'ড়', hrh:'ঢ়', j:'য়',ko:'ক',o:'অ',  a:'আ', ka:'কা',i:'ই', ki:'কি', î:'ঈ', kî:'কী', u:'উ', ku:'কু', û:'ঊ', kû:'কূ', ri:'ঋ', kri:'কৃ', e:'এ', ke:'কে', oi:'ঐ', koi:'কৈ', ü:'ও',kü:'কো',ou:'ঔ',kou:'কৌ'},
greek:{ai:"ὄ",A:"α",B:"β",C:"ς",D:"δ",E:"ε",F:"ƒ",wh:"ϝ",G:"γ",Hi:"η",I:"ι",J:"ῖ",K:"κ",L:"λ",M:"μ",N:"ν",O:"o",P:"π",Q:"ϐ",R:"ρ",S:"σ",TH:"θ",T:"τ",U:"υ",V:"φ",W:"ω",X:"ξ",Y:"γ",Z:"ζ","TH":"θ",SH:"ϡ","ß":"ς",TSH:"γ","DH":"ð",DZ:"ҙ",DJ:"γ",PS:"ψ",õ:"ὦ",ch:"χ",ɦra:"ἁ",fi:"ἰ",y:"ύ"},
GREEK:{A:"Α",B:"Β",C:"Ζ",D:"Δ",E:"Ε",F:"Ƒ",wh:"Ϝ",G:"Γ",H:"Η",I:"Ι",J:"Ι͂",K:"Κ",L:"Λ",M:"Μ",N:"Ν",O:"Ω",P:"Π",Q:"Ϙ",R:"Ρ",S:"Σ",T:"Τ",U:"Υ",V:"Φ",W:"Ω͂",X:"Ξ",ch:"Χ",Y:"Ψ",Z:"Ζ","TH":"Θ",SH:"Ϣ",GH:"Γ","DH":"Ð",DZ:"Ҙ",DJ:"Γ",PS:"Ψ"},
geez:{ ha:'ሀ',hu:'ሁ',hi:'ሂ','hā':'ሃ','hē':'ሄ','hə':'ህ',ho:'ሆ',la:'ለ',lu:'ሉ',li:'ሊ','lā':'ላ','lē':'ሌ','lə':'ል',lo:'ሎ','ḥa':'ሐ','ḥu':'ሑ','ḥi':'ሒ','ḥā':'ሓ','ḥē':'ሔ','ḥə':'ሕ','ḥo':'ሖ',ma:'መ',mu:'ሙ',mi:'ሚ','mā':'ማ','mē':'ሜ','mə':'ም',mo:'ሞ','ša':'ሠ','śu':'ሡ','śi':'ሢ','śā':'ሣ','śē':'ሤ','šə':'ሥ','šo':'ሦ',ra:'ረ',ru:'ሩ',ri:'ሪ','rā':'ራ','rē':'ሬ','rə':'ር',ro:'ሮ',sa:'ሰ',su:'ሱ',si:'ሲ','sā':'ሳ','sē':'ሴ','sə':'ስ',so:'ሶ',qa:'ቀ',qu:'ቁ',qi:'ቂ','qā':'ቃ','qē':'ቄ','qə':'ቅ',qo:'ቆ','qʷa':'ቈ','qʷi':'ቊ','qʷā':'ቋ','qʷē':'ቌ','qʷə':'ቍ',ba:'በ',bu:'ቡ',bi:'ቢ','bā':'ባ','bē':'ቤ','bə':'ብ',bo:'ቦ',ta:'ተ',tu:'ቱ',ti:'ቲ','tā':'ታ','tē':'ቴ','tə':'ት',to:'ቶ','ḫ':'ኅ','ḫu':'ኁ','ḫi':'ኂ','ḫā':'ኃ','ḫē':'ኄ','ḫo':'ኆ','ḫʷa':'ኈ','ḫʷi':'ኊ','ḫʷā':'ኋ','ḫʷē':'ኌ','ḫʷə':'ኍ',n:'ነ',nu:'ኑ',ni:'ኒ','nā':'ና','nē':'ኔ','nə':'ን',no:'ኖ','ʼa':'አ','ʼu':'ኡ','ʼi':'ኢ','ʼā':'ኣ','ʼē':'ኤ','ʼə':'እ','ʼo':'ኦ',ka:'ከ',ku:'ኩ',ki:'ኪ','kā':'ካ','kē':'ኬ','kə':'ክ',ko:'ኮ','kʷa':'ኰ','kʷi':'ኲ','kʷā':'ኳ','kʷē':'ኴ','kʷə':'ኵ',wa:'ወ',wu:'ዉ',wi:'ዊ','wā':'ዋ','wē':'ዌ','wə':'ው',wo:'ዎ','ʻa':'ዐ','ʻu':'ዑ','ʻi':'ዒ','ʻā':'ዓ','ʻē':'ዔ','ʻə':'ዕ','ʻo':'ዖ',za:'ዘ',zu:'ዙ',zi:'ዚ','zā':'ዛ','zē':'ዜ','zə':'ዝ',zo:'ዞ',ya:'የ',yu:'ዩ',yi:'ዪ','yā':'ያ','yē':'ዬ','yə':'ይ',yo:'ዮ',da:'ደ',du:'ዱ',di:'ዲ','dā':'ዳ', 'dē':'ዴ', 'də':'ድ', do:'ዶ', ga:'ገ', g:'ጉ',pa:"ፐ", pu:"ፑ", pi:"ፒ", pā:"ፓ", pē:"ፔ", pə:"ፕ", pu:"ፖ", },
pronounce:{A:"ʔ",B:"β",F:"pʰ",H:"ħ",K:"kʰ",L:"l",P:"pʰ",W:"ō","TH":"tʰ ᚦ /θ/",SH:"ʃ","ß":"δ",TSH:"tʃ",TZ:"tz","DH":"þ /ð/",DZ:"dʒ"},
coptic:{A:"Ⲁ",B:"Ⲃ",C:"Ⲥ",D:"Ⲇ",E:"Ⲉ",F:"Ⲋ",G:"Ⲅ",H:"Ϩ",I:"Ϫ",J:"Ⲓ",K:"Ϧ",L:"Ⲗ",M:"Ⲙ",N:"Ⲛ",O:"Ⲟ",P:"Ⲡ",Q:"Ϥ",R:"ⳁ",S:"Ϭ",T:"Ϯ",U:"Ⲩ",V:"Ⲫ",W:"Ⲱ",X:"Ⲝ",Y:"Ⲯ",Z:"Ⳉ","TH":"Ⲑ",SH:"Ϣ","ß":"Ⲋ",GH:"Ⲝ"},
number:{A:"1",B:"2",C:"3",D:"4",E:"5",F:"6",G:"7",H:"8",I:"9",J:"10",K:"20",L:"30",M:"40",N:"50",O:"70",P:"80",Q:"90",R:"100",S:"200",T:"300",U:"400",V:"500",W:"800",X:"600",Y:"700",Z:"900","TH":"1000",SH:"60"},
egyptian:{ch:"𓆓",eo:"𓇋𓊪",ph:"𓆑",ou:"𓊪𓊪",e:"𓇋",A:"𓄿",B:"𓃀",C:"𓍿",D:"𓂧",E:"",F:"𓆑",G:"𓎼",H:"𓉔",I:"𓇋",J:"𓆳",K:"𓎡",L:"𓏯",M:"𓌳",N:"𓈖",O:"𓊪",P:"𓊪",Q:"𓏘",R:"𓂋",S:"𓋴",T:"𓏏",U:"𓅱",V:"𓆑",W:"𓅳",X:"𓏴",Y:"𓏭",Z:"𓊃","TH":"𓆼",SH:"𓄑  𓍱",TSH:"𓆓",TZ:"𓍿"},
phoenician:{A:"𐤀",B:"𐤁",C:"𐤂",D:"𐤃",E:"𐤄",F:"𐤅",G:"𐤂",H:"𐤇",I:"𐤉",J:"𐤉",K:"𐤊",L:"𐤋",M:"𐤍",N:"𐤌",O:"𐤏",P:"𐤐",Q:"𐤒",R:"𐤓",S:"𐤑",T:"𐤕",U:"𐤅",V:"𐤅",W:"𐤅",X:"𐤎",Y:"𐤅",Z:"𐤆","TH":"𐤈",SH:"𐤔"},
ugarit:{A:'𐎀', B:'𐎁', C:'𐎋', D:'𐎏', E:'𐎅', F:'𐎛', G:'𐎙', H:'𐎅', I:'𐎛', J:'𐎊', Kr:'𐎓𐎗', k:'𐎋', L:'𐎍', M:'𐎎', N:'𐎐',î:'𐎓', O:'𐎓', Oyg:'𐎓', P:'𐎔', Q:'𐎖', R:'𐎗', S:'𐎒', T:'𐎚', U:'𐎓', V:'𐎁', W:'𐎆', X:'𐎉', Y:'𐎊', Z:'𐎕', TZ:'𐎑', ß:'𐎌', SH:'𐎒', TH:'𐎘', Ḫ:'𐎃', TD:'𐎝', DH:'𐎑', DZ:'𐎇', BR:'𐎄', Dl:'𐎄', ḥ:'𐎈', cr:'𐎈', ṭ:'𐤈', u:'𐎜', ' ':'𐎟', },
hebrew:{A:"א",BV:"ב",C:"כ",D:"ד",E:"ה",F:"ו",G:"ג",H:"ה",Ḫ:"ח",J:"י",I:"י",CH:"ך",K:"כ",L:"ל",M:"ם",m:"מ",N:"נ",n:"ן",P:"ף",p:"פ",Q:"ק",R:"ר",S:"ץ",ß:"צ",T:"ת",TH:"ט",U:"ו",V:"ו",W:".ו",X:"ס",Y:"ע",e:"ע",Z:"ז",TH:"ט",SH:"ש",SCH:"ש",Ei:'"',ei:"יי"},
arameic:{A:"ܐ",B:"ܒ",C:"ܓ",D:"ܕ",E:"ܗ",F:"ܘ",G:"ܓ",H:"ܚ",I:"ܝ",J:"ܝ",K:"ܟ",L:"ܠ",M:"ܡ",N:"ܢ",O:"ܥ أ",P:"ܦ",Q:"ܩ",R:"ܪ",S:"ܨ",T:"ܬ",U:"ܘ",V:"ܘ",W:"ܘ",X:"ܤ",Y:"ܘ",Z:"ܙ","TH":"ܛ",SH:"ܫ"},
ethiopic:{A:"አ",B:"በ",C:"ገ",D:"ደ",E:"ሀ",F:"ወ",G:"ገ",H:"ሐ",I:"የ",J:"የ",K:"ከ",L:"ለ",M:"መ",N:"ነ",O:"ዐ",P:"ፈ",Q:"ቀ",R:"ረ",S:"ጸ",T:"ተ",U:"ወ",V:"ወ",W:"ወ",X:"ሰ",Y:"ወ",Z:"ዘ","TH":"ጠ",SH:"ሠ",GH:"ኀ",TZ:"ፐ", /* ፑ 	ፒ 	ፓ 	ፔ 	ፕ 	ፖ ...*/},
RUSSIAN:{A:"А",B:"Б",v:"в",G:"Г",D:"Д",E:"Е",F:"Ѵ",Gh:"Ґ",Hi:"И",I:"Ї",Ї:"Ї",J:"Ј",K:"К",L:"Л",M:"М",N:"Н",O:"О",P:"П",Q:"Ҁ",R:"Р",S:"С",SS:"Ц",T:"Т",U:"У",V:"Ѵ",W:"Ў",X:"Х",Y:"У'",Zhe:"Ж",Ghe:"Ж","TH":"Ѳ",SH:"Ш","ßh":"Щ",TSH:"Ѯ",TZ:"З",ß:"З",DH:"Ч",DJ:"Ѳ",SCH:"Щ",ėi:"Ы",EI:"Є",As:"Э",_:"ь",HN:"Й",IA:"я", ƒ:"Ф" /* ,pth:"Ф" F/PH/TH Athens+France!*/},
russian:{A:"а",B:"б",G:"г",D:"д",E:"е",F:"ѵ",Gh:"ґ",hi:"и",h:"ъ",ї:"ї",J:"ј",K:"к",L:"л",M:"м",N:"н",O:"о",P:"п",Q:"ҁ",R:"р",S:"с",SS:"ц",T:"т",U:"у",V:"ѵ",W:"ў",X:"х",Y:"у'",Zhe:"ж",ghe:"ж","TH":"ѳ",SH:"ш","ßh":"щ",TSH:"ѯ",TZ:"з",ß:"з",DH:"џ",dj:"ѳ",sch:"щ",ėi:"ы",ei:"є",δh:"ч",ai:"э",_:"ь",u:"ӑ",uh:"ă",gh:"гъ",ia:"я",ƒ:"ф"},
name:{A:"āl'ep",B:"bēt",C:"gīml",D:"dālet",E:"hē",F:"vāw",G:"gīml",H:"ḥēt",I:"iōd",J:"jōd",K:"kāp",L:"lāmed",M:"mēm",N:"nūn",O:"o'yin",P:"pē",Q:"qōp",R:"rēš",S:"ṣādē",T:"tāw",U:"uāw",V:"vāw",W:"wāw",X:"xsāmeks",Y:"yāw",Z:"zayin","TH":"ṭzēt",SH:"šīn"},
sumerian:{A:"𒀀",B:"𒁇",C:"𒄑",D:"𒆕",E:"𒂊",F:"𒉺",G:"𒂷",H:"𒄷",I:"𒄿",J:"𒐊",K:"𒋡",L:"𒇲",M:"𒈫",N:"𒈾",O:"𒄭",P:"𒉺",Q:"𒆪",R:"𒊒",S:"𒌍",T:"𒁹",U:"𒌑",V:"𒌋",W:"𒑱",X:"𒉽",Y:"𒄿",Z:"𒋝","TH":"𒍣",SH:"𒍝","ß":"𒁲"},
akkadia:{A:"𒈨",B:"𒁁",C:"𒃰",D:"𒌓",E:"𒂊",F:"𒉺",G:"𒂷",H:"𒌨",I:"𒄿",J:"𒐊",K:"𒋡",L:"𒇲",M:"𒈠",N:"𒈾",O:"𒄭",P:"𒇥𒌒",Q:"𒌆",R:"𒊕",S:"𒊓",T:"𒁹",U:"𒌑",V:"𒌋  vu",W:"𒉿",X:"𒄬",Y:"𒄿",Z:"𒍝","TH":"𒍣",SH:"𒁲"},
chinese:{A:"吖/阿",B:"匕/卜比 北阝",C:"厂/寸",D:"刀/刂",E:"噩",F:"父",G:"产",H:"戶",I:"一",J:"亅",K:"犭",L:"了",M:"門",N:"宀",O:"口",P:"匹/片",Q:"甲",R:"人/亻𠆢 入 八日",S:"屮",T:"土/头",U:"乌",V:"巫",W:"文/无",X:"斤",Y:"义/又",Z:"左",SH:"山 shān  手 (扌,龵) shou","DH":"心 (忄,⺗) xin heart",PS:"已] yǐ"},
rune:{A:"ᚨ",B:"ᛒ",C:"ᚲ",D:"ᛞ",E:"ᛖ",F:"ᚠ",G:"ᚷ",H:"ᚻ",I:"ᛁ",J:"ᛃ,ᛇ",K:"ᚲ",L:"ᛚ",M:"ᛗ",N:"ᚾ",O:"ᛟ",P:"ᛈ",Q:"ᛟ",R:"ᚱ",S:"ᛊᛋ",T:"ᛏ",U:"ᚢ",V:"ᚢ",W:"ᚢ",X:"ᚷ",Y:"ᛃ",Z:"ᛋ","TH":"ᚦ",SH:"ᛊ","ß":"ᛉ"},
runam:{A:"*ansuz",B:"*berkanan",C:"*cenaz",D:"*dagaz",E:"*ehwaz",F:"*fehu",G:"*gebō",H:"*haglaz",I:"*īsaz",J:"*jeīwaz",K:"*kenaz",L:"*laguz",M:"*mannaz",N:"*naudiz",O:"*ōþila",P:"*perþ",Q:"*ōþala",R:"*raidō",S:"*sōwilō",T:"*teiwaz",U:"*ūruz",X:"*gebō",Y:"*yēra",Z:"*algiZ","TH":"*þurisaz",SH:"*sōwilō"},
runename:{A:"Æsir",B:"birch",C:"ctorch",D:"day",E:"horse",F:"wealth",G:"gift",H:"hail",I:"ice",J:"yew",K:"kHand",L:"lake",M:"man",N:"need",O:"eQuipe",P:"pear?",Q:"pOssess",R:"ride",S:"Sun",T:"Theius",U:"Urochs",X:"xift",Y:"year",Z:"Z'elk","TH":"Thor",SH:"Sun"},
persian:{A:"𐎠",U:"𐎢",I:"𐎡",B:"𐎲",C:"𐎨",D:"𐎭",DI:"𐎮",DU:"𐎯",EI:"𐎡",FA:"𐎳",GA:"𐎥",GU:"𐎦",HA:"𐏃",I:"𐎡",JI:"𐎪",JA:"𐎩",YA:"𐎹",KA:"𐎣",LA:"𐎾",MA:"𐎶",M:"𐎷",MU:"𐎸",NA:"𐎴",NU:"𐎵",P:"𐎱",QU:"𐎤",R_:"𐎼",RU:"𐎽",S:"𐎿",T:"𐎫",DT:"𐎬",TH:"𐎰",V:"𐎺",VI:"𐎻",XA:"𐎧",YA:"𐎹",Z:"𐏀",šς:"𐏁",š:"𐏂"},
sanscrit:{// hindi
 _T:	"ठ",A__:"अ",_A_:"अं",__:"ं", AA:	"आ", O:	"ओ", I:	"इ",IE:"य", U:	"उ", HI:	"ई", UU:	"ऊ", RR:	"ऋ", LL:	"ऌ", E:	"ऍ", e:	"ऎ", EE:	"ए", AI:	"ऐ", OH:	"ऑ", o:	"ऒ", OO:	"ओ", AU:	"औ", A:	"अ", B:	"ब", BHe:	"भ", C:	"च", D:	"द", DH:	"ध", DD:	"ड", DDH:	"ढ", E:	"ए", F:	"फ़", G:	"ग", GH:	"घ", H:	"ह", I:	"इ", J:	"ज", JH:	"झ", K:	"क", KH:	"ख", L:	"ल", LL:	"ळ", LLL:	"ऴ", M:	"म", N:	"न", NY:	"ञ", NG:	"ङ", O:	"ओ", P:	"प", PH:	"फ", Q:	"क़", R:	"र", RR:	"ऱ", S:	"स", SH:	"श",  U:	"उ", V:	"व", ΩM:	"ॐ ", Z:	"ज़", K:	"क", KH:	"ख", G:	"ग", GH:	"घ", NG:	"ङ", C:	"च", CH:	"छ", J:	"ज", JH:	"झ", NY:	"ञ", TT:	"ट", DD:	"ड", DDH:	"ढ", NN:	"ण", T:	"त", TH:	"थ", D:	"द", DH:	"ध", N:	"न", NNN:	"ऩ", P:	"प", PH:	"फ", M:	"म", Y:	"य", M:	"म", R:	"र", RR:	"ऱ", L:	"ल", LL:	"ळ", LLL:	"ऴ", V:	"व", SH:	"श", ƒS:	"ष", S:	"स", H:	"ह", AI:	"़", AVAGRAH:	"ऽ", A:	"ा", Y:	"ी", V_U:	"ु", V_UU:	"ू", _R:	"ृ", _RR:	"ॄ", E_:	"ॅ", _E:	"ॆ", _ER_:	"े", V_AI:	"ै", _O:	"ॉ", o:	"ॊ", V_O:	"ो", V_AU:	"ौ", _:	"्", OM:	"ॐ", UDATTA:	"॑", ANUDATTA:	"॒", GRAVE:"॓", _:"॔", QA:	"क़", ZA:	"ज़", FA:	"फ़", KHHA:	"ख़", GHHA:	"ग़", DDDHA:	"ड़", RHA:	"ढ़", YYA:	"य़", RRR:	"ॠ", LLL:	"ॡ", LO:	"ॢ", LL:	"ॣ", DANDA:	"।", DOUBLE_DANDA:	"॥", 0:	"०", 1:	"१", 2:	"२", 3:	"३", 4:	"४", 5:	"५", 6:	"६", 7:	"७", 8:	"८", 9:	"९", '.': "॰",'~':' ',___:"्",____:'ः',_i_:"ःि",ni:"नि",TI:'ठि', i_:	"ि",_m_:"ँ"
},
armenian:{a:'ա',b:'բ',g:'գ',d:'դ',ew:'և',ē:'է',e:'ե',f:'ֆ',ë:'ը',i:'ի',ǰ:'ջ',j:'ձ',l:'լ',k:'կ',kx:'ք',h:'հ',ł:'ղղ',ġ:'ղղ',m:'մ',n:'ն',ō:'օ',o:'ո',p:'պ',ph:'փ',č:'ճ',ch:'ց',c:'ծ',čh:'չ',s:'ս',t:'տ',t:'տ',th:'շ',sh:'շ',š:'շ',tß:'թ',r:'ր',ṙ:'ռ',v:'վ',o:'ո',w:'ւ',x:'խ',y:'յ',z:'զ',ž:'ժ',},
ARMENIAN:{A:'Ա',B:'Բ',G:'Գ',D:'Դ',EW:'ԵՒ',Ē:'Է',E:'Ե',F:'Ֆ',Ë:'Ը',I:'Ի',J̌:'Ջ',J:'Ձ',L:'Լ',K:'Կ',KX:'Ք',H:'Հ',Ł:'ՂՂ',Ġ:'ՂՂ',M:'Մ',N:'Ն',Ō:'Օ',O:'Ո',P:'Պ',PH:'Փ',Č:'Ճ',CH:'Ց',C:'Ծ',ČH:'Չ',S:'Ս',T:'Տ',T:'Տ',TH:'Շ',SH:'Շ',Š:'Շ',TSS:'Թ',R:'Ր',Ṙ:'Ռ',V:'Վ',O:'Ո',W:'Ւ',X:'Խ',Y:'Յ',Z:'Զ',Ž:'Ժ',},
LAOs:{i:"-ິ",ư:"-ຶ",u:"-ຸ",e:"ເ-ະ",E:"ເ-",æ:"ແ-ະ",AE:"ແ-",o:"ໂ-ະ",OH:"-",ǫ:"ເ-າະ",O:"-ັອ",œ:"ເ-",ia:"ເ-ັຍ",iA:"-ັຽ",ua:"ເ-ຶ",oua:"-ົວະ",Ou:"-ັວ",ai:"-ັ",ao:"ເ-ົ",am:"-ຳ",K:"ໄກ",k:"ກ",Kh:"ໄຂ",kh:"ຂ",kH:"ຄ",KH:"ຄວາ",ng:"ງ",ch:"ຈ",CH:"ຈອ",s:"ສ",S:"ເສື",t:"ຊ",T:"ຊ້າ",mo:"ຍ",D:"ເດັ",d:"ດ",t:"ຕ",th:"ຖ",tH:"ທ",n:"ນ",B:"ແບ",b:"ບ",p:"ປ",ph:"ພ",Ph:"ເຜິ້",pH:"ຜ",f:"ຝ",F:"ຟ",ff:"ໄ?",M:"ແມ",m:"ມ",y:"ຢ",r:"ຣະຄັ",l:"ລ",v:"ວ",H:"ຫ່າ",h:"ຫ",ow:"ອ ໂ",h:"ຮ ເຮືອ",ng:"ຫງ",ny:"ຫຍ",n:"ໜ",m:"ໝ",r:"ຫຼ",l:"ຫຼ",w:"ຫວ",ll:"ອ ັ",L:"ອັ",aA:"ະ",lL:"ຫ",Ll:"ລ",n_:"ນ",N_:"oັ",_n:"ຊ",nN:"oັ",oum:"ມ",s_:"sົ",_S:"ສ ົ"},// <<BAD!;) NIGHTMARE :)
GEORGIAN:{A:"Ⴀ",	B:"Ⴁ",	G:"Ⴂ",	D:"Ⴃ",	E:"Ⴄ",	F:"Ⴅ",	GZ:"Ⴆ",	H:"Ⴡ",t:"Ⴇ",	J:"Ⴈ",	K:"Ⴉ",	L:"Ⴊ",	M:"Ⴋ",	N:"Ⴌ",	HO:"Ⴢ",	O:"Ⴍ",	P:"Ⴎ",	ZH:"Ⴏ",	R:"Ⴐ",	S:"Ⴑ",	PT:"Ⴒ",	PH:"Ⴣ",	Qu:"Ⴍ",	QU:"ႭჃ",	Q:"Ⴓ",	Ph:"Ⴔ",	C:"Ⴕ",	Gh:"Ⴖ",	Qh:"Ⴗ",	Sh:"Ⴘ",	ch:"Ⴙ",	ts:"Ⴚ",	ds:"Ⴛ",	tS:"Ⴜ",	cH:"Ⴝ",	kH:"Ⴞ",	qa:"Ⴤ",	ja:"Ⴟ",	ha:"Ⴠ",	hoe:"Ⴥ",	},
georgian:{a:"ა",b:"ბ",g:"გ",d:"დ",e:"ე",f:"ვ",gz:"ზ",T:"თ",I:"ი",k:"კ",l:"ლ",m:"მ",n:"ნ",o:"ო",p:"პ",zh:"ჟ",r:"რ",s:"ს",t:"ტ",u:"უ",ph:"ფ",K:"ქ",gh:"ღ",qa:"ყ",sh:"შ",ch:"ჩ",ts:"ც",ds:"ძ",tS:"წ",CH:"ჭ",kh:"ხ",ja:"ჯ",ha:"ჰ"},
Khmer:{
	// â:"អ",a:"ា",e:"ិ",i:"ី",æ:"ឹ",œ:"ឺ",ŏ:"ុ",u:"ូ",uŏ:"ួ",aeu:"ើ",œă:"ឿ",iĕ:"ៀ",é:"េ",ê:"ែ",ai:"ៃ",aô:"ោ",au:"ៅ",ka:"ខ",
	ʰ:"អ",_:"អ",a:"ា",e:"ិ",i:"ី",ae:"ឹ",oe:"ឺ",ou:"ុ",u:"ូ",uo:"ួ",aeu:"ើ",oa:"ឿ",ie:"ៀ",ee:"េ",eh:"ែ",ai:"ៃ",ao:"ោ",au:"ៅ",oum:"ំ",oo:"ះ",
	_e:"ឥ",_ei:"ឦ",_o:"ឧ",_on:"ឨ",_ou:"ឩ",_ahu:"ឪ",_rou:"ឫ",_roe:"ឬ",_lou:"ឭ",_loe:"ឮ",_e:"ឯ",_ai:"ឰ",_ao:"ឱ",_au:"ឳ",ohu:"័",hi:"់",mou:"៉",
	ka:"ខ", k:"ក",kh:"ខ",K:"គ",kH:"ឃ",ng:"ង",ch:"ច",Ch:"ឆ",cHo:"ជ",chH:"ឈ",nh:"ញ",d:"ដ",th:"ឋ",D:"ឌ",th:"ឍ",nn:"ណ",T:"ត",Th:"ថ",tt:"ទ",tH:"ធ",nN:"ន",p:"ប",ph:"ផ",pa:"ព",pH:"ភ",m:"ម",y:"យ",r:"រ",l:"ល",v:"វ",S:"ឝ",ss:"ឞ",s:"ស",h:"ហ",ll:"ឡ",H:"្"},
 Hiragana:{_A:"ぁ",A:"あ",_I:"ぃ",I:"い",_U:"ぅ",U:"う",_E:"ぇ",E:"え",_O:"ぉ",O:"お",KA:"か",GA:"が",KI:"き",GI:"ぎ",KU:"く",GU:"ぐ",KE:"け",GE:"げ",KO:"こ",GO:"ご",SA:"さ",ZA:"ざ",SI:"し",ZI:"じ",SU:"す",ZU:"ず",SE:"せ",ZE:"ぜ",SO:"そ",ZO:"ぞ",TA:"た",DA:"だ",TI:"ち",DI:"ぢ",_TU:"っ",TU:"つ",DU:"づ",TE:"て",DE:"で",TO:"と",DO:"ど",N_:"ン",NA:"な",NI:"に",NU:"ぬ",NE:"ね",NO:"の",HA:"は",BA:"ば",PA:"ぱ",HI:"ひ",BI:"び",PI:"ぴ",HU:"ふ",BU:"ぶ",PU:"ぷ",HE:"へ",BE:"べ",PE:"ぺ",HO:"ほ",BO:"ぼ",PO:"ぽ",MA:"ま",MI:"み",MU:"む",ME:"め",MO:"も",_YA:"ゃ",YA:"や",_YU:"ゅ",YU:"ゆ",_YO:"ょ",YO:"よ",Ra:"ラ",RA:"ら",RI:"り",RU:"る",RE:"れ",RO:"ろ",_WA:"ゎ",WA:"わ",WI:"ゐ",WE:"ゑ",WO:"を",N:"ん",VU:"ゔ"},
 Katakana:{// japanese 
_A:"ァ",A:"ア",_I:"ィ",I:"イ",_U:"ゥ",U:"ウ",_E:"ェ",E:"エ",_O:"ォ",O:"オ",KA:"カ",GA:"ガ",KI:"キ",GI:"ギ",KU:"ク",GU:"グ",KE:"ケ",GE:"ゲ",KO:"コ",GO:"ゴ",SA:"サ",ZA:"ザ",SI:"シ",ZI:"ジ",SU:"ス",ZU:"ズ",SE:"セ",ZE:"ゼ",SO:"ソ",ZO:"ゾ",TA:"タ",DA:"ダ",TI:"チ",DI:"ヂ",_TU:"ッ",TU:"ツ",DU:"ヅ",TE:"テ",DE:"デ",TO:"ト",DO:"ド",NA:"ナ",NI:"ニ",N_:"ン",NU:"ヌ",NE:"ネ",NO:"ノ",HA:"ハ",BA:"バ",PA:"パ",HI:"ヒ",BI:"ビ",PI:"ピ",HU:"フ",BU:"ブ",PU:"プ",HE:"ヘ",BE:"ベ",PE:"ペ",HO:"ホ",BO:"ボ",PO:"ポ",MA:"マ",MI:"ミ",MU:"ム",ME:"メ",MO:"モ",_YA:"ャ",YA:"ヤ",_YU:"ュ",YU:"ユ",_YO:"ョ",YO:"ヨ",RA:"ラ",RI:"リ",RU:"ル",RE:"レ",RO:"ロ",_WA:"ヮ",WA:"ワ",WI:"ヰ",WE:"ヱ",WO:"ヲ",N:"ン",VU:"ヴ",_KA:"ヵ",_KE:"ヶ",VA:"ヷ",VI:"ヸ",VE:"ヹ",VO:"ヺ",A:"㋐",I:"㋑",U:"㋒",E:"㋓",O:"㋔",KA:"㋕",KI:"㋖",KU:"㋗",KE:"㋘",KO:"㋙",Ra:"ラ",SA:"㋚",SI:"㋛",SU:"㋜",SE:"㋝",SO:"㋞",TA:"㋟",TI:"㋠",TU:"㋡",TE:"㋢",TO:"㋣",NA:"㋤",NI:"㋥",NU:"㋦",NE:"㋧",NO:"㋨",HA:"㋩",HI:"㋪",HU:"㋫",HE:"㋬",HO:"㋭",MA:"㋮",MI:"㋯",MU:"㋰",ME:"㋱",MO:"㋲",YA:"㋳",YU:"㋴",YO:"㋵",RA:"㋶",RI:"㋷",RU:"㋸",RE:"㋹",RO:"㋺",WA:"㋻",WI:"㋼",WE:"㋽",WO:"㋾",WO:"ｦ",_A:"ｧ",_I:"ｨ",_U:"ｩ",_E:"ｪ",_O:"ｫ",_YA:"ｬ",_YU:"ｭ",_YO:"ｮ",_TU:"ｯ",A:"ｱ",I:"ｲ",U:"ｳ",E:"ｴ",O:"ｵ",KA:"ｶ",KI:"ｷ",KU:"ｸ",KE:"ｹ",KO:"ｺ",SA:"ｻ",SI:"ｼ",SU:"ｽ",SE:"ｾ",SO:"ｿ",TA:"ﾀ",TI:"ﾁ",TU:"ﾂ",TE:"ﾃ",TO:"ﾄ",NA:"ﾅ",NI:"ﾆ",NU:"ﾇ",NE:"ﾈ",NO:"ﾉ",HA:"ﾊ",HI:"ﾋ",HU:"ﾌ",HE:"ﾍ",HO:"ﾎ",MA:"ﾏ",MI:"ﾐ",MU:"ﾑ",ME:"ﾒ",MO:"ﾓ",YA:"ﾔ",YU:"ﾕ",YO:"ﾖ",RA:"ﾗ",RI:"ﾘ",RU:"ﾙ",RE:"ﾚ",RO:"ﾛ",WA:"ﾜ",N:"ﾝ"
 },// :"・",X1:"ヽ",X2:"ヾ",S1:"ﾞ",S2:"ﾟ",
 armenian:{a:"ա",b:"բ",g:"գ",d:"դ",e:"ե",z:"զ",ē:"է",ə:"ë",th:"թ",ž:"ժ",i:"ի",l:"լ ",x:"խ",c:"ծ",k:"կ",h:"հ",j:"ձ",ł:"ղ",ġ:"ղ",č:"ճ",m:"մ",y:"յ",n:"ն",š:"շ",o:"ո",čh:"չ",p:"պ",ǰ:"ջ",ṙ:"ռ",s:"ս",v:"վ",t:"տ",r:"ր",ch:"ց",w:"ւ",ph:"փ",kh:"ք",ew:"և",ō:"օ",f:"ֆ"},
 ARMENIAN:{A:"Ա",B:"Բ",G:"Գ",D:"Դ",E:"Ե",Z:"Զ",Ē:"Է",Ə:"Ë",TH:"Թ",Ž:"Ժ",I:"Ի",L:"Լ ",X:"Խ",C:"Ծ",K:"Կ",H:"Հ",J:"Ձ",Ł:"Ղ",Ġ:"Ղ",Č:"Ճ",M:"Մ",Y:"Յ",N:"Ն",Š:"Շ",O:"Ո ",ČH:"Չ",P:"Պ",J̌:"Ջ",Ṙ:"Ռ",S:"Ս",V:"Վ",T:"Տ",R:"Ր",CH:"Ց",W:"Ւ",PH:"Փ",KH:"Ք",EW:"ԵՒ",Ō:"Օ",F:"Ֆ"},
 telugru:{k:'క', kh:'ఖ', g:'గ', gh:'ఘ', ṅ:'ఙ', ch:'చ', chh:'ఛ', j:'జ', jh:'ఝ', ñ:'ఞ', ṭ:'ట', ṭh:'ఠ', ḍ:'డ', ḍh:'ఢ', ṇ:'ణ', t:'త', th:'థ', d:'ద', dh:'ధ', n:'న', p:'ప', ph:'ఫ', b:'బ', bh:'భ', m:'మ', y:'య', r:'ర', l:'ల', v:'వ', ḷ:'ళ', ś:'శ', ṣ:'ష', s:'స', h:'హ', ṟ:'ఱ', a:'అ', i:'ఇ', u:'ఉ', r̥:'ఋ', l̥:'ఌ', e:'ఎ', ai:'ఐ', o:'ఒ', au:'ఔ', ā:'ఆ', ī:'ఈ', ū:'ఊ', ē:'ఏ', ō:'ఓ'},

 malayam:{po:'പൊ'},
	french:{A:"à",EE:"é",AH:"á"},
	FRENCH:{A:"À",EE:"É",AH:"Á"},
	czech:{I:"Ý",C:"Č",y:"ý",c:"č",cc:'č'},
	TURK:{G:"Ğ",I:"I",J:"İ",O:"Ö",S:"Ş,",C:"Ç"},
	turk:{g:"ğ",i:"ı",j:"i̇",o:"ö",s:"ş,",c:"ç"},
	romain:{ah:"â",u:"ă",AH:"Â",AU:"Ă"},
	vietnam:{e:"ế",ä:"ệ"},
	nordic:{oe:"ø",a:"å",u:"ų"},
	ruski:{oe:"ə"},
}



// हिन्दी
let xeno2english={}
for(map in maps){
	map=maps[map]
	n=Object.keys(map).reduce((obj,key)=> {obj[map[key]]=key;return obj},{});
	Object.assign(xeno2english,n)
}

langs=keys(maps)
// // console.log(xeno2english)
// langs="deutsch	greek	greek2 geez	pronounce	assamese	coptic	number	egypt	phoenician	chinanr	hebrew	arameic	arabic	ethiopic	russian	russi	name	mean	meane	mein	persian	sumerian	akkadia	chinese	rune	runam	runename".split("\t")


function convert(text,alphabet) {
	if(!alphabet)return console.log("missing alphabet 2.nd parameter")
	map=maps[alphabet]
	if(!map)return console.log("alphabet",alphabet,"not in",Object.keys(maps))
	return text.map(c=>map[c]||map[c.lower()]||map[c.upper()]||c)	
}


function pronounce(text,old=false){
	if(!text)return ""
	if(old)
		text=text.split('').map(x=>xeno2english[x]||x).join('').replace("_","").toLowerCase()
	return runes(text).map(x=>xeno2english[x]||x).join('').replace("_","").toLowerCase()
}
function reverse(s){
    return s.split("").reverse().join("");
}

function say(text){
	text=text.trim()
chars = runes(text)
text=text.replace(/~/g,' ')
text=text.replace(/_/g,' ')
pronounced=pronounce(text.replace(/ि/g,' ि '))
pronounced=pronounced.replace(/\s*ि/g,'i') // why? split bug?
pronounced=pronounced.replace(/~/g,' ')
pronounced=pronounced.replace(/_/g,'')

c=text[0]
voice=0
function has_any(map,x){vals=Object.values(map);for(let a of x.split('')){if(vals.indexOf(a)>=0)return 1}}
function has(map,x){return Object.values(map).indexOf(x)>=0 }

if("MAC"==1){
if(text.match(/[\u0100-\u1FFFF]+/g)) voice="FOREIGN"
else if(has_any({e:'ê',ee:'é',E:'Ê',EE:'É'},text))voice="Luciana" // PT 
else if(has_any({n:'ñ'},text))voice="Jorge" 
else if(has_any(maps.turk,text))voice="Yelda" 
else if(has_any(maps.TURK,text))voice="Yelda" 
else if(has_any(maps.romain,text))voice="Yelda" // vs :
else if(has_any(maps.french,text))voice="Thomas"
else if(has_any(maps.FRENCH,text))voice="Thomas"
else if(has_any(maps.czech,text))voice="Zuzana"
else if(has_any(maps.nordic,text))voice="Satu"// fin
else if(has_any(maps.ruski,text))voice="Yuri"// fin
else if(has_any(maps.sanscrit,text))voice="Lekha" //hindy नमस्कार, मेरा नाम लेखा है
else if(has(maps.arabic,c))voice="Tarik" // Maged
else if(has(maps.hebrew,c))voice="Carmit"
else if(has(maps.RUSSIAN,c))voice="Yuri"
else if(has(maps.ethiopic,c))voice="ethiopic"
else if(has(maps.russian,c))voice="Yuri"
else if(has(maps.persian,c))voice="Melina"
else if(has(maps.greek,c))voice="Melina"
else if(has(maps.GREEK,c))voice="Melina"
else if(has(maps.sanscrit,c))voice="Lekha" //hindy नमस्कार, मेरा नाम लेखा है
else if(has(maps.chinese,c)|| text.match(/[\u4E00-\u9FFF]+/g)) voice="Ting-Ting"
else if(has(maps.Katakana,c))voice="Kyoko"// ja_JP Katakana ( 30a0 - 30ff)
else if(has(maps.Hiragana,c))voice="Kyoko"// Hiragana ( 3040 - 309f) 
else if(text.match(/[\u3040-\u30ff]+/g)) voice="Kyoko" // also for chinese chars 日本 NiPhong
else if(text.match(/[\uAC00-\uD79D]+/g)) voice="Yuna" // KO
else if(text.match(/[\u0E00-\u0E7F]+/g)) voice="Kanya" // Thai 
// else if(text.match(/[\u0D00-\u0D7F]+/g)) voice="Kanya" //Malayalam ;)
// see /System/Library/Perl/5.18/unicore/lib/Blk/Gujarati.pl ... for ranges	
}

if("Linux"){
if(text.match(/[\u0100-\u1FFFF]+/g)) voice="FOREIGN"
if(has(maps.sanscrit,c))voice="hindi" // नमस्कार, मेरा नाम लेखा है
if(has(maps.RUSSIAN,c))voice="russian"
if(has(maps.russian,c))voice="russian"
}


if(voice)console.log(voice)
console.log(text)
console.log(chars.join(' '))
if(voice=="Melina")text=text.replace("ἁ","χα").replace("ἰ","φι").replace("ύ","ι")
if(voice=="Maged" || voice=="Tarik" || voice=="Carmit")// right to left
console.log(reverse(chars.join(' ')))
// console.log(pronounce(text).join(''))
if(voice=="FOREIGN"){
	// text=pronounced=exec("/me/ai/phonemes/pronouce_via_phonemes.swift "+text).toString('utf8')
	voice=0 //"Vicki" english-us default
	// console.log("FOREIGN!!!")
}
if( voice=="Ting-Ting"){
	// pronounced=exec("/me/ai/phonemes/pronouce_via_phonemes.swift "+text).toString('utf8')
}
if(voice=="Maged" || voice=="Tarik") pronounced=pronounced.replace(/^ał/,"al ")
if(voice=="Maged" || voice=="Tarik") pronounced=pronounced.replace(/ ał/," ti ")
if(voice=="Maged" || voice=="Tarik") pronounced=pronounced.replace(/ee/,"e")
if(pronounced!=text)
console.log(pronounced)

// if(has(maps.armenian,c)){voice="Steffi";text=pronounced;}
// if(has(maps.ARMENIAN,c)){voice="Steffi";text=pronounced;}

linux=1
if(linux);//exec("espeak -v mb-en1 -k5 -s150 "+text)
else if(voice)exec("say -s 100 -v"+voice+" '"+text+"'")
else exec("say '"+pronounced+"'")
}// end of say()

// βʔpʰħ
// لابس

module.exports={pronounce,say,convert}

// IF MAIN()
text=process.argv.slice(2,process.argv.length).join(" ")
try{
	if(is_file(text))text=read_text(text)
}catch(ex){console.error(ex);}
if(!text)	process.stdin.on('data', function(pipe) {say(""+pipe)})
else say(text)

