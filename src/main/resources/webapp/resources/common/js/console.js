$(document).ready(function(){
	populateNetworkTopo(nodes_json["nodes"]);
});

function populateNetworkTopo(data){
  var nodes_json = data;
  var vis_data = [];
  var vis_edges = [];

  for (var i = nodes_json.length - 1; i >= 0; i--) {
    var node = nodes_json[i];
    var id = node['id'];
    var label = node['label'];
    var parent = node['parentId'];

    //get topo edges
    if(parent!= null && parent.length != 0){
      var edge = {
      from: id,
      to:parent
      };
      vis_edges.push(edge);
    }

    var data = {};
    data['id'] = id;
    data['label'] = label;
    vis_data.push(data);
  }

  // create an array with nodes
  var nodes = new vis.DataSet(vis_data);

  // create an array with edges
  var edges = new vis.DataSet(vis_edges);

  // create a network
  var container = document.getElementById('vis_network');
  var data = {
    nodes: nodes,
    edges: edges
  };
  var options = {};
  var network = new vis.Network(container, data, options);
}

var nodes_json = {
    "nodes": [
        {
            "id": "1",
            "label": "SBS",
            "type": "SBS",
            "parentId": "2"
        },
        {
            "id": "3",
            "label": "SBS",
            "type": "SBS",
            "parentId": "2"
        },
        {
            "id": "2",
            "label": "MBS",
            "type": "MBS",
            "parentId": "4"
        },
        {
            "id": "4",
            "label": "REGIONAL",
            "type": "REGIONAL",
            "parentId": ""
        }
    ]
}
