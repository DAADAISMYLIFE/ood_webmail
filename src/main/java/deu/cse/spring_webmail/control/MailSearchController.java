/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.service.MailSearchService;
import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.mail.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author keyrb
 */
@Controller
public class MailSearchController {

    private final AgentFactory agentFactory;
    
    @Autowired
    public MailSearchController(AgentFactory agentFactory){
        this.agentFactory = agentFactory;
    }

    @PostMapping("/search_mail")
    public String searchMail(HttpSession session, @RequestParam("keyword") String keyword,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");
        String password = (String) session.getAttribute("password");

        Pop3Agent agent = agentFactory.pop3AgentCreate(host, userid, password);
        Message[] messages = agent.getMessages();

        int pageSize = 10;
        MailSearchService service = new MailSearchService(userid);  // 사용자별 포맷 유지
        String result = service.filterMessagesByKeyword(messages, keyword, sort, page, pageSize);  // HTML 리턴
        int totalPages = service.getTotalPages();

        model.addAttribute("messageList", result);
        model.addAttribute("totalPages", totalPages);

        return "main_menu";  // main_menu.jsp로 반환
    }
}
