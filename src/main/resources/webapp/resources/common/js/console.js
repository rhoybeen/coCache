$(document).ready(function(){
	populateNetworkTopo(nodes_json["nodes"]);
});

//todo: remove this temporary function. It is only for testing.
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

function populateNetworkTopo1(){
    $.ajax({
        url: '/console/nodes',
        success: function(data) {
            var jsonObj = JSON.parse(data);
            if (jsonObj['isSuccess']) {
                var nodes = jsonObj['payload']
                var vis_data = [];
                var vis_edges = [];

                for (var i = nodes.length - 1; i >= 0; i--) {
                  var node = nodes[i];
                  var id = node['id'];
                  var name = node['name'];
                  var parent = node['parentId'];

                  //get topo edges
                  if(parent!= "0" && parent.length != 0){
                    var edge = {
                    from: id,
                    to:parent
                    };
                    vis_edges.push(edge);
                  }

                  var data = {};
                  data['id'] = id;
                  data['name'] = name;
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

                } else {
                //show error msg.
                $(".alert").show();
               }
            },
        error: function(data){

        },
        complete: function() {

        }
    });
}

