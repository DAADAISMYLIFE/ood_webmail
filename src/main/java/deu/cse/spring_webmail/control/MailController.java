/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.service.MailListService;
import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.mail.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 전체 메일 목록을 페이징 검색 메일 목록 페이징은 MailSearchController 담당
 *
 * @author keyrb
 */
@Controller
public class MailController {

    @GetMapping("/main_menu")
    public String showMailList(HttpSession session, @RequestParam(value = "sort", defaultValue = "desc") String sortOrder,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");
        String password = (String) session.getAttribute("password");

        Pop3Agent agent = new Pop3Agent(host, userid, password);
        Message[] messages = agent.getMessages();

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) messages.length / pageSize);

        MailListService service = new MailListService(userid);
        String result = service.buildPagedMessageList(messages, sortOrder, page, pageSize);

        model.addAttribute("totalPages", totalPages);
        model.addAttribute("messageList", result);
        return "main_menu";
    }
}
