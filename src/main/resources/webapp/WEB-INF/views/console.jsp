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

<div class="container-fluid">
    <div class="col-md-8 well">
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
    </div>
    <div class="col-md-4" >
      <div class="well panel">
       <h4>节点拓扑</h4> <hr>
       <div id="vis_network" style="height: 400px;"></div>
      </div>

    </div>
        <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    资源列表
                </div>
                <div class="panel-body">
                    <table class="table table-condensed">

                      <tbody>
                        <tr>
                          <td>001.mp4</td>
                          <td>13660</td>
                          <td><span class="label label-success">本地缓存</span></td>
                        </tr>
                        <tr>
                          <td>002.mp4</td>
                          <td>7862</td>
                          <td><span class="label label-success">本地缓存</span></td>
                        </tr>
                        <tr>
                          <td>003.mp4</td>
                          <td>1160</td>
                          <td><span class="label label-info">协作节点</span></td>
                        </tr>
                      </tbody>
                    </table>
                </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    服务时延
                </div>
                <div class="panel-body">
                    <table class="table table-condensed table-striped">
                        <thead>
                            <tr>
                              <th>协作节点</th>
                              <th>时延</th>
                            </tr>
                          </thead>
                      <tbody>
                        <tr>
                          <td>A2</td>
                          <td>57ms</td>
                        </tr>
                        <tr>
                            <td>A3</td>
                            <td>86ms</td>
                          </tr>
                          <tr>
                              <td>A4</td>
                              <td>93ms</td>
                            </tr>
                            <tr>
                              <td>B1</td>
                              <td>46ms</td>
                            </tr>
                            <tr>
                                <td>B2</td>
                                <td>75ms</td>
                              </tr>
                              <tr>
                                  <td>C1</td>
                                  <td>67ms</td>
                                </tr>
                      </tbody>
                    </table>
                    <div align="right"><button class="btn-info">刷新</button></div>
                </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    缓存控制
                </div>
                <div class="panel-body">
                  <p><input type="radio"> 协作缓存 - 采用基于内容与节点偏好的CS匹配算法进行缓存更新。</p>
                  <p><input type="radio"> 流行度缓存 - 协作缓存时不考虑节点与内容偏好，只根据内容流行度分配缓存。</p>
                  <p><input type="radio"> 非协作缓存 - 各节点独立更新缓存内容，不与其它节点共享信息。</p>
                  <div align="right"><button class="btn-info">评估</button> <button class="btn-success">更新</button></div>
                  <font size="10" color="#66ff66">3201.2s</font>
                  <p>预估整体服务时延</p>
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
