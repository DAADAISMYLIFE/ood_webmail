<%-- 
    Document   : list
    Created on : 2025. 5. 14., 오전 1:29:19
    Author     : keyrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주소록 목록</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_style.css">
</head>
<body>

<%@include file="../header.jspf" %>

<div id="sidebar">
    <jsp:include page="../sidebar_previous_menu.jsp"/>
</div>

<div style="display: flex;">
    <!-- 좌측 메뉴 -->
    <div style="width: 150px; padding: 50px;">
        <a href="${pageContext.request.contextPath}/main_menu">메일 읽기</a><br>
        <a href="${pageContext.request.contextPath}/write_mail">메일 쓰기</a><br>
        <a href="${pageContext.request.contextPath}/addrbook/list">주소록</a><br>
        <a href="${pageContext.request.contextPath}/login.do?menu=92">로그아웃</a>
    </div>

    <!-- 메인 컨텐츠 -->
    <div style="flex: 1; padding: 20px;">
        <h2>주소록 목록</h2>

        <!-- 검색 폼 추가 -->
        <form action="${pageContext.request.contextPath}/addrbook/list" method="get" style="margin-bottom: 10px;">
            <input type="text" name="keyword" value="${keyword}" placeholder="이름 또는 이메일 검색"/>
            <button type="submit">검색</button>
        </form>

        <ul>
            <c:forEach var="entry" items="${addrbookList}">
                <li>
                        ${entry.email} - ${entry.name} - ${entry.phone}
                    <a href="${pageContext.request.contextPath}/addrbook/delete?email=${entry.email}"
                       onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
                </li>
            </c:forEach>
        </ul>
        <br>
        <a href="${pageContext.request.contextPath}/addrbook/adduser">새 주소 등록</a>
    </div>
</div>
</body>
</html>
