// "use strict"
const netbaseUrl = "http://localhost:8181/html/all/"
const netbaseAll = "http://localhost:8181/json/all/"
const netbaseAbs = "http://localhost:8181/json/abstract/"
const searchUrl = "https://ixquick.com/do/metasearch.pl?q="
let mod_key = 0
let nodeIds = []
let parsedIds = []
let entities = []
let leaves = []
let statementIds=[]
let entityIndex = 0
let leaveIndex = 0
let statementIndex = 0
let currentID = 0
let EDGE_LIMIT = 20 // see limit field!
const RESULT_LIMIT = 4
const LOADING = 'loading...'
let loading = [{ id: LOADING, label: LOADING }]
// let nodeArray=[
//       {id: 'a1', value: 2 ,label: 'Node 1', title: '3 emails per week'},
//       {id: 'a2', value: 4 ,label: 'Node 2',image:"https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/MfS_wanzen.jpg/150px-MfS_wanzen.jpg", shape: 'image' },
//       {id: 'a3', value: 12,label: 'Node 3',image:"150px-MfS_wanzen.jpg", shape: 'image'},
//       {id: 'a4', value: 22,label: 'Node 4'},
//       {id: 'a5', value: 0 ,label: 'Node 5'}
//     ];
// let edgeArray=[
//       {from: 'a1', to: 'a3', value: 2 },
//       {from: 'a1', to: 'a2', value: 4},
//       {from: 'a2', to: 'a4', value: 12, color:"black",id:111},
//       {from: 'a2', to: 'a5', value: 22},
//       {from: 'a3', to: 'a5', value: 0 }
//     ]
edgeArray = []
nodeArray = [] 
nodeArray = loading

    // nodeArray=localStorage.getItem('nodes')
    // edgeArray=localStorage.getItem('edges')
let nodes = new vis.DataSet(nodeArray);
let edges = new vis.DataSet(edgeArray);
let network;
let container = document.getElementById('mynetwork');
let isNumber = n => !isNaN(parseInt(n)) && isFinite(n); // parseInt() "SHOULD NOT BE USED".
let data = {
    nodes: nodes,
    edges: edges
};
let options = {
      // nodes: {
      //  selectionWidth: 20
      // },
    // selectionWidth: function (width) {return width*2;}
      //   // shape: 'dot',
      //   scaling:{
      //     label: {
      //       min:8,
      //       max:20
      //     }
      //   }
      // }

  groups: {
    myGroup: {color:{background:'red'}, borderWidth:3}
  },

    interaction: {
        tooltipDelay: 100,
        zoomView: true,
        hover:true
            //   navigationButtons: true,
            //   keyboard: true
            // selectable: true,
            // selectConnectedEdges: true,
            // multiselect: false,
    },
    // manipulation: {
    //      addNode: addNode,
    //      editNode: editNode,
    //      addEdge: addEdge
    //  },
    physics: {
        // animation: false,
        stabilization: {
            // enabled: false,
            enabled: true,
            // iterations: 1, // maximum number of iteration to stabilize
            // // updateInterval: 10,
            // onlyDynamicEdges: false,
            // fit: true
        },
        // timestep: 1
    }
    // physics: {stabilization:{enabled:true}}
};
network = new vis.Network(container, data, options); // 
network.setOptions({ physics: { stabilization: { enabled: true } } });
network.on('select', nodeSelected);
network.on('keypress', keydown);
network.on('wheel', MouseWheelHandler);
// network.on('hoverNode', traceBack);

// canvas.onwheel=MouseWheelHandler
document.addEventListener("mousewheel", MouseWheelHandler, false);
document.addEventListener("DOMMouseScroll", MouseWheelHandler, false); // Firefox
document.onmousedown = event => {
    if (event.button == 2) return false };
document.oncontextmenu = document.body.oncontextmenu = function() {
    return false; }
to.focus()
function get(id){
  return nodes.get(id||currentID)
}function getEdge(id){
  return edges.get(id)
}
function wiki(page){
    window.open('https://de.wikipedia.org/wiki/'+page,"_blank")
}
function wikiData(id){
    window.open('https://www.wikidata.org/wiki/Q'+id,"_blank")
}

