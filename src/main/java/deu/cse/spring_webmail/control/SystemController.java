/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.model.ImageManager;
import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import java.util.LinkedList;
import java.util.List;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 초기 화면과 관리자 기능(사용자 추가, 삭제)에 대한 제어기
 *
 * @author skylo
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class SystemController {

    private static final String UID_SESSION_KEY = "userid"; // 세션에 저장된 사용자 ID 키

    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;

    private final ImageManager imageManager;
    private final AgentFactory agentFactory;

    @Value("${root.id}")
    private String ROOT_ID;
    @Value("${root.password}")
    private String ROOT_PASSWORD;
    @Value("${admin.id}")
    private String ADMINISTRATOR;  //  = "admin";
    @Value("${james.control.port}")
    private Integer JAMES_CONTROL_PORT;
    @Value("${james.host}")
    private String JAMES_HOST;

    @Autowired
    public SystemController(AgentFactory agentFactory, ImageManager imageManager) {
        this.imageManager = imageManager;
        this.agentFactory = agentFactory;
    }

    @GetMapping("/")
    public String index() {
        log.debug("index() called...");
        session.setAttribute("host", JAMES_HOST);
        session.setAttribute("debug", "false");

        String userId = (String) session.getAttribute(UID_SESSION_KEY); // 세션에서 사용자 ID를 가져옴

        if (userId != null && !userId.trim().isEmpty()) {
            // 이미 로그인된 사용자
            return moveToMainMenu(userId);
        }
        // 로그인되지 않았다면 기존처럼 로그인 페이지로
        log.debug("No user logged in. Showing index page.");
        return "/index";
    }

    // 유저/어드민 메인 페이지 이동 메서드
    private String moveToMainMenu(String rawUserId) {
        String url = "redirect:/main_menu";
        if (isAdmin(rawUserId)) {
            url = "redirect:/admin_menu";
            log.debug("ADMIN user ({}) 로그인 중. /admin_menu로 리다이렉트 합니다. ", rawUserId);
        } else {
            log.debug("User ({}) 로그인 중. /main_menu로 리다이렉트 합니다. ", rawUserId);

        }
        return url;
    }

    // 도메인 후보 탐색. james 서버에서 사용자 관리를 하기 때문에
    // 사용자가 로그인을 하면, 아이디와 도메인을 함께 탐색하여 자동 로그인하게 해줌
    private List<String> getDomainCandidates(String userIdWithoutDomain) {
        // 관리자 권한으로 사용자 리스트를 가져옴
        String cwd = ctx.getRealPath(".");
        UserAdminAgent agent = agentFactory.userAdminAgentCreate(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
        List<String> fullUserList = agent.getUserList();

        // 일치하는 아이디를 기반으로 후보 목록 추출
        List<String> candidates = new LinkedList<>();
        for (String user : fullUserList) {
            if (user.startsWith(userIdWithoutDomain + "@")) {
                candidates.add(user);
            }
        }
        return candidates;
    }

    @RequestMapping(value = "/login.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginDo(@RequestParam Integer menu, Model model) {
        String url = "";
        log.debug("로그인 처리: menu = {}", menu);

        switch (menu) {
            case CommandType.LOGIN:
                String host = (String) request.getSession().getAttribute("host");
                String rawUserId = request.getParameter(UID_SESSION_KEY);
                String password = request.getParameter("passwd");

                // '@' 없는 경우 후보 조회
                if (!rawUserId.contains("@")) {
                    List<String> candidates = getDomainCandidates(rawUserId);
                    if (!candidates.isEmpty()) {
                        rawUserId = candidates.get(0); // 도메인 자동 매칭
                    }
                }

                // POP3 인증 시도
                Pop3Agent pop3Agent = agentFactory.pop3AgentCreate(host, rawUserId, password);
                boolean isLoginSuccess = pop3Agent.validate();

                if (isLoginSuccess) {
                    session.setAttribute(UID_SESSION_KEY, rawUserId);
                    session.setAttribute("password", password);

                    url = moveToMainMenu(rawUserId);
                } else {
                    url = "redirect:/login_fail";
                }
                break;

            case CommandType.LOGOUT:
                session.invalidate();
                url = "redirect:/";
                break;

            default:
                break;
        }

        return url;
    }

    @GetMapping("/login_fail")
    public String loginFail() {
        return "login_fail";
    }

    protected boolean isAdmin(String userid) {
        boolean status = false;

        if (userid.equals(this.ADMINISTRATOR)) {
            status = true;
        }

        return status;
    }

    @GetMapping("/admin_menu")
    public String adminMenu(Model model) {
        String sessionUserid = (String) session.getAttribute(UID_SESSION_KEY);

        // 비로그인 차단 및 비관리자 차단
        if (sessionUserid == null || !isAdmin(sessionUserid)) {
            return "redirect:/login_fail";
        }

        // 관리자만 로그인
        log.debug("root.id = {}, root.password = {}, admin.id = {}",
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

        model.addAttribute("userList", getUserList());
        return "admin/admin_menu";
    }

    @GetMapping("/add_user")
    public String addUser() {
        return "admin/add_user";
    }

    @PostMapping("/add_user.do")
    public String addUserDo(@RequestParam String id, @RequestParam String password,
            RedirectAttributes attrs) {
        log.debug("add_user.do: id = {}, password = {}, port = {}",
                id, password, JAMES_CONTROL_PORT);

        try {
            String cwd = ctx.getRealPath(".");
            UserAdminAgent agent = agentFactory.userAdminAgentCreate(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

            // if 사용자 등록 성공 팦업창
            // else 사용자 등록 실패 팝업창
            if (agent.addUser(id, password)) {
                attrs.addFlashAttribute("msg", String.format("사용자(%s) 추가를 성공하였습니다.", id));
            } else {
                attrs.addFlashAttribute("msg", String.format("사용자(%s) 추가를 실패하였습니다.", id));
            }
        } catch (Exception ex) {
            log.error("add_user.do: 시스템 접속에 실패했습니다. 예외 = {}", ex.getMessage());
        }

        return "redirect:/admin_menu";
    }

    @GetMapping("/delete_user")
    public String deleteUser(Model model) {
        log.debug("delete_user called");
        model.addAttribute("userList", getUserList());
        return "admin/delete_user";
    }

    /**
     *
     * @param selectedUsers <input type=checkbox> 필드의 선택된 이메일 ID. 자료형: String[]
     * @param attrs
     * @return
     */
    @PostMapping("delete_user.do")
    public String deleteUserDo(@RequestParam String[] selectedUsers, RedirectAttributes attrs) {
        log.debug("delete_user.do: selectedUser = {}", List.of(selectedUsers));

        try {
            String cwd = ctx.getRealPath(".");
            UserAdminAgent agent = agentFactory.userAdminAgentCreate(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
            agent.deleteUsers(selectedUsers);  // 수정!!!
        } catch (Exception ex) {
            log.error("delete_user.do : 예외 = {}", ex);
        }

        return "redirect:/";
    }

    private List<String> getUserList() {
        String cwd = ctx.getRealPath(".");
        UserAdminAgent agent = agentFactory.userAdminAgentCreate(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
        List<String> userList = agent.getUserList();
        log.debug("userList = {}", userList);

        //(주의) root.id와 같이 '.'을 넣으면 안 됨.
        userList.sort((e1, e2) -> e1.compareTo(e2));
        return userList;
    }

    @GetMapping("/img_test")
    public String imgTest() {
        return "img_test/img_test";
    }

    /**
     * https://34codefactory.wordpress.com/2019/06/16/how-to-display-image-in-jsp-using-spring-code-factory/
     *
     * @param imageName
     * @return
     */
    @GetMapping(value = "/get_image/{imageName}")
    @ResponseBody
    public byte[] getImage(@PathVariable String imageName) {
        try {
            String folderPath = ctx.getRealPath("/WEB-INF/views/img_test/img");
            return imageManager.getImageBytes(folderPath, imageName);
        } catch (Exception e) {
            log.error("/get_image 예외: {}", e.getMessage());
        }
        return new byte[0];
    }
}
