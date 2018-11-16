$(document).ready(function(){
	populateNetworkTopo1();
});

function displayNodeInfo(nodeId){
    var table = $("#nodeTable");
    var nodeMapData = $("#vis_network").data['nodeMap'];
    var node = nodeMapData[nodeId];
    if(node != null){
        $("#nodeName").text(node['name']);
        $("#nodeId").text(node['id']);
        $("#nodeIp").text(node['ip']);
        $("#nodeType").text(node['nodeType']);
        $("#nodeCapacity").text(node['capacity']);
        $("#nodeResourceNum").text(node['resourceAmount']);
        $("#nodeParentId").text(node['parentId']);
    }
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
                var node_map = {};

                for (var i = nodes.length - 1; i >= 0; i--) {
                  var node = nodes[i];
                  var id = node['id'];
                  var name = node['name'];
                  var parent = node['parentId'];
                  var type = node['nodeType'];

                  node_map[id] = node;
                  //get topo edges
                  if(parent!= "0" && parent.length != 0){
                    var edge = {
                    from: id,
                    to:parent,
                    color: {color:'grey'}
                    };
                    vis_edges.push(edge);
                  }

                  var data = {};
                  data['id'] = id;
                  data['label'] = name;
                  data['group'] = type;
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
                var options = {
                  nodes: {
                      shape: 'dot',
                      size: 10,
                      font: {
                          size: 10,
                          color: '#000000'
                      },
                      borderWidth: 1
                  },
                  edges: {
                      width: 1,
                  },
                  groups: {
                      SBS_MEC: {
                          color: 'rgba(0,255,0,1)',
                          borderWidth:2
                      },
                      MBS_MEC: {
                          size : 13,
                          color: {background:'green',border:'white'},
                          borderWidth:2
                      },
                      REGIONAL_MEC: {
                          size: 15,
                          color:'rgb(0,255,140)'
                      }
                  }
              };
                var network = new vis.Network(container, data, options);
                  //set onclick event for every node.
                var netContainer = $('#vis_network');
                netContainer.data['nodeMap'] = node_map;
                network.on('click',function(){
                  var nodes = network.getSelectedNodes();
                  if(nodes.length > 0){
                    var currentNodeId = nodes[0];
                    displayNodeInfo(currentNodeId);
                  }
                });
                displayNodeInfo('1');

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

