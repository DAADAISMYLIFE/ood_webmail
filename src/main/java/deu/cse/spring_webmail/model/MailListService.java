/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import deu.cse.spring_webmail.util.MailTableUtil;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author keyrb
 */
public class MailListService {

    private final String userid;

    public MailListService(String userid) {
        this.userid = userid;
    }

    public String buildPagedMessageList(Message[] messages, String sortOrder, int page, int pageSize) {
        List<Message> messageList = Arrays.asList(messages);

        // 정렬: 최신순(desc) 또는 오래된 순(asc)
        Comparator<Message> comparator = Comparator.comparing(m -> {
            try {
                return m.getSentDate();
            } catch (Exception e) {
                return new Date(0);  // 예외 시 가장 오래된 것으로 처리
            }
        });
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        messageList.sort(comparator);

        // 페이징
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, messageList.size());
        if (start >= messageList.size()) {
            return "<p style='color: gray;'>표시할 메일이 없습니다.</p>";
        }

        List<Message> pagedList = messageList.subList(start, end);

        return MailTableUtil.buildMessageTable(pagedList, userid, start);
    }
}
