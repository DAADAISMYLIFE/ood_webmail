package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.model.SmtpAgent;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class WriteController {

    @Value("${file.upload_folder}")
    private String UPLOAD_FOLDER;

    @Value("${file.max_size}")
    private String MAX_SIZE;

    @Autowired
    private ServletContext ctx;

    @Autowired
    private HttpSession session;

    private final AddrbookService addrbookService;
    private final AgentFactory agentFactory;

    static final String UID_SESSION = "userid";

    @Autowired
    public WriteController(AgentFactory agentFactory, AddrbookService addrbookService) {
        this.agentFactory = agentFactory;
        this.addrbookService = addrbookService;
    }

    /**
     * 메일 작성 화면 + 주소록 데이터 제공
     */
    @GetMapping("/write_mail")
    public String writeMail(@RequestParam(required = false) String keyword,
                            Model model, HttpSession session) {
        log.debug("write_mail called...");
        session.removeAttribute("sender");

        String user = (String) session.getAttribute(UID_SESSION);
        List<Addrbook> addrbookList = (keyword == null || keyword.isBlank())
                ? addrbookService.getAddrbookList(user)
                : addrbookService.searchAddrbookList(user, keyword);

        model.addAttribute("addrbookList", addrbookList);
        model.addAttribute("keyword", keyword);

        return "write_mail/write_mail";
    }

    /**
     * 메일 전송 처리
     */
    @PostMapping("/write_mail.do")
    public String writeMailDo(@RequestParam String to, @RequestParam String cc,
                              @RequestParam String subj, @RequestParam String body,
                              @RequestParam(name = "file1") MultipartFile[] upFiles,
                              RedirectAttributes attrs) {
        log.debug("write_mail.do: to = {}, cc = {}, subj = {}, body = {}, file1 = {}", to, cc, subj, body, upFiles.length);

        if (subj == null || subj.trim().isEmpty()) {
            subj = "제목 없음";
        }

        for (MultipartFile upFile : upFiles) {
            if (!"".equals(upFile.getOriginalFilename())) {
                String userid = (String) session.getAttribute(UID_SESSION);
                String basePath = ctx.getRealPath("/WEB-INF/download") + File.separator + userid;

                File userDir = new File(basePath);
                if (!userDir.exists()) {
                    userDir.mkdirs();
                }

                File f = new File(basePath + File.separator + upFile.getOriginalFilename());
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))) {
                    bos.write(upFile.getBytes());
                } catch (IOException e) {
                    log.error("upload.do: 오류 발생 - {}", e.getMessage());
                }
            }
        }

        boolean sendSuccessful = sendMessage(to, cc, subj, body, upFiles);

        if (sendSuccessful) {
            attrs.addFlashAttribute("msg", "메일 전송이 성공했습니다.");
        } else {
            attrs.addFlashAttribute("msg", "메일 전송이 실패했습니다.");
        }

        return "redirect:/main_menu";
    }

    /**
     * 메일 전송 로직
     */
    private boolean sendMessage(String to, String cc, String subject, String body, MultipartFile[] upFiles) {
        boolean status = false;

        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute(UID_SESSION);

        SmtpAgent agent = agentFactory.smtpAgentCreate(host, userid);
        agent.setTo(to);
        agent.setCc(cc);
        agent.setSubj(subject);
        agent.setBody(body);

        for (MultipartFile upFile : upFiles) {
            String fileName = upFile.getOriginalFilename();
            if (fileName != null && !"".equals(fileName)) {
                File f = new File(ctx.getRealPath("/WEB-INF/download") + File.separator + userid + File.separator + fileName);
                agent.addAttachment(f.getAbsolutePath());
            }
        }

        if (agent.sendMessage()) {
            status = true;
        }

        return status;
    }
}
