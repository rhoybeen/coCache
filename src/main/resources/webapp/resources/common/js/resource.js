$(document).ready(function(){
	populateResourceGallery();
});

function populateResourceGallery(){
    $.ajax({
        url: '/slave/resource/list',
        success: function(data) {
            var jsonObj = JSON.parse(data);
            if (jsonObj['isSuccess']) {
                var canvas = $("#canvas");
                var payload = jsonObj['payload']
                var nodeId = payload['id'];
                var videoList = payload['resources'];
                var resourceMap = payload['resourceMap'];
                var mostPopularVideo = videoList[0];
                var maxClicks = mostPopularVideo["clickNum"];
                var gallery = $("#gallery");
                var chartLabels = [];
                var charData = [];
                canvas.data['nodeInfo'] = payload;

                videoList.forEach(video => {
                    var videoName = video['name'];
                    var videoClicks = video['clickNum'];
                    var cachedNodes = resourceMap[videoName];
                    var popularity = (maxClicks == 0) ? 0 : videoClicks / maxClicks * 100;
                    chartLabels.push(videoName);
                    charData.push(videoClicks);

                    var strDiv = '<div class="col-sm-6 col-md-4">';
                    strDiv += '<div class="thumbnail" >';
                    strDiv += '<img src="/resources/thumbnails/img'+ videoName + '.jpg" alt="1" width="100%">';
                    strDiv += '<div class="caption">';
                    strDiv += '<h3><a href="/slave/play/' + videoName + '">' + videoName + '.mp4</a></h3>';
                    var cacheNode;
                    var cacheType;
                    if($.isEmptyObject(cachedNodes)){
                        cacheNode = 'none';
                        cacheType = '<span class="label label-danger">未缓存</span>';
                    }else{
                        cacheNode = cachedNodes[0];
                        cacheType = (cacheNode == nodeId) ? '<span class="label label-success">本地缓存</span>' : '<span class="label label-warning">协作缓存</span>';
                    }
                    strDiv += '<p align="left">缓存位置：' + cacheNode + cacheType + '</p>';
                    strDiv += '<p align="left">点击量：' + videoClicks + '</p>';
                    strDiv += '<div class="progress">';
                    strDiv += '<div class="progress-bar progress-bar-striped bg-success" role="progressbar" style="width:'+popularity+'%" aria-valuemin="0" aria-valuemax="100"></div>';
                    strDiv += '</div></div></div></div>';
                    gallery.append(strDiv);
                });
                populateChart(chartLabels,charData);
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

function populateChart(videoNames, videoClicks){
    var canvas = $("#canvas");
    var label = 'A1 SBS_MEC';
    var labels = videoNames;
    var chartData = videoClicks;
    var barChartData = {
        labels: labels,
        datasets:[{
            label:label,
            backgroundColor: "rgba(204,66,0,0.4)",
            borderColor: "rgba(204,66,0,1)",
            borderWidth: 1,
            data:chartData
        }]
    };
    var ctx = document.getElementById('canvas').getContext('2d');
    var myBarChart = new Chart(ctx,{
        type: 'bar',
        data: barChartData,
        options: {
            responsive: true,
            legend: {
                position: 'top',
                show:false
            },
            title: {
                display: true,
                text: '节点历史请求记录',
            },
        }
    });
}

function requestVideo(videoName){
    var nodeInfo = $("#canvas").data['nodeInfo'];
    var resourceMap = nodeInfo['resourceMap'];
    var cachedNodes = resourceMap[videoName];
    var playStr = ""
    window.location.href = "play/" + videoName;
}