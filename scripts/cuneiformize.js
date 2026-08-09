#!/usr/bin/env node
exec = require('child_process').execSync
// const runes = require('runes') // for split chars…
require('/Users/me/dev/js/extensions.js')()
map = {}

let list_file = "/Users/me/uruk_egypt/abc/cuneiform.list";
let list_file1 = "/Users/me/uruk_egypt/abc/cuneiform.csv.full"

// # DEPRECATED see and use cuneiform.list !
// let list_file2 = "/Users/me/uruk_egypt/abc/cuneiform.main";
// # BUT it contains unmapped signs like 𒌎 …!!!
// # DEPRECATED see and use cuneiform.list !
// let list_file3 = "/Users/me/uruk_egypt/dicts/cuneiform/signs.txt"

map['\n'] = '\n'
map['='] = '='
map['1'] = '𒑰'
map['2'] = '𒈫'
map['3'] = '𒐈'
map['4'] = '𒐘'
// map['4']='𒐉'
map['5'] = '𒐊'
map['6'] = '𒐋'
map['7'] = '𒐌'
map['8'] = '𒐍'
map['9'] = '𒐎'
// map['10']='𒐏'
// map['20']='𒐐'

map['DU&DU'] = '𒁻'
map['𒍏'] = '𒍏'
map['@g'] = '𒆦'
map['@s'] = '·𒎙'
map['@g@g'] = '·𒆦𒆦'
map['@t'] = '@t'
map['lugal@s'] = '𒈚'
map['ne@s'] = '𒉋'
map['ur@s'] = '𒌪'
map['aš@k'] = '𒍻' // 𒀺
map['gu%gu'] = '𒄗'
map['nam@n'] = '𒉅'
map['en@en'] = '𒂜'
map['kur@kur'] = '𒆴'
map['lu2@lu2'] = '𒈓'
map['@lu2'] = '𒈓' // bug!
map['naga@naga'] = '𒉃'
map['PIRIG@PIRIG'] = '𒊎'
map['𒊔@g'] = '𒊨'
map['zubud@g'] = '𒆦·𒄪'
map['saŋ'] = '𒊔' // ?
map['dag kisim5×a+maš'] = '𒁗'
map['dag kisim5×amar'] = '𒁘'
map['dag kisim5×balag'] = '𒁙'
map['dag kisim5×bi'] = '𒁚'
map['dag kisim5×ga'] = '𒁛'
map['dag kisim5×ga+maš'] = '𒁜'
map['dag kisim5×gi'] = '𒁝'
map['dag kisim5×gir2'] = '𒁞'
map['dag kisim5×gud'] = '𒁟'
map['dag kisim5×ḫa'] = '𒁠'
map['dag kisim5×ir'] = '𒁡'
map['dag kisim5×ir+lu'] = '𒁢'
map['dag kisim5×kak'] = '𒁣'
map['dag kisim5×la'] = '𒁤'
map['dag kisim5×lu'] = '𒁥'
map['dag kisim5×lu+maš2'] = '𒁦'
map['dag kisim5×lum'] = '𒁧'
map['dag kisim5×ne'] = '𒁨'
map['dag kisim5×pap+pap'] = '𒁩'
map['dag kisim5×si'] = '𒁪'
map['dag kisim5×tak4'] = '𒁫'
map['dag kisim5×u2+gir2'] = '𒁬'
map['dag kisim5×uš'] = '𒁭'
map['dag kisim5×u2+maš'] = '𒍳'
map['an%an'] = '𒀮'
map['aš%aš.tug2%tug2.tug2%tug2.pap'] = '𒀻'
map['aš%aš'] = '𒋰'
map['aš%aš%aš'] = '𒀼'
map['aš%aš%aš.crossing.aš%aš%aš'] = '𒀽'
map['ash%ash.tug2%tug2.tug2%tug2.pap'] = '𒀻'
map['ash%ash%ash'] = '𒀼'
map['ash%ash%ash.crossing.ash%ash%ash'] = '𒀽'
map['bal%bal'] = '𒁅'
map['bu%bu.ab'] = '𒁎'
map['bu%bu.un'] = '𒁏'
map['bulug%bulug'] = '𒁒'
map['du%du'] = '𒁻'
map['e%e.nun%nun'] = '𒂌'
map['en%en'] = '𒂛' // 𒂛 ?