function selectNode(id) {
    setEntity(id)
    var n = nodes.get(id)
    from_id.valueAsNumber = id
    from.value = n.label
}

function makeEntity(entity) {
    if (entity.name.match('http')) return;
    title=(entity.description || "") + " (" + entity.id + ")"
    if (nodeIds.indexOf(entity.id) < 0) {
      type=entity.class || entity.type || entity.topic
      label=entity.name 
      if(type&&type!="entity"&&type!="undefined")label= label+ ": " + type
      makeNode(entity.id,label,title,entity.image)// value:entity.statementCount
    }else{
      if(!entity.image) nodes.update([{ id: entity.id, title:title}])
      else nodes.update([{ id: entity.id, title:title,image:entity.image,shape:'image'}])
    }
    max = EDGE_LIMIT
    for (key in entity.statements) {
        if (!max--) break;
        makeStatement(entity.statements[key])
    }
}

function makeStatement(statement, elem) {
    if (!statement || !statement.object) return
    if (statement.object.match('.jpg')) return;
    if (statement.object.match('http')) return;
    if (statement.object.match(/^\d$/)) return;
    oid = statement.oid
    sid = statement.sid
    makeNode(sid,statement.subject, statement.title || sid ,0,statement.predicate);
    makeNode(oid,statement.object, statement.title || oid, 0,statement.predicate);
     // label: statement.predicate, pid value: 1,
    if (sid != oid && statementIds.indexOf(statement.id)<0){
        edges.add({ id: statement.id, pid: statement.pid, from: sid, to: oid, title: statement.predicate, arrows: 'middle', font: { align: 'top' } });
        statementIds.push(statement.id)
      }
    // entityStatements[entityIndex][statementIndex++] = oid
}

function parseResults(results0) {
    network.stabilize();
    network.setOptions({ physics: { stabilization: { enabled: true } } });
    deleteNode(LOADING)
    if (results0) results = results0;
    max = RESULT_LIMIT;
    for (key in results['results']) {
        if (!max--) break;
        entity = results['results'][key]
        if (!entity.id) break; // how?
        console.log(entity);
        makeEntity(entity);
        if (entity.name == from.value) addWord(entity.id)
        entities.push(entity.id)
    }
    setEntity(currentID)
    network.setOptions({ physics: { stabilization: { enabled: true } } });
}

function processStatus(response) {
    if (response.status === 200 || response.status === 0) { // "0" to handle local files 
        console.log('netbase OK');
        response.json().then(data => parseResults(data)); // WTF
        return Promise.resolve(response)
    } else {
        return Promise.reject(new Error(response.statusText))
    }
};

function postText(h, text) {
    try {
        let http = new XMLHttpRequest();
        http.open("GET", text, true);
        http.send();
    } catch (x) {
        log(x)
    }
}

// function clearPopUp() {
//     document.getElementById('saveButton').onclick = null;
//     document.getElementById('cancelButton').onclick = null;
//     document.getElementById('network-popUp').style.display = 'none';
// }

// function cancelEdit(callback) {
//     clearPopUp();
//     callback(null);
// }
// function addNode(data, callback) {
//     document.getElementById('operation').innerHTML = "Add Node";
//     document.getElementById('node-id').value = data.id;
//     document.getElementById('node-label').value = data.label;
//     document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
//     document.getElementById('cancelButton').onclick = clearPopUp.bind();
//     document.getElementById('network-popUp').style.display = 'block';
// }

// function editNode(data, callback) {
//     document.getElementById('operation').innerHTML = "Edit Node";
//     document.getElementById('node-id').value = data.id;
//     document.getElementById('node-label').value = data.label;
//     document.getElementById('saveButton').onclick = saveData.bind(this, data, callback);
//     document.getElementById('cancelButton').onclick = cancelEdit.bind(this, callback);
//     document.getElementById('network-popUp').style.display = 'block';
// }
// function saveData(data, callback) {
//     data.id = document.getElementById('node-id').value;
//     data.label = document.getElementById('node-label').value;
//     clearPopUp();
//     callback(data);
// }

