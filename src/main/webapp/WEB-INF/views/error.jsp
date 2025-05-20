<%-- 
    Document   : error
    Created on : 2025. 5. 20., 오후 10:18:12
    Author     : qkekd
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page isErrorPage="true" %>

<!DOCTYPE html>

<html lang="ko">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>오류 발생</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main_style.css">
    </head>
    <body>

        <%@include file="header.jspf"%> <%-- header.jspf의 경로에 따라 수정 필요 --%>

        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh;">
            <h2>죄송합니다. 오류가 발생했습니다.</h2>
            <p>요청을 처리하는 중 예상치 못한 문제가 발생했습니다.</p>
            <p>잠시 후 다시 시도해 주십시오.</p>

            <%-- 개발 환경에서만 에러 상세 정보를 보여주는 것이 좋습니다. --%>
            <% if (exception != null) {%>
            <p style="color: grey; font-size: 0.9em;">오류 메시지: <%= exception.getMessage()%></p>
            <%-- 스택 트레이스는 보안상 프로덕션 환경에서는 노출하지 않는 것이 좋습니다. --%>
            <pre style="background-color: #f8f8f8; padding: 10px; border: 1px solid #ddd; max-height: 300px; overflow-y: scroll;"><%= exception.toString()%></pre> 
            <% }%>

            <br>
            <a href="${pageContext.request.contextPath}" class="button">메인 화면으로 돌아가기</a>
        </div>

        <%@include file="footer.jspf"%> <%-- footer.jspf의 경로에 따라 수정 필요 --%>
    </body>
</html>
