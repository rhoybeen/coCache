$(document).ready(function(){
    prepareData();
});

function prepareData(){
    $.ajax({
        url: '/console/compare',
        success: function(data) {
            var jsonObj = JSON.parse(data);
            if (jsonObj['isSuccess']) {
                var container = $("#container");
                var data = jsonObj['payload'];
                container.data['nodeMap'] = data['nodeMap'];
                container.data['delayMap'] = data['delayMap'];
                initComponents();
                initCharts();
            }
        },
        error: function(data){
        },
        complete: function() {
        }
    });
}

function initComponents(){
    var container = $("#container");
    var nodeMap = container.data['nodeMap'];
    var delayMap = container.data['delayMap'];

    var slaveClient = nodeMap['1'];
    var resourceMap = slaveClient['resourceMap'];
    var remoteServerIp = slaveClient['remoteServerIp'];
    var videoName = $("#videoName").val();
    console.log(videoName);
    var locations = resourceMap[videoName];

    var currentNodeId;
    var currentNodeName;
    if($.isEmptyObject(locations)){
        currentNodeId = '0';
        currentNodeName = 'Remote Server';
    }else{
        currentNodeId = locations[0];
        currentNodeName = nodeMap[currentNodeId]['name'];
    }
    $("#currentNodeName").text(currentNodeName);

    var dropdownMenu = $('.dropdown-menu').eq(0);
    var remoteServer = {};
    remoteServer['id'] = "0";
    remoteServer['name'] = 'Remote Server';
    remoteServerIp['ip'] = remoteServerIp;
    nodeMap['0'] = remoteServer;

    for(nodeId in nodeMap){
        console.log(nodeId);
        var node = nodeMap[nodeId];
        var nodeName = node['name'];
        var delay = nodeId=="0"?'**' : delayMap['1'][nodeId].toFixed(1);
        var itemStr = '<li role="presentation"><a role="menuitem" tabindex="-1" href="#" onclick="onItemChange('+ nodeId +')">';
        itemStr = itemStr + nodeName + ' ' + delay + 'ms';
        itemStr += '</a></li>';
        dropdownMenu.append(itemStr);
    }

//    var player1SrcStr = 'http://' + nodeMap[currentNodeId]['ip'] + '/' + videoName;
    var player1SrcStr = 'http://localhost:8080/resources/video/daguojueqi.mp4';
    document.getElementById("videoSource1").src = player1SrcStr;
    document.getElementById("player").load();
}

function onItemChange(nodeId){
    var container = $("#container");
    var videoName = $("#videoName").val();
    var nodeMap = container.data['nodeMap'];
    var node = nodeMap[nodeId];
    var nodeIp = node['ip'];
//    var url = 'http://' + nodeIp + '/' + videoName;
    var url = 'http://localhost:8080/resources/video/daguojueqi.mp4';
    document.getElementById("videoSource2").src = url;
    document.getElementById("player2").load();
    $("#comparedNodeName").text(node['name']);
}