// function addEdge(data, callback) {
//     if (data.from == data.to) {
//         console.log('please drag');
//         // let r = confirm("Do you want to connect the node to itself?");
//         // if (r == true) {
//         //   callback(data);
//         // }
//     } else {
//         callback(data);
//     }
// }
let turk = '#00FFFF'
let cyan = '#FF00FF'
let neutral = '#FFFFFF'
let blueHiglight='#00FFFF'
let lastHighlight = 0
function unhighlightNode(id){
    if(id)nodes.update([{ id: id, color: { background: neutral }}]);
    lastHighlight=0
}
function highlightNode(id,leave=true) {
  if(!id)return;
    // let random = '#' + Math.floor((Math.random() * 255 * 255 * 255)).toString(16);
    if(lastHighlight)
    nodes.update([{ id: lastHighlight, color: { background: neutral }}]);
    nodes.update([{ id: id, color: { background: turk } }]);
    if(leave)
    lastHighlight = id
}

function clearNet() {
    console.log('clearNet');
    nodeIds = []
    parsedIds = []
    entities = []
    leaves = []
    nodes.clear();
    edges.clear();
    nodeArray = [{ id: LOADING, label: LOADING }]
    nodes = new vis.DataSet(nodeArray);
    setTheData();
}

function resetAllNodes() {
    console.log('resetAllNodes!');
    setTheData()
}

function setTheData() {
    // nodes = new vis.DataSet(nodeArray);
    // edges = new vis.DataSet(edgeArray);
    network.setData({ nodes: nodes, edges: edges })
        // network.stabilize();
        // network.fit();
}

let last = 0

function setNode(id) {
    clearNet()
    fetchWord(id)
}

function edgeSelected(params) {
    id = params.edges[0]
    if (!id) return
        // console.log(id);
    edge_ = edges.get(id)
    edge_id.valueAsNumber = edge_.pid
    edge.value = edge_.label || edge_.title || "see"
    from.value = "$" + id
        // selection.innerHTML = 'EDGE: ' + edge_.id+" "+edge_.label
}

function nodeSelected(params) {
    id = params.nodes[0]
    e = params.event.srcEvent
    e.preventDefault()
    e.stopPropagation()
    console.log(params);
    if (!id) return edgeSelected(params)
    mod_key = e.metaKey || e.altKey || e.optionKey || e.controlKey || e.ctrlKey
    node = nodes.get(id)
        // node=nodeArray.filter(x=>x.id==id)[0]
        // if(node)
    if (mod_key) {
        // selection.innerHTML = 'TO: ' + node.label
        to.value = node.label
        to_id.valueAsNumber = node.id
        submit()
    } else if (e.shiftKey) {
        addWord(id)
    } else {
        if (params.event.tapCount == 2) return setNode(id);
        // selection.innerHTML = 'FROM: ' + node.label
        from_id.valueAsNumber = id
        from.value = node.label
        addWord(id)
    }
}


function makeNode(id, label,title,image,group) {
    if(isNumber(label))return 0;
    if(label=="◊")return 0;
    id = id || "TEMP-"+(Math.random() * 1e7).toString(32);
    if (nodeIds.indexOf(id) < 0) {
        nodeIds.push(id);
        label = label || id
        label = label.trim()
        // label = label.replace(" ", "_")
        color={background:'rgba(195,225,255,0.9)', border:'#3060B0',highlight:{background:'cyan',border:'blue'}}
        if(!image) nodes.add({ id: id, label: label,title:title,color: color,group:group,useDefaultGroups:group?0:1 })
        else nodes.add({ id: id, label: label,title:title,color: color,image:image, shape: 'image' })
        nodeArray.push({ id: id, value: 4, label: label,title:title })
        leaves.push(id)
    }
    return id;
}