map['ga2.times.nun%nun'] = '𒃡'
map['ga2%ga2'] = '𒃭'
map['gad%gad.gar%gar'] = '𒃱'
map['gal.gad%gad.gar%gar'] = '𒃳'
map['gan2%gan2'] = '𒃹'
map['gi4%gi4'] = '𒄅'
map['gud%gud.lugal'] = '𒄡'
map['idim%idim.bur'] = '𒅃'
map['idim%idim.squared'] = '𒅄'
map['igi%igi.shir%shir.ud%ud'] = '𒅉'
map['kad5%kad5'] = '𒆔'
map['kal%kal'] = '𒆙' // kal x kal ?
map['kaskal.lagab.times.u%lagab.times.u'] = '𒆝'
map['kaskal%kaskal.lagab.times.u%lagab.times.u'] = '𒆞'
map['kisim5%kisim5'] = '𒆩'
map['ku%hi.times.ash2.ku%hi.times.ash2'] = '𒆫'
map['lagar.gunu%lagar.gunu.she'] = '𒇰'
map['lugal%lugal'] = '𒈘  '
map['lum%lum'] = '𒈞'
map['lum%lum.gar%gar'] = '𒈟'
map['mu%mu'] = '𒈭'
map['mush%mush'] = '𒈶'
map['muš%muš'] = '𒈶'
map['mush%mush.times.a.plus.na'] = '𒈷'
map['nun.lagar.times.sal%nun.lagar.times.sal'] = '𒉧'
map['nun%nun'] = '𒉪'
map['nun.crossing.nun.lagar%lagar'] = '𒉬'
map['sag%sag'] = '𒊧'
map['she%she.gad%gad.gar%gar'] = '𒊼'
map['she%she.tab%tab.gar%gar'] = '𒊽'
map['shir%shir.bur%bur'] = '𒋕'
map['shu%inverted.shu'] = '𒋘'
map['sig4%sig4.shu2'] = '𒋟'
map['su%su'] = '𒋣'
map['tab%tab.ni%ni.dish%dish'] = '𒋱'
map['tir%tir'] = '𒌃'
map['tir%tir.gad%gad.gar%gar'] = '𒌄'
map['tur%tur.za%za'] = '𒌊'
map['u%u.pa%pa.gar%gar'] = '𒌎'
map['u%u.sur%sur'] = '𒌏'
map['u%u.u.reversed%u.reversed'] = '𒌐'
map['zi%zi'] = '𒍤'

lowers = {
    '₀': '0', '₁': '1', '₂': '2', '₃': '3', '₄': '4', '₅': '5', '₆': '6', '₇': '7', '₈': '7', '₉': '9',
}

