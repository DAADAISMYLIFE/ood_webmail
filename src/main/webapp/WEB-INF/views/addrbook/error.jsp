<%-- 
    Document   : error
    Created on : 2025. 5. 20., 오후 10:13:11
    Author     : qkekd
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>오류 발생</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_style.css">
    </head>
    <body>
        <%@include file="../header.jspf"%>

        <div id="sidebar">
            <jsp:include page="../sidebar_previous_menu.jsp" />
        </div>

        <div style="display: flex;">
            <div style="width: 150px; padding: 50px;">           
                <a href="${pageContext.request.contextPath}/main_menu">메일 읽기</a><br>
                <a href="${pageContext.request.contextPath}/write_mail">메일 쓰기</a><br>
                <a href="${pageContext.request.contextPath}/addrbook/list">주소록</a><br>
                <a href="${pageContext.request.contextPath}/login.do?menu=92">로그아웃</a>
            </div>

            <div style="flex: 1; padding: 20px;">
                <h2>오류가 발생했습니다.</h2>
                <p style="color: red;">
                    <%= request.getAttribute("ERR_MSG") != null ? request.getAttribute("ERR_MSG") : "요청 처리 중 오류가 발생했습니다."%>
                </p>
                <br>
                <a href="${pageContext.request.contextPath}/addrbook/list">주소록 목록으로 돌아가기</a>
            </div>
        </div>
    </body>
</html>
