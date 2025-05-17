/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.MessageFormatter;
import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Prof.Jong Min Lee
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class ReadController {

    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Value("${file.download_folder}")
    private String DOWNLOAD_FOLDER;

    // show_message 에서 url 경로를 안보이게 설정하기 위해
    // messageId값 받는 부분 따로 만듬
    @GetMapping("/select_message")
    public String selectMessage(@RequestParam String id) {
        session.setAttribute("selectedMessageId", id);
        return "redirect:/show_message";
    }

    // 기존 메서드 주석 처리함. 메일 테이블 생성 시
    // message-id 포함 링크로 변경하였음
    // url 안보이게 처리
    @GetMapping("/show_message")
    public String showMessageById(Model model) {
        String id = (String) session.getAttribute("selectedMessageId");
        if (id == null || id.isBlank()) {
            model.addAttribute("msg", "잘못된 접근입니다. 메일이 선택되지 않았습니다.");
            return "/read_mail/show_message";
        }

        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");
        String password = (String) session.getAttribute("password");

        Pop3Agent agent = new Pop3Agent(host, userid, password);
        Message[] messages = agent.getMessages();

        for (Message msg : messages) {
            try {
                String[] headers = msg.getHeader("Message-ID");
                if (headers != null && headers.length > 0) {
                    String messageIdHeader = headers[0].replaceAll("[<>]", "");
                    if (messageIdHeader.equals(id)) {
                        MessageFormatter formatter = new MessageFormatter(userid);
                        formatter.setRequest(request);
                        String content = formatter.getMessage(msg);
                        session.setAttribute("sender", formatter.getSender());
                        session.setAttribute("subject", formatter.getSubject());
                        session.setAttribute("body", formatter.getBody());
                        model.addAttribute("msg", content);
                        return "/read_mail/show_message";
                    }
                }
            } catch (Exception e) {
                log.error("오류 발생", e);
            }
        }
        model.addAttribute("msg", "해당 메일을 찾을 수 없습니다.");
        return "/read_mail/show_message";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("userid") String userId,
            @RequestParam("filename") String fileName) {
        log.debug("userid = {}, filename = {}", userId, fileName);

        // 1. 내려받기할 파일의 기본 경로 설정
        String basePath = ctx.getRealPath(DOWNLOAD_FOLDER) + File.separator + userId;

        // 2. 파일의 Content-Type 찾기
        Path path = Paths.get(basePath + File.separator + fileName);
        String contentType = null;
        try {
            contentType = Files.probeContentType(path);
            log.debug("File: {}, Content-Type: {}", path.toString(), contentType);
        } catch (IOException e) {
            log.error("downloadDo: 오류 발생 - {}", e.getMessage());
        }

        // 3. Http 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.builder("attachment").filename(fileName, StandardCharsets.UTF_8).build());

        // ContentType 이 null 일 경우, 기본값 지정 (추가)
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        // 4. 파일을 입력 스트림으로 만들어 내려받기 준비
        Resource resource = null;
        try {
            resource = new InputStreamResource(Files.newInputStream(path));
        } catch (IOException e) {
            log.error("downloadDo: 오류 발생 - {}", e.getMessage());
        }
        if (resource == null) {
            log.error("요청한 파일이 존재하지 않음: {}", path.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    // 기존의 삭제 방식(msgid 대신 message id 기반으로 삭제)
    @GetMapping("/delete_mail.do")
    public String deleteMailDo(@RequestParam("messageId") String messageId, RedirectAttributes attrs) {
        log.debug("delete_mail.do: messageId = {}", messageId);

        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");
        String password = (String) session.getAttribute("password");

        Pop3Agent agent = new Pop3Agent(host, userid, password);
        Message[] messages = agent.getMessages();

        boolean found = false;
        String extractedId = messageId.replaceAll("[<>]", "");

        for (int i = 0; i < messages.length; i++) {
            try {
                String[] headers = messages[i].getHeader("Message-ID");
                if (headers != null && headers.length > 0) {
                    String actualId = headers[0].replaceAll("[<>]", "");  // 꺾쇠 제거
                    if (actualId.equals(extractedId)) {
                        found = agent.deleteMessage(i + 1, true);  // POP3는 1부터 시작
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("오류 발생", e);
            }
        }

        if (found) {
            attrs.addFlashAttribute("msg", "메시지 삭제를 성공하였습니다.");
        } else {
            attrs.addFlashAttribute("msg", "해당 메시지를 찾을 수 없습니다.");
        }

        return "redirect:main_menu";
    }
}
