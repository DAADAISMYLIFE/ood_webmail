/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.util;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import java.text.SimpleDateFormat;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author keyrb
 */
@Slf4j
public class MailTableUtil {

    // private 생성자 추가
    private MailTableUtil() {
    }

    // 기본 버전: msgNo = 1부터 시작 (검색용)
    public static String buildMessageTable(List<Message> messages) {
        return buildMessageTable(messages, 0);  // startIndex = 0
    }

    // 오버로딩: msgNo = startIndex + 1부터 시작(페이징용)
    public static String buildMessageTable(List<Message> messages, int startIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        sb.append("<tr><th>번호</th><th>보낸 사람</th><th>제목</th><th>날짜</th><th>삭제</th></tr>");

        int msgNo = startIndex + 1;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Message message : messages) {
            try {
                String from = extractSender(message);
                String subject = message.getSubject() != null ? message.getSubject() : "(제목 없음)";
                String date = format.format(message.getSentDate());

                // Message-ID 추출
                String[] messageIdHeader = message.getHeader("Message-ID");
                String messageId = (messageIdHeader != null && messageIdHeader.length > 0)
                        ? messageIdHeader[0].replaceAll("[<>]", "")
                        : "";

                sb.append("<tr>");
                sb.append(String.format("<td>%d</td>", msgNo));
                sb.append(String.format("<td>%s</td>", from));
                sb.append(String.format(
                        "<td id='subject'><a href='select_message?id=%s'>%s</a></td>",
                        messageId, subject));
                sb.append(String.format("<td>%s</td>", date));
                sb.append(String.format(
                        "<td><a href='delete_mail.do?messageId=%s' onclick=\"return confirm('정말 삭제하시겠습니까?');\">삭제</a></td>",
                        messageId));
                sb.append("</tr>");

                msgNo++;
            } catch (Exception e) {
                log.error("메시지 테이블 빌드 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        sb.append("</table>");
        return sb.toString();
    }

    public static String extractSender(Message message) throws MessagingException {
        Address[] froms = message.getFrom();
        if (froms != null && froms.length > 0) {
            return ((InternetAddress) froms[0]).getAddress();
        } else {
            return "";
        }
    }

}
