

// -- GLOBAL VARIABLES -- //
var isReset = true;
var selectedNode = null;
var traceedges = [];
var tracenodes = [];
var startpages=[]
// ---------------------- //


//Get all the nodes tracing back to the start node.
function getTraceBackNodes(node) {
  var finished = false;
  var path = [];
  while (! finished) { //Add parents of nodes until we reach the start
    path.push(node);
    if (startpages.indexOf(node) !== -1) { //Check if we've reached the end
      finished = true;
    }
    node = nodes.get(node).parent; //Keep exploring with the node above.
  }
  return path;
}

//Get all the edges tracing back to the start node.
function getTraceBackEdges(tbnodes) {
  tbnodes.reverse();
  var path = [];
  for (var i=0; i<tbnodes.length-1; i++) { //Don't iterate through the last node
    path.push( getEdgeConnecting(tbnodes[i], tbnodes[i+1]) );
  }
  return path;
}
function colorNodes(xs){
  for(x of xs){
        nodes.update([{ id: x, color: { background: '#FF0000' } }]);
  }
}
//Reset the color of all nodes, and width of all edges.
function resetProperties() {
  if (!isReset) {
    selectedNode = null;
    //Reset node color
    var modnodes = tracenodes.map(function(i){return nodes.get(i);});
    colorNodes(modnodes, 0);
    //Reset edge width and color
    var modedges = traceedges.map(function(i){
      var e=edges.get(i);
      e.color=getEdgeColor(nodes.get(e.to).level);
      return e;
    });
    edgesWidth(modedges, 1);
    tracenodes = [];
    traceedges = [];
  }
}

//Highlight the path from a given node back to the central node.
function traceBack(node) {
  startpages.push(node)
  if (node != selectedNode) {
    selectedNode = node;
    resetProperties();
    tracenodes = getTraceBackNodes(node);
    traceedges = getTraceBackEdges(tracenodes);
    //Color nodes yellow
    var modnodes = tracenodes.map(function(i){return nodes.get(i);});
    colorNodes(modnodes, 1);
    //Widen edges
    var modedges = traceedges.map(function(i){
      var e=edges.get(i);
      e.color={inherit:"to"};
      return e;
    });
    edgesWidth(modedges, 5);
  }
}