function initCharts(){
    console.log("create charts");
    myChart = new Chart(document.getElementById("myChart"), data);
    myChart1 = new Chart(document.getElementById("myChart1"), data1);
    myChart2 = new Chart(document.getElementById("myChart2"), data2);
    // var myChart3 = new Chart(document.getElementById("myChart3"), data3);
    var myPlayer = document.getElementById('player');
    myPlayer.addEventListener('progress',function(){
    var duration = myPlayer.duration;
    if(duration>0){
      for (var i = 0; i < myPlayer.buffered.length; i++) {
        if (myPlayer.buffered.start(myPlayer.buffered.length - 1 - i) < myPlayer.currentTime) {
          var progress1 = (myPlayer.buffered.end(myPlayer.buffered.length - 1 - i) / duration) * 100;
          var curTime = new Date().getTime();
          pre_buffered = cur_buffered;
          cur_buffered = progress1;

          //console.log(cur_buffered+" "+pre_buffered+" "+(curTime-startTime));
          var rate1 = (cur_buffered - pre_buffered)*sizeOfVideo*10 /((curTime-startTime)*1024*1024);
          startTime = curTime;
          if(!isNaN(rate1)&&isFinite(rate1)){
            data2.data.datasets[0].data[0] = rate1.toFixed(2);
            download_rate1 = rate1.toFixed(2);
            if(download_rate1>max_rate1) max_rate1 = download_rate1;
          }
            data.data.datasets[0].data[0] = progress1.toFixed(2);
            buf_progress1 = progress1.toFixed(2);
          // console.log((cur_buffered - pre_buffered)+" dur:"+(curTime-startTime)+"rate: "+rate);

          // updateChart(0,0,progress.toFixed(2));
          // updateChart(2,0,rate.toFixed(2));
          break;
        }
      }

    }
    });

    myPlayer.addEventListener('timeupdate', function() {
    var duration =  myPlayer.duration;
    if (duration > 0) {
      var time = (myPlayer.currentTime / duration)*100;
      data1.data.datasets[0].data[0] = time.toFixed(2);
      play_progress1 = time.toFixed(2);
      // updateChart(1,0,time.toFixed(2));
    }
    });

    myPlayer.addEventListener('waiting', function() {
      delay_count++;
    });

    var startTime2 = new Date().getTime();
      var pre_buffered2 = 0;
      var cur_buffered2 = 0;
    var myPlayer2 = document.getElementById('player2');
        myPlayer2.addEventListener('progress',function(){
    var duration = myPlayer2.duration;
    if(duration>0){
      for (var i = 0; i < myPlayer2.buffered.length; i++) {
        if (myPlayer2.buffered.start(myPlayer2.buffered.length - 1 - i) < myPlayer2.currentTime) {
          var progress2 = (myPlayer2.buffered.end(myPlayer2.buffered.length - 1 - i) / duration) * 100;
          var curTime = new Date().getTime();
          pre_buffered2 = cur_buffered2;
          cur_buffered2 = progress2;
          var rate2 = (cur_buffered2 - pre_buffered2)*sizeOfVideo*10 /((curTime-startTime2)*1024*1024);
          // console.log((cur_buffered2 - pre_buffered2)+" dur:"+(curTime-startTime2)+"rate: "+rate);
          startTime2 = curTime;
        if(!isNaN(rate2)&&isFinite(rate2)){
          data2.data.datasets[0].data[1] = rate2.toFixed(2);
          download_rate2 = rate2.toFixed(2);
          if(download_rate2>max_rate2) max_rate2 = download_rate2;
          }
          data.data.datasets[0].data[1] = progress2.toFixed(2);
          buf_progress2 = progress2.toFixed(2);
          // updateChart(0,1,progress.toFixed(2));
          // updateChart(2,1,rate.toFixed(2));
          break;
        }
      }

    }
    });

    myPlayer2.addEventListener('timeupdate', function() {
    var duration =  myPlayer2.duration;
    if (duration > 0) {
      var time = (myPlayer2.currentTime / duration)*100;
      data1.data.datasets[0].data[1] = time.toFixed(2);
      play_progress2 = time.toFixed(2);
      // updateChart(1,1,time.toFixed(2));
    }
    });
}

function startVideo(){
    var mPlayer1 = document.getElementById("player");
    var mPlayer2 = document.getElementById("player2");
    mPlayer1.play();
    mPlayer2.play();
  setTimeout(function(){
      mPlayer1.pause();
      mPlayer2.pause();
      var delay = 213 - mPlayer1.currentTime.toFixed(3);
      var str = "卡顿时间：" + delay + "max_rate:" +max_rate1 +"delay count" + delay_count;
      alert(str);

  },213000);
}

function pauseVideo(){
  document.getElementById("player").pause();
  document.getElementById("player2").pause();
}

function updateChart(chart, col , value){
  switch(chart){
    case 0:myChart.data.datasets[0].data[col] = value;myChart.update();break;
    case 1:myChart1.data.datasets[0].data[col] = value;myChart1.update();break;
    case 2:myChart2.data.datasets[0].data[col] = value;myChart2.update();break;
    default: break;
  }

}

setInterval(function(){
  // myChart.data.datasets[0].data[0] = buf_progress1;
  // myChart.data.datasets[0].data[1] = buf_progress2;
  // myChart1.data.datasets[0].data[0] = play_progress1;
  // myChart1.data.datasets[0].data[1] = play_progress2;
  // myChart2.data.datasets[0].data[0] = download_rate1;
  // myChart2.data.datasets[0].data[1] = download_rate2;
  // data3.data.datasets[0].data.pop();
  // data3.data.labels.forEach(function(val,index,arr){

  // });
//   console.log(buf_progress1+" "+play_progress1+" "+download_rate1);
//     console.log(buf_progress2+" "+play_progress2+" "+download_rate2);
  document.getElementById("info").innerHTML = "Bufferes Progress: " + buf_progress1 + "% &nbsp;&nbsp;&nbsp;" +" Playing Progress: " + play_progress1 + "% &nbsp;&nbsp;&nbsp;" + " Downloading Rate: "+ download_rate1 +" MB/s";
  document.getElementById("info2").innerHTML = "Bufferes Progress: " + buf_progress2 + "% &nbsp;&nbsp;&nbsp;" +" Playing Progress: " + play_progress2 + "% &nbsp;&nbsp;&nbsp;" + " Downloading Rate: "+ download_rate2 +" MB/s";
  myChart.update();
  myChart1.update();
  myChart2.update();
},1500);