function submit(former) {
    console.log('learn!');
    // selection.innerHTML = from.value
    // localStorage.setItem(from.value, to.value);
    // ok = localStorage.getItem(from.value);
    // console.log(ok);
    if (!from_id.valueAsNumber) makeNode(from.value)
    if (!to_id.valueAsNumber) to_id.value = makeNode(to.value)
    id = from_id.valueAsNumber || from.value
    eid = edge_id.valueAsNumber || edge.value || "see"
    tid = to_id.valueAsNumber || to.value.trim().replace(" ", "_")
    edges.add({ from: id, to: tid, value: 2, label: edge.value, font: { align: 'top' } });
    edgeArray.push({ from: id, to: tid, value: 2, label: edge.value })
    fetch('http://localhost:8181/llearn%20' + encodeURIComponent(id + " " + eid + " " + tid))
        // postText

    // localStorage.setItem('nodes', nodes._data);
    // localStorage.setItem('edges', edges._data);
    // localStorage.setItem('nodeIDs',nodeIDs);
    // nodeIds.push(from_id.value);
    // document.forms[0].submit()
}

function keyup(event) {
    // mod_key=0
}
Array.prototype.deduplicate = function(){return this.filter((item, pos)=>this.indexOf(item) == pos)};
Array.prototype.remove = function(x) { var i = this.indexOf(x); if (i > -1) this.splice(i, 1); return this } 
Array.prototype.contains = function(x) { return this.indexOf(x)>=0}
// 'this' NOT AVAILABLE FOR LAMDA!

function deleteNode(id) {
    if (id != LOADING){
      console.log('deleteNode');
      id = id || from_id.valueAsNumber
      leaves.map(l=>{if(!parsedIds.contains(l))nodes.remove({id:l})})
      nodeIds.remove(id)
      entities.remove(id)
      leaves=[]
      nextEntity()
    }
    nodes.remove({ id: id });
}

function destroyNode(id) {
    console.log('destroyNode');
    id = id || from_id.valueAsNumber
    deleteNode(id)
    if (id != LOADING) fetch('http://localhost:8181/ddelete%20' + id)
}

function deleteEdge(id) {
    id = id || parseInt(from.value.match(/\d+/))
        // id = id||from_id.valueAsNumber
    edges.remove({ id: eid })
    fetch('http://localhost:8181/ddelete%20' + "$" + eid)
}

function fetchNet() {
    console.log('fetchNet');
    // clearNet()
    n0 = localStorage.getItem('nodes')
    nodeArray = eval(localStorage.getItem('nodes'))
    edgeArray = eval(localStorage.getItem('edges'))
    console.log(n0);
    console.log(nodeArray);
    edges = new vis.DataSet(edgeArray);
    nodes = new vis.DataSet(nodeArray);
    network.setData({ nodes: nodes, edges: edges })
    network.stabilize();
    network.fit();
    // resetAllNodes()
}

function saveNet() {
    console.log('saveNet');
    localStorage.setItem('nodes', nodeArray.toSource()); // only string :(
    localStorage.setItem('edges', edgeArray.toSource())
}

function fetchWord(word) {
    clearNet()
    addWord(word);
    entityIndex = 0
}
function setEntity(word){
  if(!word)return;
  var old=currentID
  currentID=word
  entities.push(word)
  entities=entities.deduplicate()
  var gedges=edges.get().filter(e=>e.to==currentID||e.from==currentID)
  leaves=gedges.map(e=>e.to==currentID?e.from:e.to)
  leaves=leaves.deduplicate()// dedup
  if(!get(word))from.value=word
  else{
    from_id.value=word
    from.value=get(word).label
    selection.innerHTML=get(word).title
  }
  console.log('currentID '+currentID);
  if(old)nodes.update([{ id: old, color: { background: neutral } }]);
  nodes.update([{ id: currentID, color: { background: cyan } }]);
  lastHighlight=0
}
function addWord(word) {
    if (!word) return
    unhighlightNode(currentID); 
    if (entities.indexOf(word) < 0) entities.push(word)
    if (parsedIds.indexOf(word) >= 0)return setEntity(word);
    parsedIds.push(word)
    currentID=word
    EDGE_LIMIT = limit.value || EDGE_LIMIT
    if (word && isNumber(word))
        fetch(netbaseAll + encodeURIComponent(word) + "+limit+" + EDGE_LIMIT).then(processStatus)
    else{
        makeNode(0,word)// TEMP!
        fetch(netbaseAbs + encodeURIComponent(word.replace(/\s*\:.*/, "")) + "+limit+" + EDGE_LIMIT).then(processStatus)
      }
        // fetch("http://de.netbase.pannous.com:81/json/verbose/"+encodeURIComponent(word)+"+limit+5").then(processStatus)
        // parseResults(data);
}

