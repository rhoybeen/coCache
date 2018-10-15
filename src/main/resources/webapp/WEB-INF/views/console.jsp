<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta charset="utf-8">
	<title>移动边缘CDN缓存协作系统</title>
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
  <div class="row">
    <div class="col-md-8 well">
        <h4>协作缓存节点</h4><hr>
        <div class="col-md-4">
          <div class="panel panel-info">
              <div class="panel-heading">
                  id: A1 SBS
              </div>
              <div class="panel-body">
                  面板内容
              </div>
          </div>
        </div>
    </div>
    <div class="col-md-4" >
      <div class="well panel">
       <h4>节点拓扑</h4> <hr>
       <div id="vis_network" style="height: 400px;"></div>
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
