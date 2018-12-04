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

<link href="/resources/common/css/play.css" rel="stylesheet">

<script src="/resources/common/js/Chart.min.js" type="text/javascript"></script>
<script src="/resources/common/js/play.js"></script>

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
        <li class><a href="/console">控制台</a></li>
        <li ><a href="/slave/resource">视频点播</a></li>
        <li class="active"><a href="#">播放比较</a></li>
      </ul>
    </div>
  </div>
</nav>

<div class="container-fluid" id="container">
        <input type="hidden" id="videoName" value='${videoName}'>
        <h2  style="text-align:center;color:#1ABC9C;">高清视频优化加速比较-${videoName}.mp4</h2>
        	<!-- <h2  style="text-align:center;color:#1ABC9C;">高清视频优化加速比较</h2> -->
        <div class="row">
          <div class="col-md-6">
              <h6 style="text-align:center" style="margin:0px;">当前副本来源-<div id='currentNodeName'></div></h6>
              <video id="player" controls="controls"  width="480" height="300">
                <source id="videoSource1" src="#">
              </video>
              <div>
              <p id="info">nothing to show</p>
              </div>

          </div>
          <div class="col-md-6">
              <h6 style="text-align:center" style="margin:0px;">对比节点-<div id='comparedNodeName'></div></h6>
              <video id="player2" controls="controls" width="480" height="300">
                 <source id="videoSource2" src="#">
              </video>
              <div>
              <p id="info2">nothing to show</p>
              </div>
          </div>
        </div>
         <div class="row">
          <div class="col-md-3">
    		    <canvas id="myChart" height="200"></canvas>
          </div>
          <div class="col-md-3">
    		    <canvas id="myChart1" height="200"></canvas>
          </div>
          <div class="col-md-3">
    		    <canvas id="myChart2" height="200"></canvas>
          </div>
          <div class="col-md-3" style="padding-top:20px;">
    		  <button type="button" class="btn btn-success" onclick="startVideo()">播放</button>
              <button type="button" class="btn btn-info" onclick="pauseVideo()">暂停</button>
              <div class="dropdown">
              	<button type="button" class="btn dropdown-toggle" id="dropdownMenu1"
              			data-toggle="dropdown">
              		对比节点
              		<span class="caret"></span>
              	</button>
              	<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
              	</ul>
              </div>
          </div>
</div>

<div class="jumbotron text-center" style="margin-bottom:0">
  <p>Copyright©2018 WSPN-BUPT All Rights Reserved.</p>
</div>
</body>
</html>
