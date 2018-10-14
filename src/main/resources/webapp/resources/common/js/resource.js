$(document).ready(function(){
	populateResourceGallery();
});

function populateResourceGallery(){
    $.ajax({
        url: 'http://localhost:8080/slave/resource/list',
        dataType:"jsonp",  //数据格式设置为jsonp
        jsonp:"callback",  //Jquery生成验证参数的名称
        success: function(data) {
            var jsonObj = JSON.parse(data);
            if (jsonObj['isSuccess']) {
                var payload = jsonObj['payload']
                var nodeId = payload['id'];
                var videoList = payload['resources'];
                var resourceMap = payload['resourceMap'];
                var mostPopularVideo = videoList[0];
                var maxClicks = mostPopularVideo["clickNum"];
                var gallery = $("#gallery");

                videoList.forEach(video => {
                    var videoName = video['name'];
                    var videoClicks = video['clickNum'];
                    var cachedNodes = resourceMap[videoName];
                    var popularity = (maxClicks == 0) ? 0 : videoClicks / maxClicks * 100;

                    var strDiv = '<div class="col-sm-6 col-md-4">';
                    strDiv += '<div class="thumbnail">';
                    strDiv += '<img src="./thumbnails/img'+ videoName + '.jpg" alt="1" width="100%">';
                    strDiv += '<div class="caption">';
                    strDiv += '<h3>' + videoName + '.mp4</h3>';
                    var cacheNode;
                    var cacheType;
                    if($.isEmptyObject(cachedNodes)){
                        cacheNode = 'no-cache';
                        cacheType = 'no_cache';
                    }else{
                        cacheNode = cachedNodes[0];
                        cacheType = (cacheNode == nodeId) ? 'local_cache' : 'peer_cache';
                    }
                    strDiv += '<p align="left">缓存位置：' + cacheNode + '</div class="'+ cacheType +'"></div></p>';
                    strDiv += '<p align="left">点击量：' + videoClicks + '</p>';
                    strDiv += '<div class="progress">';
                    strDiv += '<div class="progress-bar progress-bar-striped bg-success" role="progressbar" style="width:'+popularity+'%" aria-valuemin="0" aria-valuemax="100"></div>';
                    strDiv += '</div></div></div></div>';
                    gallery.append(strDiv);
                });
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