function norm(text) {
    for ([dig, big] of lowers) {
        text = text.replaceAll(dig, big)
    }
    text = text.replace(/1\/3/, '⅓')
    text = text.replace(/2\/3/, '⅔')
    text = text.replace(/^\|/, '')
    text = text.replace(/\|$/, '')
    text = text.replace(/~/g, ' ')
    text = text.toLower()
    text = text.replace(/".*/g, '')
    text = text.replace(/(\d+)/g, '$1 ')
    text = text.replace(/æ/g, 'ḫ')
    text = text.replace(/</g, ' ')
    text = text.replace(/>/g, ' ')
    text = text.replace(/«/g, ' ')
    text = text.replace(/»/g, ' ')
    text = text.replace(/,/g, ' ,')
    text = text.replace(/;/g, ' ;')
    // text=text.replace(/,/g,' ')
    // text=text.replace(/;/g,' ')
    text = text.replace(/:/g, ' ')
    text = text.replace(/=/g, ' = ')
    text = text.replace(/_/g, ' ')
    text = text.replace(/-/g, ' ')
    text = text.replace(/ -/g, '-')
    text = text.replace(/˹/g, ' ')
    text = text.replace(/˺/g, ' ')
    text = text.replace(/⸢/g, ' ')
    text = text.replace(/⸣/g, ' ')
    text = text.replaceAll("dsa2", 'd sa2') // todo d...
    text = text.replaceAll("dnu", 'd nu')
    text = text.replaceAll("den", 'd en')
    text = text.replaceAll("dnin", 'd nin')
    text = text.replaceAll("/", ' ')
    text = text.replace(/\?/g, ' ')
    text = text.replace(/\\/g, ' ')
    
    text = text.replace(/\[/g, '')
    text = text.replace(/\]/g, '')
    text = text.replace(/\|/g, ' ')
    text = text.replace(/\s+@t/, '@t')
    // text=text.replace(/\s+@t/,'/tenu/')
    text = text.replace(/\./g, ' ')
    text = text.replace(/ x /g, '×')
    text = text.replace(/ \* /g, '×')
    text = text.replace(/\*/g, '×')
    text = text.replace(/ TIMES /g, '×')
    text = text.replace(/sh/g, 'š') // !
    text = text.replace(/Ḫ/g, 'H') // !
    text = text.replace(/ḫ/g, 'h')
    text = text.replace(/c/g, 'š')
    text = text.replace(/j/g, 'ĝ')
    text = text.replace(/ng/g, 'ĝ')
    text = text.replace(/g̃/g, 'ĝ')
    // text = text.replace(/ĝ/g, 'ng')
    text = text.replace(/dN/g, 'd N')
    text = text.replace(/(\d+)\s*×/g, '$1×') // todo: HOW?
    return text
}


// text_to_cuneiform
function norm_cuneiform(text) {
    text = text.replace(/(\d+)/g, '$1 ')
    text = text.replace(/cuneiform/g, "")
    text = text.replace(/mušen/g, " mušen ")
    text = text.replace(/ĝeštin/g, "ĝeshtin")
    // text=text.replace(/ĝeštin/g,"𒃾")
    text = text.replace(/gia/g, "gi a")
    text = text.replace(/ĝeš/g, " ĝeš ")
    text = text.replace(/ĝiš/g, " ĝeš ")
    text = text.replace(/uruda/g, " 𒍏 ")
    text = text.replace(/urudu/g, " 𒍏 ")
    text = text.replace(/urud/g, " 𒍏 ")
    // text=text.replace(/kuš1/g," kush1 ")
    // text=text.replace(/kuš2/g," kush2 ")
    // text=text.replace(/kuš3/g," kush3 ")
    // text=text.replace(/kuš4/g," kush4 ")
    text = text.replace(/urud/g, " urud ")
    text = text.replace(/ -/g, '-')
    text = text.replace(/\+/g, ' ')
    text = text.replace(/&/g, ' ')
    text = text.replace(/\!/g, '')
    text = text.replace(/\r\n/g, '\n')
    text = text.replace(/\n/g, ' \n ')
    text = text.replace("Akk. ", "#")
    text = text.replace("wr. ", "")
    text = text.replace(";", "; ")
    // text=text.replace("  "," ㅤ ") // invisible space to keep distance
    text = text.replace(/\(\|(.*?)\|\)/g, " $1 ") //  adx(|BAD.LU2|); adx(|LU2×GAM|);
    text = text.replace(/\(/g, " ( ")
    text = text.replace(/\)/g, " ) ")
    text = text.replaceAll('\\|', ' ')
    text = text.replaceAll('2  ×', '2×') // todo: HOW?
    text = text.replaceAll('3  ×', '3×') // todo: HOW?
    text = text.replace(/(\d+)\s*×/g, '$1×') // todo: HOW?
    text = text.replace('sag@n', "𒊔")
    text = text.replaceAll('á ', 'a2 ')
    text = text.replaceAll('à ', 'a3 ')
    text = text.replaceAll('í ', 'i2 ')
    text = text.replaceAll('ì ', 'i3 ')
    // text = text.replaceAll('ḳ', 'k/g') // wtf Abulhab 𒃲 ḳal but ḳi = ki
    // text = text.replace(/ṭ/g, 't') // NO ṭa -> da 𒁕

    // text = text.replaceAll('án ', 'an2 ') 
    // text = text.replaceAll('àn ', 'an3 ')
    // text = text.replaceAll('ín ', 'in2 ')
    // text = text.replaceAll('ìn ', 'in3 ')
    // text = text.replaceAll('ág ', 'ag2 ') 
    // text = text.replaceAll('àg ', 'ag3 ')
    // text = text.replaceAll('íg ', 'ig2 ')
    // text = text.replaceAll('ìg ', 'ig3 ')

    // text=text.replace(/\(.*?\)/g,"")
    // text=text.replace(/\[.*?\]/g,"")
    // text=text.replace(/\(.*?\)/g,"")
    return text
}


function add_variants(trans, glyph, ignore_duplicates = 0) {
    if(!trans)
        return;
    if(trans[0] == "#")
        return;
    if (!glyph)
        return
    if (glyph[0] == "#")
        return
    if(trans!='\n') trans = trans.trim()
    trans = trans.replace("_", "")
    trans = trans.replace(" x ", "×")
    trans = trans.replace(" /tenu/", "@t") //without space!
    trans = trans.replace(" /tenû/", "@t")
    trans = trans.replace(" /gunu/", "@g")
    trans = trans.replace(" /gunû/", "@g")
    trans = trans.replace(" opposing ", "@")
    trans = trans.replace(/Ḫ/g, 'H') // !
    trans = trans.replace(/ḫ/g, 'h')
    trans = trans.replace(/ @g/g, '@g')
    // todo sín <> sin2 etc
    // trans=trans.replace(/ĝ/g,'g')
    // glyph=map[glyph]||glyph
    // glyph=map[glyph.replaceAll("|","")]||glyph
    trans = trans.trim()
    if (!trans) return
    // console.log(glyph,trans)
    if (map[trans]) {
        // if(map[trans]!=glyph)
        //     console.log(trans+" <- "+map[trans]+" ≠ "+glyph)
        // throw trans+" <- "+map[trans]+" ≠ "+glyph
    } else map[trans] = glyph.trim()
}



function load_signs() {
    csv = read_csv(list_file,'\t')
    for (line of csv) {
        name = line[0]
        glyph = line[1]
        if (!name) continue
        add_variants(name, glyph)
        add_variants(name.lower(), glyph)
        add_variants(name.replace(/\d+/, ""), glyph, 1)
        add_variants(name.lower().replace(/\d+/, ""), glyph, 1)
    }
}



function load_signs1() {
    csv = read_csv(list_file1)
    // 𒀉       U+12009         A_2     560     334     ID
    for (line of csv) {
        glyph = line[0]
        trans = line[1]
        alts = line[4]
        if (!trans) continue
        add_variants(trans, glyph)
        add_variants(trans.lower(), glyph)
        add_variants(trans.replace(/\d+/, ""), glyph, 1)
        add_variants(trans.lower().replace(/\d+/, ""), glyph, 1)
        if (!alts) continue
        for (alt of alts.split(",")) {
            add_variants(alt, glyph)
            add_variants(alt.lower(), glyph)
        }
    }
    // break
}


function load_signs2() {
    csv = read_lines(list_file2)
    for (line of csv) {
        if (!line || line == "") continue
        cols = line.split("\t")
        trans = cols[0]
        glyph = cols[1]
        if (!trans || !glyph)
            continue
        if (trans.contains('Note')) break;
        glyph = glyph.replace("?", "")
        glyph = glyph.trim()
        trans = trans.trim()
        add_variants(trans, glyph)
        add_variants(trans.lower(), glyph)
        add_variants(trans.replace(/\d+/, ""), glyph, 1)
        add_variants(trans.lower().replace(/\d+/, ""), glyph, 1)
    }
}


function load_signs3() {
    csv = read_lines(list_file3)
    for (line of csv) {
        // if(line.indexOf("silaĝ")>=0)
        // console.log(line)
        if (!line || line == "") continue
        cols = line.split(" ")
        let dest = cols[0]
        let src = cols[1]
        if (!src || !dest)
            continue
        // if (src.indexOf('-') >= 0) {
        //     ts = src.split('-')
        //     gs = dest.split('.')
        //     if (ts.size() == gs.size())
        //         for (var i = 0; i < ts.size(); i++)
        //             add_variants(ts[i], gs[i])
        // }
        if (src.contains('Note')) break;
        dest = dest.replaceAll("|", "")
        dest = dest.replace("?", "")
        dest = dest.trim()
        src = src.trim()
        dest = map[dest] || map[norm(dest)] || map[norm_cuneiform(dest)] || cuneiformize(dest) || "?" //glyph
        if (dest == "?") {
            console.log("unknown glyph: "+line)
            continue
        }
            // if(line.indexOf("zug4")>=0) {
            //     console.log(line)
            //     console.log(src,">>>>>>>>>>>>",map[src],"\t",dest)
            // }
            // continue
        if (src != dest && src.indexOf(".") < 0 && src.indexOf("×") < 0 && src.indexOf("-") < 0  && src.indexOf("|") < 0 && dest != "𒋗" && !map[src]) {
            console.log(src, "\t", dest)
        }

        add_variants(src, dest)
        add_variants(src.lower(), dest)
        add_variants(src.replace(/\d+/, ""), dest, 1)
        add_variants(src.lower().replace(/\d+/, ""), dest, 1)
    }
}


map[""] = ""
map["("] = "("
map[")"] = ")"
map[";"] = ";"
map["'"] = "'"
map[","] = ","
map[" "] = " "
map["\t"] = " "
map["-"] = " "
map["_"] = " "
map["|"] = "|"

function find_unknown(word) { // hack
    return word // should be part of list_file3
}


// Function to convert number to cuneiform
function numberToCuneiform(num) {
    if(num=="1/2")return "𒈦"
    if(num=="1/4")return "𒑠"
    if(num=="1/3")return "𒑚"
    if(num=="2/3")return "𒑛"
    const cuneiformDigits = ['𒁹', '𒁹𒁹', '𒁹𒁹𒁹', '𒐼', '𒐊', '𒐋', '𒑂', '𒑄', '𒑆'];
    const cuneiformTen = '𒌋';
    
    let result = '';
    let tens = Math.floor(num / 10);
    let units = num % 10;
    
    // Add cuneiform tens
    for (let i = 0; i < tens; i++) 
        result += cuneiformTen;
    // Add cuneiform units
    if (units > 0) 
        result += cuneiformDigits[units - 1];
    return result;
}

// text_to_cuneiform
function cuneiformize(text) {
    if(map[text])return map[text]
    text = norm(text)
    if(map[text])return map[text]
    text = text.toLower()
    if(map[text])return map[text]
    text = norm_cuneiform(text).trim()
    if(map[text])return map[text]
    if(text[0]=='b' &&  map[text.substring(1)]) 
        return map[text.substring(1)]; // in https://ehammurabi.org/#laws '⫻'
    if(text[0]=='d' &&  map[text.substring(1)]) 
        return '𒀭'+map[text.substring(1)]; // devine … ! an
    if(text.startsWith("ilu") &&  map[text.substring(3)])
        return '𒀭' + map[text.substring(3)]
    // if(text.startsWith("ilu") || text.startsWith("ili"))
    //     return '𒀭'
    chars = text.split(' ')
    trans = ""
    for (char of chars) {
        if(char!="\n")
        char=char.strip()
        if (!char || char == "") continue
        if(typeof char == 'number' || Number.isInteger(char) || int(char)>0){
            trans += numberToCuneiform(char) + " "
            continue
        }
        if (char.startsWith("×")) char = char.substring(1, char.length)
        let glyph = map[char] || "?"
        if (glyph.indexOf("|") >= 0)
            glyph.replace("\|(.*?)\|", (_, match) => map[match] || match + "!!!");


        if (char.startsWith("lak")) glyph = "¿"
        if (char.length < 2 && glyph == "𒍮")
            continue // BUG!?
        if (char.contains("×") && glyph == "?")
            glyph = char.split("×").map(c => map[c] || c).join("·")
        if (glyph == "?" && char.startsWith("LAK"))
            glyph == "¿" // lacking / lacunae
        if (glyph == "?" && char.startsWith("šim"))
            glyph = "𒋆" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("kaš"))
            glyph = "𒁉" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("dug"))
            glyph = "𒂁" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("ku6"))
            glyph = "𒄩" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("kuš"))
            glyph = "𒋢" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("din"))
            glyph = "𒁷" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("uzu"))
            glyph = "𒍜" + (map[char.substring(3, char.length)] || "?")
        if (glyph == "?" && char.startsWith("munus"))
            glyph = "𒊩" + (map[char.substring(5, char.length)] || "?")
        if (glyph == "?" && char.startsWith("zabar"))
            glyph = "𒌓𒊕𒁇" + (map[char.substring(5, char.length)] || "?")
        if (glyph == "?" && char.endsWith("ku6"))
            glyph = map[char.substring(0, char.length - 3)] + "𒄩"
        if (glyph == "?" && char.endsWith("zabar"))
            glyph = map[char.substring(0, char.length - 5)] + "𒌓𒊕𒁇"
        if (glyph == "?" && char.endsWith("tug2"))
            glyph = map[char.substring(0, char.length - 4)] + "𒌆"
        if (glyph == "?" && char.startsWith("gi"))
            glyph = "𒄀" + (map[char.substring(2, char.length)] || "?")
        if (glyph == "?" && char.startsWith("za"))
            glyph = "𒍝" + (map[char.substring(2, char.length)] || "?")
        if (glyph == "?" && char.startsWith("im"))
            glyph = "𒅎" + (map[char.substring(2, char.length)] || "?")
        if (glyph == "?" && char.endsWith("gi"))
            glyph = (map[char.substring(2, char.length)] || "?") + "𒄀"
        if (glyph == "?" && char.endsWith("še"))
            glyph = (map[char.substring(2, char.length)] || "?") + "𒊺"
        if (glyph == "?" && char.endsWith("sar"))
            glyph = (map[char.substring(0, char.length - 3)] || "?") + "𒊬"
        if(glyph=="?" && char.strip())
            console.error("unknown",char)
        glyph = map[glyph] || glyph
        trans += glyph
    }
    if (trans == "?" && (text.endsWith("-") || !text.contains("-"))) return ""
    return trans
}

