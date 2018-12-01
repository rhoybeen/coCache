$(document).ready(function(){
	populateNetworkTopo();
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
    populateResourceList(node);
    var delayMapData = $("#vis_network").data['delayMap'];
    //populateDelayMap(delayMapData);
}

function populateResourceList(data){
    var node = data;
    var nodeId = node['id'];
    var table = $("#resourceDataTable").DataTable({
       "destroy":     true,
    });
    var tableData = [];
    var counters = node['counters'];
    var resourceMap = node['resourceMap'];

    for(var videoId in counters){
        var tableRow = [];
        var videoCount = counters[videoId];
        var caches = resourceMap[videoId];
        var str;
        tableRow.push(videoId);
        tableRow.push(videoCount);
        if(caches.length == 0){
            str = '<span class="label label-danger">无缓存</span>';
        }else if(caches[0] == nodeId){
            str = '<span class="label label-success">本地缓存</span>';
        }else{
            str = '<span class="label label-info">协作缓存</span>';
        }
        tableRow.push(str);
        tableData.push(tableRow);
    }
    table.clear().draw();
    table.rows.add(tableData).draw();
}

function populateDelayMap(data){
        var currentNodeId = $("#nodeId").text();
        var nodeMap = $('#vis_network').data['nodeMap'];
        var delayMap = data;
        var currentNodeDelayMap = delayMap[currentNodeId];
        var table = $("#delayMapTable").DataTable({
           "destroy": true,
        });
        var tableData = [];

        for(var nodeId in currentNodeDelayMap){
              var currentNode = nodeMap[nodeId];
              var nodeType = currentNode['nodeType'];
              var nodeName = currentNode['name'];
              var capacity = currentNode['capacity'];
              var delay = parseInt(currentNodeDelayMap[nodeId]);
              var tableRow = [];
              tableRow.push(nodeName);
              tableRow.push(nodeType);
              tableRow.push(capacity);
              tableRow.push(delay + 'ms');
              tableData.push(tableRow);
        }
        table.clear().draw();
        table.rows.add(tableData).draw();
}

function updateDelays(){
    $('#delayPanelBody').loading();
    $.ajax({
        url: '/console/delays',
        success: function(data) {
            var jsonObj = JSON.parse(data);
            if (jsonObj['isSuccess']) {
                var delayMap = jsonObj['payload'];
                populateDelayMap(delayMap);
                var netContainer = $('#vis_network');
                netContainer.data['delayMap'] = delayMap;
            }
        },
        error: function(data){

        },
        complete: function() {
            setTimeout(function() {
                  $('#delayPanelBody').loading('stop');
            }, 500);
        }
    });
}

function evaluateStrategies(){
        $('#cachePanelBody').loading();
        $.ajax({
            url: '/console/cache/evaluate',
            success: function(data) {
                var jsonObj = JSON.parse(data);
                if (jsonObj['isSuccess']) {
                    var delayMap = jsonObj['payload']['expectedDelay'];
                    $("#tdGSDelay").text(delayMap['GS'].toFixed(2) + 'ms');
                    $("#tdPopDelay").text(delayMap['POP_CO'].toFixed(2) + 'ms');
                    $("#tdRanDelay").text(delayMap['RAN_CO'].toFixed(2) + 'ms');
                    $("#tdPopNonDelay").text(delayMap['POP_NON_CO'].toFixed(2) + 'ms');
                }
            },
            error: function(data){

            },
            complete: function() {
                setTimeout(function() {
                      $('#cachePanelBody').loading('stop');
                }, 500);
            }
        });
}

function updateCache(){
        $('#cachePanelBody').loading();
        var strategy = $("input:checked").eq(0).val().trim();
        $.ajax({
            url: '/console/cache/update/' + strategy,
            success: function(data) {
                var jsonObj = JSON.parse(data);
                if (jsonObj['isSuccess']) {
                    var data = jsonObj['payload'];
                    populateNetworkTopo();
                }
            },
            error: function(data){

            },
            complete: function() {
                setTimeout(function() {
                      $('#cachePanelBody').loading('stop');
                }, 500);
                alert("成功更新系统缓存，点击各节点查看缓存情况。")
            }
        });
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

function populateNetworkTopo(){
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
            updateDelays();
        }
    });
}

