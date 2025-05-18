<%-- 
    Document   : write_mail.jsp
    Author     : jongmin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>

<!DOCTYPE html>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>메일 쓰기 화면</title>
    <link type="text/css" rel="stylesheet" href="css/main_style.css"/>
    <script>
        function addRecipient(email) {
            const toField = document.getElementsByName('to')[0];
            if (toField.value) {
                toField.value += ", " + email;
            } else {
                toField.value = email;
            }
        }
    </script>
</head>
<body>
<%@include file="../header.jspf" %>

<div id="sidebar">
    <jsp:include page="../sidebar_previous_menu.jsp"/>
</div>

<div id="main" style="display: flex; align-items: flex-start;">
    <div style="flex: 3; margin-right: 20px;">
        <%-- <jsp:include page="mail_send_form.jsp" /> --%>
        <%-- 삼항연산자가 들어있어 c:if (c:out, choose 등으로) 변경 (코드 스멜 제거) --%>
        <form enctype="multipart/form-data" method="POST" action="write_mail.do">
            <form enctype="multipart/form-data" method="POST" action="write_mail.do">
                <table>
                    <tr>
                        <td>수신</td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty param['sender']}">
                                    <input type="text" id="toField" name="to" size="80" value="${param['sender']}">
                                </c:when>
                                <c:otherwise>
                                    <input type="text" id="toField" name="to" size="80" value="">
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <td>참조</td>
                        <td><input type="text" name="cc" size="80"></td>
                    </tr>
                    <tr>
                        <td>메일 제목</td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty param['sender']}">
                                    <input type="text" name="subj" size="80" value="RE: ${sessionScope['subject']}">
                                </c:when>
                                <c:otherwise>
                                    <input type="text" name="subj" size="80" value="">
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                    <!-- 본문 -->
                    <tr>
                        <td colspan="2">본 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 문</td>
                    </tr>
                    <tr>  <%-- TextArea    --%>
                        <td colspan="2">
                            <c:choose>
                                <c:when test="${not empty param['sender']}">
                                <textarea rows="15" name="body" cols="80">

----
                                        ${sessionScope['body']}
                                </textarea>
                                </c:when>
                                <c:otherwise>
                                    <textarea rows="15" name="body" cols="80"></textarea>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <td>첨부 파일</td>
                        <td><input type="file" name="file1" size="80" multiple></td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="메일 보내기">
                            <input type="reset" value="다시 입력">
                        </td>
                    </tr>
                </table>
            </form>
        </form>
    </div>
    <!-- 주소록 표시 -->
    <div style="flex: 1;">
        <h3>주소록</h3>


        <!-- 검색 폼 추가 -->
        <form action="${pageContext.request.contextPath}/write_mail" method="get" style="margin-bottom: 10px;">
            <input type="text" name="keyword" value="${keyword}" placeholder="이름 또는 이메일 검색"/>
            <button type="submit">검색</button>
        </form>

        <c:forEach var="entry" items="${addrbookList}">
            <input type="checkbox" class="addrCheckbox" value="${entry.email}">
            ${entry.name} (${entry.email})<br>
        </c:forEach>
    </div>
</div>

<%@include file="../footer.jspf" %>

<script>
    const checkboxes = document.querySelectorAll('.addrCheckbox');
    const toField = document.getElementById('toField');

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', () => {
            const selectedEmails = Array.from(checkboxes)
                .filter(cb => cb.checked)
                .map(cb => cb.value)
                .join(', ');

            toField.value = selectedEmails;
        });
    });
</script>
</body>
</html>
