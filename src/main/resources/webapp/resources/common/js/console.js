$(document).ready(function(){
	populateNetworkTopo1();
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
    var type = node['type'];

    //get topo edges
    if(parent!= null && parent.length != 0){
      var edge = {
      from: id,
      to:parent,
      color: 'grey'
      };
      vis_edges.push(edge);
    }

    var data = {};
    data['id'] = id;
    data['label'] = label;
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
                  var type = node['nodeType'];

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