var delay_count = 0;
var max_rate1 = 0;
var max_rate2 = 0;
var buf_progress1 = 0,
    buf_progress2 = 0,
    play_progress1 = 0,
    play_progress2 = 0,
    download_rate1 = 0,
    download_rate2 = 0;
var startTime = new Date().getTime();
var pre_buffered = 0;
var cur_buffered = 0;
var sizeOfVideo = 202866533;//byte
var data = {
  type: 'bar',
  data: {
      labels: ["当前节点", "对比节点"],
      datasets: [{
          label: "Buffered Progress %",
          data: [0, 0],
          backgroundColor: [
              'rgba(255, 99, 132, 0.3)',
              'rgba(54, 162, 235, 0.3)',
          ],
          borderColor: [
              'rgba(255,99,132,1)',
              'rgba(54, 162, 235, 1)',
          ],
          borderWidth: 1,
      }]
  },
  options: {
      scales: {
          xAxes: [{
              ticks: {
                  beginAtZero:true
              },
              barPercentage: 0.4
          }],
          yAxes: [{
              ticks: {
                  beginAtZero:true,
                  max:100
              }
          }]
      },
      title: {
        display: false,
        text: 'Buffered Progress %'
    },
    animation:{
      duration:0
    }
  }
};

var data1 = {
type: 'bar',
data: {
    labels: ["当前节点", "对比节点"],
    datasets: [{
      label: "Playing Progress %",
        data: [0, 0],
        backgroundColor: [
            'rgba(255, 99, 132, 0.3)',
            'rgba(54, 162, 235, 0.3)',
        ],
        borderColor: [
            'rgba(255,99,132,1)',
            'rgba(54, 162, 235, 1)',
        ],
        borderWidth: 1,
    }]
},
options: {
    scales: {
        xAxes: [{
            ticks: {
                beginAtZero:true
            },
            barPercentage: 0.4
        }],
        yAxes: [{
            ticks: {
                beginAtZero:true,
                max:100
            }
        }]
    },
      title: {
        display: false,
        text: 'Playing Progress %'
    },
    animation:{
      duration:0
    }
}
};

var data2 = {
type: 'bar',
data: {
    labels: ["当前节点", "对比节点"],
    datasets: [{
      label: "Downloading Rate MB/s",
        data: [0, 0],
        backgroundColor: [
            'rgba(255, 99, 132, 0.3)',
            'rgba(54, 162, 235, 0.3)',
        ],
        borderColor: [
            'rgba(255,99,132,1)',
            'rgba(54, 162, 235, 1)',
        ],
        borderWidth: 1,
    }]
},
options: {
    scales: {
        xAxes: [{
            barPercentage: 0.4
        }],
        yAxes: [{
            ticks: {
                beginAtZero:true,
                max:10
            }
        }]
    },
      title: {
        display: false,
        text: 'Downloading Rate MB/s'
    },
    animation:{
      duration:0
    }
}
};

var data3 = {
        type: 'line',
        data: {
            labels: [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14],
            datasets: [{
              label: "当前节点",
                data: [0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            fillColor : "rgba(220,220,220,0.5)",
            strokeColor : "rgba(220,220,220,1)",
            pointColor : "rgba(220,220,220,1)",
            pointStrokeColor : "#fff",
                borderWidth: 1,
            },
        {
            label: "对比节点",
                data: [0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            fillColor : "rgba(151,187,205,0.5)",
            strokeColor : "rgba(151,187,205,1)",
            pointColor : "rgba(151,187,205,1)",
            pointStrokeColor : "#fff",
                borderWidth: 1,
            },
        ]
        },
        options: {
            scales: {
                xAxes: [{
                    barPercentage: 0.4
                }],
                yAxes: [{
                    ticks: {
                        beginAtZero:true,
                        max:10
                    }
                }]
            },
              title: {
                display: false,
                text: '下载速率MB/s'
            },
            animation:{
              duration:0
            }
        }
      };
 var myChart,myChart1,myChart2;