// text_to_cuneiform
function print_cuneiform(text) {
    if(!text)return
    orig = text
    // console.log(text)
    text = norm(text)
    text = norm_cuneiform(text)
    console.log(text)
// chars = text.split(/\s+/g) // kills newlines
    chars = text.split(" ")
    trans = ""
    for (char of chars) {
        glyph = map[char.trim()] || char
        if(char=='\n'){glyph='\n'}
        trans += glyph
        // process.stdout.write(glyph)
        // process.stdout.write(" ")
    }
    // trans=trans.replace(/ \)  ; /g,";  ")
    // trans=trans.replace(/  \( /g,"")

    trans = trans.replace(/\)/g, " ") // sometimes desired!
    trans = trans.replace(/\(/g, " ")
    trans = trans.replace(/ ;/g, "\n") // todo remove after Assurbanipal hack!
    console.log(trans)
}

load_signs()
load_signs1()
// load_signs2() # DEPRECATED see and use cuneiform.list !
// load_signs3()
for (x in map) {
    map[x.toUpper()] = map[x]
    map[x.toLower()] = map[x]
}
// fuck hammurabi
for (x in map) {
    if(!map['b'+x])
        map['b'+x] = map[x]
    if(!map['m'+x])
        map['m'+x] = '𒁹' + map[x]
    if(!map['d'+x])
        map['d'+x] = '𒀭' + map[x]
    if(!map[x+'ki'])
        map[x+'ki'] = map[x]+'𒆠'
}


cune = cuneiformize

if (module.parent) {
    // imported
} else {
    // main
    text = process.argv.slice(2, process.argv.length).join(" ")
    // if (!text) text = "~/uruk_egypt/texts/sumerian/x" // test!!
    if (!text) text = "~/dev/script/julia/x" // test!!
    if (!text) process.stdin.on('data', function (pipe) {
        cuneiformize("" + pipe)
    })
    text = text.replace("~/", "/Users/me/")
    if (is_file(text)) text = read_text(text)
    // print_cuneiform(text)
    cun = cuneiformize(text)
    if(cun) console.log(cun)
}

module.exports = {cuneiformize}
