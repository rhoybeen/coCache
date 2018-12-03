<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta charset="utf-8">
	<title>移动边缘CDN协作缓存加速系统</title>
</head>
<body>
<jsp:include page="lib_header.jsp"/>

<link href="/resources/common/css/console.css" rel="stylesheet">
<link href="/resources/vis/vis.min.css" rel="stylesheet">
<link href="/resources/bootstrap/jquery.dataTables.min.css" rel="stylesheet">

<script src="/resources/bootstrap/jquery.dataTables.min.js"></script>
<script src="/resources/vis/vis.min.js"></script>
<script src="/resources/common/js/console.js"></script>

<div class="jumbotron text-center" style="margin-bottom:0">
  <h1>移动边缘CDN缓存协作系统</h1>
  <p>WSPN实验室 北京邮电大学</p>
</div>

<nav class="navbar navbar-inverse">
  <div class="container-fluid">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="#">导航</a>
    </div>
    <div class="collapse navbar-collapse" id="myNavbar">
      <ul class="nav navbar-nav">
        <li class="active"><a href="#">控制台</a></li>
        <li><a href="/slave/resource">视频点播</a></li>
      </ul>
    </div>
  </div>
</nav>

<div class="container-fluid" id="container">
    <div class="col-md-8 well" id="divNodeInfo">
        <h4>协作缓存节点</h4><hr>
        <table class="table table-striped" id="nodeTable">
          <thead>
            <tr>
              <th>项目</th>
              <th>描述</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>当前节点:</td>
              <td id='nodeName'>A1</td>
            </tr>
            <tr>
              <td>节点ID:</td>
              <td id='nodeId'>A1</td>
            </tr>
            <tr>
              <td>服务地址:</td>
              <td id = 'nodeIp'>12.12.12.48</td>
            </tr>
            <tr>
              <td>节点等级:</td>
              <td id = 'nodeType'>SBS_MEC</td>
            </tr>
            <tr>
              <td>节点容量:</td>
              <td id = 'nodeCapacity'>15</td>
            </tr>
            <tr>
              <td>服务资源数:</td>
              <td id = 'nodeResourceNum'>1000</td>
            </tr>
            <tr>
              <td>父节点ID:</td>
              <td id = 'nodeParentId'>B1</td>
            </tr>
          </tbody>
        </table>
          <div align="right">
          <button class="btn-primary" onclick="initRequest()">初始化</button>
          <button class="btn-info" onclick="resetCache()">重置缓存</button>
          <button class="btn-success" onclick="resetRequest()">重置请求</button></div>
    </div>
    <div class="col-md-4" >
      <div class="well panel">
       <h4>节点拓扑</h4> <hr>
       <div id="vis_network" style="height: 350px;"></div>
      </div>

    </div>
        <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    资源列表
                </div>
                <div class="panel-body">
                    <table id="resourceDataTable" class="table table-condensed table-striped", width="100%">
                        <thead>
                            <tr>
                              <th>资源名</th>
                              <th>请求数</th>
                              <th>缓存情况</th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    服务时延
                </div>
                <div class="panel-body" id = "delayPanelBody">
                    <table id = "delayMapTable" class="table table-condensed table-striped">
                        <thead>
                            <tr>
                              <th>协作节点</th>
                              <th>节点类型</th>
                              <th>容量</th>
                              <th>时延</th>
                            </tr>
                          </thead>
                    </table>
                    <div align="right"><button class="btn-info" onclick="updateDelays()">刷新</button></div>
                </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    缓存控制
                </div>
                <div class="panel-body" id="cachePanelBody">
                  <p><input name="strategy" type="radio" value="GS" checked="checked"> GS协作缓存 - 采用基于内容与节点偏好的CS匹配算法进行缓存更新。</p>
                  <p><input name="strategy" type="radio" value="POP_CO"> 流行度协作缓存 - 协作缓存时不考虑节点与内容偏好，根据节点内容流行度分配缓存。</p>
                  <p><input name="strategy" type="radio" value="POP_NON_CO"> 流行度非协作缓存 - 协作缓存时不考虑节点与内容偏好，只根据当前节点内容流行度分配缓存。</p>
                  <p><input name="strategy" type="radio" value="RAN_CO"> 随机协作缓存 - 各节点随机更新缓存内容，不与其它节点共享信息。</p>
                  <div align="right"><button class="btn-info" onclick="evaluateStrategies()">评估</button>
                  <button class="btn-success" onclick="updateCache()">更新</button></div>
                  <hr>
                  <table id = "cacheTable" class="table table-condensed table-striped">
                      <thead>
                          <tr>
                            <th>缓存策略</th>
                            <th>预估平均服务时延</th>
                          </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>GS协作缓存</td>
                                <td id="tdGSDelay"></td>
                            </tr>
                            <tr>
                                <td>流行度协作缓存</td>
                                <td id="tdPopDelay"></td>
                            </tr>
                            <tr>
                                <td>流行度非协作缓存</td>
                                <td id="tdPopNonDelay"></td>
                            </tr>
                            <tr>
                                <td>随机协作缓存</td>
                                <td id="tdRanDelay"></td>
                            </tr>
                        </tbody>
                  </table>
                </div>
            </div>
          </div>
    </div>
  </div>
</div>

<div class="jumbotron text-center" style="margin-bottom:0">
  <p>Copyright©2018 WSPN-BUPT All Rights Reserved.</p>
</div>
</body>
</html>
