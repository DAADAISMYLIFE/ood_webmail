<%-- 
    Document   : main_menu
    Created on : 2022. 6. 10., 오후 3:15:45
    Author     : skylo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>

<!-- 제어기에서 처리하면 로직 관련 소스 코드 제거 가능!
<jsp:useBean id="pop3" scope="page" class="deu.cse.spring_webmail.model.Pop3Agent" />
<%
    pop3.setHost((String) session.getAttribute("host"));
    pop3.setUserid((String) session.getAttribute("userid"));
    pop3.setPassword((String) session.getAttribute("password"));
%>
-->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>주메뉴 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@include file="header.jspf"%>

        <div id="sidebar">
            <jsp:include page="sidebar_menu.jsp" />
        </div>

        <!-- 메시지 삭제 링크를 누르면 바로 삭제되어 실수할 수 있음. 해결 방법은? -->
        <div id="main">
            <!-- 검색&정렬 폼 -->
            <form action="search_mail" method="post" accept-charset="UTF-8" style="margin-bottom: 20px;">
                <input type="text" name="keyword" placeholder="메일 제목 또는 발신자 검색" />
                <select name="sort">
                    <option value="desc" ${param.sort == 'desc' ? 'selected' : ''}>최신순</option>
                    <option value="asc" ${param.sort == 'asc' ? 'selected' : ''}>오래된 순</option>
                </select>
                <button type="submit">검색</button>
            </form>

            <!-- 메일 목록 출력 -->
            <div>
                <c:choose>
                    <c:when test="${not empty messageList}">
                        ${messageList}
                    </c:when>
                    <c:otherwise>
                        <p style="color: gray;">표시할 메일이 없습니다.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- 페이징 링크 -->
            <c:if test="${totalPages > 1}">
                <div style="margin-top: 20px;">
                    <c:forEach var="i" begin="1" end="${totalPages}">
                        <form action="${empty param.keyword ? 'main_menu' : 'search_mail'}" method="${empty param.keyword ? 'get' : 'post'}" style="display:inline;">
                            <input type="hidden" name="page" value="${i}" />
                            <input type="hidden" name="sort" value="${param.sort}" />
                            <c:if test="${not empty param.keyword}">
                                <input type="hidden" name="keyword" value="${param.keyword}" />
                            </c:if>
                            <button type="submit" ${param.page == i ? 'disabled' : ''}>${i}</button>
                        </form>
                    </c:forEach>
                </div>
            </c:if>         
        </div>

        <%@include file="footer.jspf"%>
    </body>
</html>
