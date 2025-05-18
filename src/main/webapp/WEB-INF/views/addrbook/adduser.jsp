<%-- 
    Document   : adduser
    Created on : 2025. 5. 14., 오전 1:29:28
    Author     : keyrb
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>주소록 등록</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_style.css">
    </head>
    <body>
        
        <%@include file="../header.jspf"%>

        <div id="sidebar">
            <jsp:include page="../sidebar_previous_menu.jsp" />
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
                <h2>새 주소록 등록</h2>
                <form action="${pageContext.request.contextPath}/addrbook/adduser" method="post">
                    이메일: <input type="text" name="email" required><br>
                    이름: <input type="text" name="name" required><br>
                    전화번호: <input type="text" name="phone"><br>
                    <button type="submit">등록</button>
                </form>
                <br>
                <a href="${pageContext.request.contextPath}/addrbook/list">목록으로 돌아가기</a>
            </div>
        </div>
</html>
