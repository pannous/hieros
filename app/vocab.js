#!/usr/bin/env node
// #!/usr/bin/nodemon  # nodemon ./vocab.js For automatic reload
var fs = require('fs');
var express = require('express');
var app = express();
var port=process.env.PORT || 3000
var debug = require('debug')('uruk egypt:server');

// app.set('view engine', 'ejs') jade-- ejs haml-- pug         NO just use template=`<html>${content}</html>`
// app.use(express.static('fonts'));
app.use('/fonts', express.static('fonts'));
app.use('/script', express.static('script'));

require('./script/hieroglyps-vocab.js');
// require('./script/gardiner_input.js'); IN BROWSER!
require('./extensions.js');

title='Gardiner search'
html=read("vocab.html")
template=()=>eval("`"+html+"`") // ()=>`...`

// app.post('/add', // npm install --save body-parser WTF
last=""
      
// app.post('/add',(req, res) => {
//       req.body.q  vs req.query in 'GET'

app.get('/add',(req, res) => {
      // console.log( req)
	line=req.query.h
	text=req.query.x||req.query.q
    if(text && text!=last && text.length>0)
	    fs.appendFile('changes.txt',line+"\n"+text+"\n\n")
       text=last
	res.status(204);// no content
	res.send("{'OK'}")//  necessary, what the flock
});


app.get('/', (req, res) => {
	q=req.query.q||"sankt" // Do not trim in order to preserve proper words
	content=find_word(q).join("<br/>")
	content=template(content,q)
	  res.send(content)// ONLY ONCE!
});

app.listen(port, function () {
	loadVocab();
	// console.log(find_word("mouth"));
	  console.log('Example app listening on http://localhost:'+port);
	//  try{
	 // 	browse("http://localhost:"+port)	  	
	 // }catch(ex){}
});
