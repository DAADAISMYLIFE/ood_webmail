package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.model.AddrbookService;
import deu.cse.spring_webmail.model.SmtpAgent;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    private ServletContext ctx;

    @Autowired
    private HttpSession session;

    /**
     * 주소록 조회
     */
    @GetMapping("/addrbook/list")
    public String listAddrbook(HttpSession session, Model model) {
        String user = (String) session.getAttribute("userid");
        AddrbookService service = new AddrbookService();
        List<Addrbook> addrbookList = service.getAddrbookList(user);
        model.addAttribute("addrbookList", addrbookList);
        return "addrbook/list";
    }

    /**
     * 주소록 등록 페이지 이동
     */
    @GetMapping("/addrbook/adduser")
    public String showAdduserPage() {
        return "addrbook/adduser";
    }

    /**
     * 주소록 등록 처리
     */
    @PostMapping("/addrbook/adduser")
    public String adduserAddrbook(@RequestParam String email,
                                  @RequestParam String name,
                                  @RequestParam String phone,
                                  HttpSession session,
                                  Model model) {
        if (email == null || email.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            model.addAttribute("errorMessage", "이메일과 이름은 필수 입력 항목입니다.");
            return "addrbook/error";
        }
        String user = (String) session.getAttribute("userid");
        AddrbookService service = new AddrbookService();
        boolean success = service.registerAddrbook(user, email, name, phone);

        if (success) {
            return "redirect:/addrbook/list";
        } else {
            model.addAttribute("errorMessage", "등록에 실패하였습니다.");
            return "addrbook/error";
        }
    }

    /**
     * 주소록 삭제
     */
    @GetMapping("/addrbook/delete")
    public String deleteAddrbook(@RequestParam String email, HttpSession session, Model model) {
        String user = (String) session.getAttribute("userid");
        AddrbookService service = new AddrbookService();
        boolean success = service.deleteAddrbook(user, email);

        if (success) {
            return "redirect:/addrbook/list";
        } else {
            model.addAttribute("errorMessage", "삭제에 실패하였습니다.");
            return "addrbook/error";
        }
    }

    /**
     * 메일 작성 화면 + 주소록 데이터 제공
     */
    @GetMapping("/write_mail")
    public String writeMail(Model model, HttpSession session) {
        log.debug("write_mail called...");
        session.removeAttribute("sender");

        String user = (String) session.getAttribute("userid");
        AddrbookService service = new AddrbookService();
        List<Addrbook> addrbookList = service.getAddrbookList(user);
        model.addAttribute("addrbookList", addrbookList);

        return "write_mail/write_mail";
    }

    /**
     * 메일 전송 처리
     */
    @PostMapping("/write_mail.do")
    public String writeMailDo(@RequestParam String to, @RequestParam String cc,
                              @RequestParam String subj, @RequestParam String body,
                              @RequestParam(name="file1") MultipartFile[] upFiles,
                              RedirectAttributes attrs) {
        log.debug("write_mail.do: to = {}, cc = {}, subj = {}, body = {}, file1 = {}",
                to, cc, subj, body, upFiles.length);

        //메일 제목이 공란인 경우 제목 없음으로 제목 대체후 하이퍼 링크 생성해 내용확인가능
        if (subj == null || subj.trim().isEmpty()) {
            subj = "제목 없음";
        }
        
        // FormParser 클래스의 기능은 매개변수로 모두 넘어오므로 더이상 필요 없음.
        // 업로드한 파일이 있으면 해당 파일을 UPLOAD_FOLDER에 저장해 주면 됨.
        for (MultipartFile upFile : upFiles) {
            if (!"".equals(upFile.getOriginalFilename())) {
                String basePath = ctx.getRealPath(UPLOAD_FOLDER);
                log.debug("{} 파일을 {} 폴더에 저장...", upFile.getOriginalFilename(), basePath);
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
     * FormParser 클래스를 사용하지 않고 Spring Framework에서 이미 획득한 매개변수 정보를 사용하도록
     * 기존 webmail 소스 코드를 수정함.
     * 
     * @param to
     * @param cc
     * @param subject
     * @param body
     * @param upFiles
     * @return 
     */

    private boolean sendMessage(String to, String cc, String subject, String body, MultipartFile[] upFiles) {
        boolean status = false;

        // 1. toAddress, ccAddress, subject, body, file1 정보를 파싱하여 추출
        // 2.  request 객체에서 HttpSession 객체 얻기
        // 3. HttpSession 객체에서 메일 서버, 메일 사용자 ID 정보 얻기
        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");

        // 4. SmtpAgent 객체에 메일 관련 정보 설정
        SmtpAgent agent = new SmtpAgent(host, userid);
        agent.setTo(to);
        agent.setCc(cc);
        agent.setSubj(subject);
        agent.setBody(body);
        
        for (MultipartFile upFile : upFiles) {
            String fileName = upFile.getOriginalFilename();
            if (fileName != null && !"".equals(fileName)) {
                log.debug("sendMessage: 파일({}) 첨부 필요", fileName);
                File f = new File(ctx.getRealPath(UPLOAD_FOLDER) + File.separator + fileName);
                agent.addAttachment(f.getAbsolutePath());
            }
        }
        // 5. 메일 전송 권한 위임
        if (agent.sendMessage()) {
            status = true;
        }
        return status;
    }  // sendMessage()
}

