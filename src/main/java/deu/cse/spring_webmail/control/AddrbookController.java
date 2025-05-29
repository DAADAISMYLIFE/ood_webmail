package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/addrbook")
@Slf4j
public class AddrbookController {

    static final String UID_SESSION = "userid";
    static final String ERR_MSG = "errorMessage";

    private final AddrbookService service;

    @Autowired
    public AddrbookController(AddrbookService service) {
        this.service = service;
    }

    /**
     * 주소록 조회
     */
    @GetMapping("/list")
    public String listAddrbook(@RequestParam(required = false) String keyword,
                               HttpSession session, Model model) {
        String user = (String) session.getAttribute(UID_SESSION);
        List<Addrbook> addrbookList = (keyword == null || keyword.isBlank())
                ? service.getAddrbookList(user)
                : service.searchAddrbookList(user, keyword);

        model.addAttribute("addrbookList", addrbookList);
        model.addAttribute("keyword", keyword);
        return "addrbook/list";
    }

    /**
     * 주소록 등록 페이지
     */
    @GetMapping("/adduser")
    public String showAdduserPage() {
        return "addrbook/adduser";
    }

    /**
     * 주소록 등록 처리
     */
    @PostMapping("/adduser")
    public String adduserAddrbook(@RequestParam String email,
                                  @RequestParam String name,
                                  @RequestParam String phone,
                                  HttpSession session,
                                  Model model) {
        if (email == null || email.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            model.addAttribute(ERR_MSG, "이메일과 이름은 필수 입력 항목입니다.");
            return "redirect:/main_menu";
        }

        String user = (String) session.getAttribute(UID_SESSION);
        boolean success = service.registerAddrbook(user, email, name, phone);

        if (success) {
            return "redirect:/addrbook/list";
        } else {
            model.addAttribute(ERR_MSG, "등록에 실패하였습니다.");
            return "addrbook/error";
        }
    }

    /**
     * 주소록 삭제
     */
    @GetMapping("/delete")
    public String deleteAddrbook(@RequestParam String email, HttpSession session, Model model) {
        String user = (String) session.getAttribute(UID_SESSION);
        boolean success = service.deleteAddrbook(user, email);

        if (success) {
            return "redirect:/addrbook/list";
        } else {
            model.addAttribute(ERR_MSG, "삭제에 실패하였습니다.");
            return "addrbook/error";
        }
    }
}
