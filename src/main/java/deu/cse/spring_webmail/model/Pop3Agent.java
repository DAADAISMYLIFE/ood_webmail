/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import java.util.Properties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author skylo
 */
@Slf4j
@NoArgsConstructor        // 기본 생성자 생성
public class Pop3Agent {

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private String userid;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private Store store;
    @Getter
    @Setter
    private String excveptionType;
    @Getter
    @Setter
    private HttpServletRequest request;

    // 220612 LJM - added to implement REPLY
    @Getter
    private String sender;
    @Getter
    private String subject;
    @Getter
    private String body;

    public Pop3Agent(String host, String userid, String password) {
        this.host = host;
        this.userid = userid;
        this.password = password;
    }

    public boolean validate() {
        boolean status = false;

        try {
            status = connectToStore();
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.validate() error : " + ex);
            status = false;  // for clarity
        } finally {
            return status;
        }
    }

    public boolean deleteMessage(int msgid, boolean really_delete) {
        boolean status = false;

        if (!connectToStore()) {
            return status;
        }

        try {
            // Folder 설정
//            Folder folder = store.getDefaultFolder();
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            // Message에 DELETED flag 설정
            Message msg = folder.getMessage(msgid);
            msg.setFlag(Flags.Flag.DELETED, really_delete);

            // 폴더에서 메시지 삭제
            // Message [] expungedMessage = folder.expunge();
            // <-- 현재 지원 안 되고 있음. 폴더를 close()할 때 expunge해야 함.
            folder.close(true);  // expunge == true
            store.close();
            status = true;
        } catch (Exception ex) {
            log.error("deleteMessage() error: {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    /*
     * 페이지 단위로 메일 목록을 보여주어야 함.
     */
    
    // getMessage 메소드 수정 (페이징 기능) 
    public String getMessage(int msgid) {
        String result = "POP3 서버 연결이 되지 않아 메시지를 볼 수 없습니다.";

        if (!connectToStore()) {
            log.error("POP3 connection failed!");
            return result;
        }

        try {
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, profile);

            // 최신순 정렬 (페이징/목록 화면 기준)
            List<Message> messageList = new ArrayList<>(List.of(messages));
            messageList.sort((m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (Exception e) {
                    return 0;
                }
            });

            if (msgid <= 0 || msgid > messageList.size()) {
                return "유효하지 않은 메시지 번호입니다.";
            }

            Message message = messageList.get(msgid - 1);  // 1-based index

            MessageFormatter formatter = new MessageFormatter(userid);
            formatter.setRequest(request);
            result = formatter.getMessage(message);

            sender = formatter.getSender();
            subject = formatter.getSubject();
            body = formatter.getBody();

            folder.close(true);
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessage() 예외: {}", ex.getMessage());
            result = "Pop3Agent.getMessage() 예외: " + ex;
        }

        return result;
    }

    // 호출만 함
    public String getMessageList() {
        return getMessageList(1, Integer.MAX_VALUE, "desc");
    }

    // 페이징, 정렬 기능이 포함된 새로운 메서드 추가 (오버로딩)
    public String getMessageList(int page, int pageSize, String sortOrder) {
        String result = "";
        Message[] messages;

        if (!connectToStore()) {
            log.error("POP3 connection failed!");
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }

        try {
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            messages = folder.getMessages();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, fp);

            // 정렬: 날짜 기준 asc/desc
            List<Message> messageList = List.of(messages);
            messageList = new ArrayList<>(messageList);  // mutable list

            messageList.sort((m1, m2) -> {
                try {
                    if ("asc".equalsIgnoreCase(sortOrder)) {
                        return m1.getSentDate().compareTo(m2.getSentDate());
                    } else {
                        return m2.getSentDate().compareTo(m1.getSentDate());
                    }
                } catch (Exception e) {
                    return 0;
                }
            });

            // 페이징 처리
            int total = messageList.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            if (start >= total) {
                result = "<p style='color: gray;'>해당 페이지에 메일이 없습니다.</p>";
            } else {
                List<Message> pagedMessages = messageList.subList(start, end);
                MessageFormatter formatter = new MessageFormatter(userid);
                result = formatter.getMessageTable(pagedMessages.toArray(new Message[0]));
            }

            folder.close(true);
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList(page) 예외: {}", ex.getMessage());
            result = "Pop3Agent.getMessageList(page) 예외: " + ex.getMessage();
        }

        return result;
    }

    private boolean connectToStore() {
        boolean status = false;
        Properties props = System.getProperties();
        // https://jakarta.ee/specifications/mail/2.1/apidocs/jakarta.mail/jakarta/mail/package-summary.html
        props.setProperty("mail.pop3.host", host);
        props.setProperty("mail.pop3.user", userid);
        props.setProperty("mail.pop3.apop.enable", "false");
        props.setProperty("mail.pop3.disablecapa", "true");  // 200102 LJM - added cf. https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
        props.setProperty("mail.debug", "false");
        props.setProperty("mail.pop3.debug", "false");

        Session session = Session.getInstance(props);
        session.setDebug(false);

        try {
            store = session.getStore("pop3");
            store.connect(host, userid, password);
            status = true;
        } catch (Exception ex) {
            log.error("connectToStore 예외: {}", ex.getMessage());
        } finally {
            return status;
        }
    }

    // 검색 기능 추가를 위한 getMessages() 메서드 추가
    // Message[]를 리턴하여 Controller에서 받아서 MailService에서 필터링
    // 기존의 HTML 문자열 리턴 방식을 삭제하진 않음.
    public Message[] getMessages() {
        if (!connectToStore()) {
            log.error("POP3 연결 실패");
            return new Message[0];
        }

        try {
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, profile);
            return messages;
        } catch (Exception ex) {
            log.error("getMessages() 예외: {}", ex.getMessage());
            return new Message[0];
        }
    }

    // 메일 안전한 삭제를 위해 추가
    // 정렬 전 순서 Pop3의 n번째 메일 가져오기 -> 메일 헤더 id값 순회하여 찾기
    public List<String> getMessageIdList() {
        List<String> idList = new ArrayList<>();
        if (!connectToStore()) {
            return idList;
        }

        try {
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();
            for (Message message : messages) {
                String[] ids = message.getHeader("Message-ID");
                if (ids != null && ids.length > 0) {
                    idList.add(ids[0]);
                } else {
                    idList.add("<no-id>");
                }
            }
            folder.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Pop3Agent.getMessageIdList() error : " + e);
        }
        return idList;
    }

}
