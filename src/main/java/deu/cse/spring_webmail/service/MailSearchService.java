/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.util.MailTableUtil;
import jakarta.mail.Message;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 메일 제목 또는 발신자 검색 기능 담당 서비스 클래스 검색 결과 페이징도 같이 담당
 *
 * @author keyrb
 */
@Slf4j
public class MailSearchService {

    final String userid;
    private int totalPages;

    public MailSearchService(String userid) {
        this.userid = userid;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String filterMessagesByKeyword(Message[] messages, String keyword, String sortOrder, int page, int pageSize) {

        List<Message> filteredList = new ArrayList<>();
        keyword = keyword.toLowerCase();  // 대소문자 무시

        for (Message message : messages) {
            try {
                String subject = message.getSubject() != null ? message.getSubject().toLowerCase() : "";
                String from = MailTableUtil.extractSender(message).toLowerCase();

                if (subject.contains(keyword) || from.contains(keyword)) {
                    filteredList.add(message);
                }
            } catch (Exception e) {
                log.error("오류 발생", e);
            }
        }

        // 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) filteredList.size() / pageSize);

        // 정렬(최신순, 오래된 순) 추가
        Comparator<Message> comparator = Comparator.comparing(m -> {
            try {
                return m.getSentDate();
            } catch (Exception e) {
                return new Date(0);  // fallback
            }
        });
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        filteredList.sort(comparator);

        // 검색 결과가 없을 경우
        if (filteredList.isEmpty()) {
            return "<p style='color: gray;'>검색 결과가 없습니다.</p>";
        }

        // 페이징 처리
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredList.size());
        List<Message> pagedList = filteredList.subList(start, end);

        return MailTableUtil.buildMessageTable(pagedList);
    }

}