entityIndex = 0
statementIndex = 0

function nextEntity() {
    entityIndex = (entityIndex + 1) % entities.length
    leaveIndex=0
    word = entities[entityIndex]
    addWord(word)
    console.log('entityIndex ' + entityIndex + " / " + entities.length);
    console.log('word ' + word);
}

function previousEntity() {
    entityIndex = (entityIndex - 1) % entities.length
    leaveIndex=0
    if(entityIndex<0)entityIndex=entities.length-1
    word = entities[entityIndex]
    addWord(word)
    console.log('entityIndex ' + entityIndex + " / " + entities.length);
    console.log('word ' + word);
}


function nextLeave() {
    leaveIndex = (leaveIndex + 1) % leaves.length
    word = leaves[leaveIndex]
    highlightNode(word)
    console.log('leaveIndex ' + leaveIndex + " / " + leaves.length);
    console.log('word ' + word);
}

function previousLeave() {
    leaveIndex = (leaveIndex - 1) % leaves.length
    if(leaveIndex<0)leaveIndex=leaves.length-1
    word = leaves[leaveIndex]
    highlightNode(word)
    console.log('leaveIndex ' + leaveIndex + " / " + leaves.length);
    console.log('word ' + word);
}

function MouseWheelHandler(event) {
    console.log('MouseWheelHandler');
    // console.log(event);
    event.stopPropagation();
    event.preventDefault()
        // event.cancel()
    event.bubbles = false
    return false
}

function cancelEvent(event) {
    // console.log('cancelEvent');
    console.log(event);
    event.stopPropagation();
    event.preventDefault();
}

function keydown(event) {
    var e = event
    var fo = from_id.valueAsNumber || from.value
    var word = to_id.valueAsNumber || to.value || fo
    var wo = encodeURIComponent(to.value || from.value) // no int
    var mod_key = e.metaKey || e.controlKey || e.ctrlKey // e.altKey ||e.optionKey || 
    if (event.key == "-") { edge_id.value = 0;
        edge.value = "not";
        cancelEvent(event) }
    // if (event.key == "!"){edge_id.value=0;edge.value="not" ;cancelEvent(event)}
    if (event.key == "!") { edge_id.value = 0;
        edge.value = "opposite";
        cancelEvent(event) }
    if (event.key == "≠") { edge_id.value = 0;
        edge.value = "not";
        cancelEvent(event) } // alt+==≠

    // if (event.key == "\\"){edge_id.value=0;edge.value="opposite" ;cancelEvent(event)}
    if (event.key == "\\") { edge_id.value = 0;
        edge.value = "versus";
        cancelEvent(event) }
    if (event.key == "?") { edge_id.value = 0;
        edge.value = "why/what";
        cancelEvent(event) }
    // if (event.key == "?"){edge_id.value=0;edge.value="maybe" ;cancelEvent(event)}
    if (event.key == ":") { edge_id.value = 0;
        edge.value = "is";
        cancelEvent(event) }
    if (event.key == "`") { edge_id.value = 0;
        edge.value = "instance";
        cancelEvent(event) }
    if (event.key == "~") { edge_id.value = 0;
        edge.value = "similar";
        cancelEvent(event) }
    if (event.key == "=") { edge_id.value = 0;
        edge.value = "equals";
        cancelEvent(event) }
    if (event.key == "/") { edge_id.value = 0;
        edge.value = "see";
        cancelEvent(event) }
    if (event.key == ">") { edge_id.value = 0;
        edge.value = "derives";
        cancelEvent(event) }
    if (event.key == "<") { edge_id.value = 0;
        edge.value = "derived";
        cancelEvent(event) }
    if (event.key == "i" && mod_key) { window.open(netbaseUrl + wo, '_blank');
        console.log('OK');
        return }

    if (mod_key && event.charCode > 0) {
        if (!event.key.match(/[acklrxv]/)) cancelEvent(event) }
    // && keyCode>0
    // if(mod_key){
    //   event.stopPropagation();
    //   event.preventDefault();
    // }

    if (event.key == "s" && mod_key) return saveNet()
    if (event.key == "l" && mod_key) {
        if (e.shiftKey) url = "https://dict.leo.org/chde/index_de.html#/search="
        else url = "https://dict.leo.org/ende/index_de.html#/search="
        window.open(url + wo, '_blank');
        console.log(url + wo);
    }
    
        // window.open(searchUrl+wo,'_blank')

    if (event.key == " " || event.keyCode==32){
          addWord(to.value);
          to.value="" //!!!
      // addWord(lastHighlight);selectNode(lastHighlight);
    }
    if (event.key == "g" && mod_key) { selectNode(word);
        if (e.shiftKey) addWord(word);
        else fetchWord(word) }
    if (event.key == "t" && mod_key) { selectNode(word);
        if (e.shiftKey) word = fo
        else addWord(word); }
    if (event.key == "f" && mod_key) { //
        if (!e.shiftKey) word = fo
        return fetchWord(word)
    }
    if (event.key == "a" && mod_key && e.shiftKey) return addWord()
        //     // return fetchNet()
        // }
    if (event.key == "n" && mod_key) { //
        cancelEvent(event)
        return fetchNet()
    }
    if (event.key == "ArrowDown") cancelEvent(event)
        // if (mod_key) deleteNode();

    switch (event.keyCode) {
        // case KeyEvent.DOM_VK_SPACE:
        //   addWord(currentID);
        //   break;
        case KeyEvent.DOM_VK_TAB:
            if (!e.shiftKey) {
                to.focus()
                to.select()
                cancelEvent(event)
            }
            break;
        case KeyEvent.DOM_VK_PAGE_DOWN:
            nextEntity()
            break;
        case KeyEvent.DOM_VK_PAGE_UP:
            previousEntity()
            break;
            // case KeyEvent.DOM_VK_RIGHT:
        case KeyEvent.DOM_VK_DOWN:
            nextLeave()
                // nextStatement()
            cancelEvent(event);
            break;
        case KeyEvent.DOM_VK_UP:
            // case KeyEvent.DOM_VK_LEFT:
            previousLeave()
                // previousStatement()
            break;
        case KeyEvent.DOM_VK_BACK_SPACE:
        case KeyEvent.DOM_VK_DELETE:
            if (event.target == document.body) {
                event.preventDefault(); //  dont go back in history
            }
            selection.innerHTML = ""
            if (mod_key && e.shiftKey) {
                    if (from.value.match(/^\$\d+/)) deleteEdge();
                    else destroyNode()
            }else if(e.shiftKey) deleteNode()
            break;
        case KeyEvent.DOM_VK_ESCAPE:
            // resetAllNodes()
            selection.innerHTML = ""
            edge.value = ""
            edge_id.value = ""
                // stop();
                // clearNet();
            break;
        case KeyEvent.DOM_VK_ENTER:
        case KeyEvent.DOM_VK_RETURN:
            if(!e.shiftKey)return addWord(lastHighlight);
            submit();
            break;
        default:
            // if (selection.innerHTML.match(to.value)) selection.innerHTML = ""
            // if (selection.innerHTML.match(from.value)) selection.innerHTML = ""
            // if (!mod_key && event.charCode > 0 && event.target != from && event.target != to && event.target != edge) {
            //     selection.innerHTML += event.key;
            // }
    }
    // console.log(event);
}

fetchWord(from_id.valueAsNumber || from.value || location.search.split('q=')[1] || "bug")
