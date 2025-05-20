/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.ControllerTest;

import deu.cse.spring_webmail.control.SystemController;
import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.model.ImageManager;
import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import jakarta.mail.Message;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(SystemController.class)

class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentFactory agentFactory;

    @MockBean
    private ImageManager imageManager;

    private static final int COMMAND_LOGIN = 91;
    private static final int COMMAND_LOGOUT = 92;

    // 인덱스 페이지
    @Test
    void indexTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("/index"));
    }

    // 로그인 기능
    @Test
    void loginAdminSuccessTest() throws Exception {
        String testUserid = "admin@webmail.com";
        String testPassword = "admin";
        String testHost = "test.webmail.com";

        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);

        // agentFactory 클래스의 pop3AgentCreate 메서드가 실행될 경우 mockPop3Agent를 리턴함
        BDDMockito.given(agentFactory.pop3AgentCreate(
                anyString(),
                eq(testUserid),
                eq(testPassword))
        ).willReturn(mockPop3Agent);

        // mockPop3Agent가 validate를 하면 true를 리턴함
        BDDMockito.given(mockPop3Agent.validate()).willReturn(true);

        // login.do로 post 메서드를 테스트
        mockMvc.perform(MockMvcRequestBuilders.post("/login.do")
                .param("menu", String.valueOf(COMMAND_LOGIN))
                .param("userid", testUserid)
                .param("passwd", testPassword)
                .sessionAttr("host", testHost)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"));
    }

    @Test
    void loginUserSuccessTest() throws Exception {
        String testUserid = "test@webmail.com";
        String testPassword = "test_password";
        String testHost = "test.webmail.com";

        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);

        BDDMockito.given(agentFactory.pop3AgentCreate(
                anyString(),
                eq(testUserid),
                eq(testPassword))
        ).willReturn(mockPop3Agent);

        BDDMockito.given(mockPop3Agent.validate()).willReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/login.do")
                .param("menu", String.valueOf(COMMAND_LOGIN))
                .param("userid", testUserid)
                .param("passwd", testPassword)
                .sessionAttr("host", testHost)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"));
    }

    @Test
    void loginFailTest() throws Exception {
        String testUserid = "fail@webmail.com";
        String testPassword = "wrong_password";
        String testHost = "test.webmail.com";

        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);

        BDDMockito.given(agentFactory.pop3AgentCreate(
                anyString(),
                eq(testUserid),
                eq(testPassword))
        ).willReturn(mockPop3Agent);

        BDDMockito.given(mockPop3Agent.validate()).willReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/login.do")
                .param("menu", String.valueOf(COMMAND_LOGIN))
                .param("userid", testUserid)
                .param("passwd", testPassword)
                .sessionAttr("host", testHost)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_fail"));
    }

    @Test
    void logoutTest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userid", "loggedInUser");

        mockMvc.perform(MockMvcRequestBuilders.get("/login.do")
                .param("menu", String.valueOf(COMMAND_LOGOUT))
                .session(session)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginFailViewTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login_fail"))
                .andExpect(status().isOk())
                .andExpect(view().name("login_fail"));
    }

    @Test
    void loginUserSuccessWithDomainCandidateTest() throws Exception {
        String userIdWithoutDomain = "user";
        String fullUserid = "user@domain.com";
        String testPassword = "test_password";
        String testHost = "test.webmail.com";

        // 1. Mock UserAdminAgent 설정
        UserAdminAgent mockUserAdminAgent = Mockito.mock(UserAdminAgent.class);
        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(), anyString(), anyString(), anyString()
        )).willReturn(mockUserAdminAgent);

        // 2. getUserList()가 특정 사용자 목록을 반환하도록 설정
        BDDMockito.given(mockUserAdminAgent.getUserList())
                .willReturn(Arrays.asList("user1@other.com", fullUserid, "admin@domain.com"));

        // 3. Mock Pop3Agent 설정 (로그인 검증용)
        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);
        BDDMockito.given(agentFactory.pop3AgentCreate(
                testHost,
                fullUserid,
                testPassword)
        ).willReturn(mockPop3Agent);
        BDDMockito.given(mockPop3Agent.validate()).willReturn(true); // 로그인 성공

        // 4. login.do로 POST 요청 수행
        mockMvc.perform(MockMvcRequestBuilders.post("/login.do")
                .param("menu", String.valueOf(COMMAND_LOGIN))
                .param("userid", userIdWithoutDomain) // 도메인 없는 사용자 ID
                .param("passwd", testPassword)
                .sessionAttr("host", testHost)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"));

        // 5. getDomainCandidates 로직이 잘 동작했는지 검증 (optional, but good practice)
        Mockito.verify(agentFactory).pop3AgentCreate(
                testHost, fullUserid, testPassword
        );
    }

    // 메인 메뉴 
    @Test
    void mainMenuTest() throws Exception {
        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);
        Message mockMessage = Mockito.mock(Message.class);

        BDDMockito.given(agentFactory.pop3AgentCreate(anyString(), anyString(), anyString()))
                .willReturn(mockPop3Agent);

        Mockito.when(mockPop3Agent.getMessages()).thenReturn(new Message[]{mockMessage});

        mockMvc.perform(MockMvcRequestBuilders.get("/main_menu")
                .sessionAttr("host", "test.webmail.com")
                .sessionAttr("userid", "testuser@webmail.com")
                .sessionAttr("password", "testpassword")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("main_menu"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("messageList"));

    }

    @Test
    void searchMailTest() throws Exception {
        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);
        Message mockMessage = Mockito.mock(Message.class);

        BDDMockito.given(agentFactory.pop3AgentCreate(anyString(), anyString(), anyString()))
                .willReturn(mockPop3Agent);

        Mockito.when(mockPop3Agent.getMessages()).thenReturn(new Message[]{mockMessage});

        mockMvc.perform(MockMvcRequestBuilders.post("/search_mail")
                .param("keyword", "검색내용")
                .param("sort", "desc")
                .param("page", "1")
                .sessionAttr("host", "test.webmail.com")
                .sessionAttr("userid", "testuser@webmail.com")
                .sessionAttr("password", "testpassword")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("main_menu"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("messageList"));

    }

    @Test
    void adminMenuTest() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userid", "admin@webmail.com");

        UserAdminAgent mockUserAdminAgent = Mockito.mock(UserAdminAgent.class);

        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).willReturn(mockUserAdminAgent);

        List<String> dummyUserList = java.util.Arrays.asList("user1@test.com", "user2@test.com", "admin@webmail.com");
        BDDMockito.given(mockUserAdminAgent.getUserList()).willReturn(dummyUserList);

        mockMvc.perform(MockMvcRequestBuilders.get("/admin_menu")
                .session(session)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_menu"))
                .andExpect(model().attributeExists("userList"))
                .andExpect(model().attribute("userList", dummyUserList.stream().sorted().toList()));
    }

    @Test
    void adminMenuDeniedTest() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userid", "testUser@webmail.com");

        UserAdminAgent mockUserAdminAgent = Mockito.mock(UserAdminAgent.class);

        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).willReturn(mockUserAdminAgent);

        List<String> dummyUserList = java.util.Arrays.asList("user1@test.com", "user2@test.com", "admin@webmail.com");
        BDDMockito.given(mockUserAdminAgent.getUserList()).willReturn(dummyUserList);

        mockMvc.perform(MockMvcRequestBuilders.get("/admin_menu")
                .session(session)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_fail"));
    }

    // 유저 추가
    // 페이지
    @Test
    void addUserTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/add_user"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_user"));
    }

    // 유저 추가 do
    @Test
    void addUserDoSucessTest() throws Exception {
        String testUserid = "newuser@webmail.com";
        String testPassword = "newuser";

        UserAdminAgent mockAdminAgent = Mockito.mock(UserAdminAgent.class);

        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).willReturn(mockAdminAgent);
        BDDMockito.given(mockAdminAgent.addUser(testUserid, testPassword))
                .willReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/add_user.do")
                .param("id", testUserid)
                .param("password", testPassword)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"))
                .andExpect(flash().attributeExists("msg"))
                .andExpect(flash().attribute("msg", String.format("사용자(%s) 추가를 성공하였습니다.", testUserid)));
    }

    @Test
    void addUserDoFailTest() throws Exception {
        String testUserid = "newuser@test.com";
        String testPassword = "newuser";

        UserAdminAgent mockAdminAgent = Mockito.mock(UserAdminAgent.class);
        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).willReturn(mockAdminAgent);

        BDDMockito.given(mockAdminAgent.addUser(testUserid, testPassword))
                .willReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/add_user.do")
                .param("id", testUserid)
                .param("password", testPassword)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"))
                .andExpect(flash().attributeExists("msg"))
                .andExpect(flash().attribute("msg", String.format("사용자(%s) 추가를 실패하였습니다.", testUserid)));
    }

    // 유저 삭제
    // 페이지
    @Test
    void deleteUserTest() throws Exception {

        UserAdminAgent mockUserAdminAgent = Mockito.mock(UserAdminAgent.class);

        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).willReturn(mockUserAdminAgent);

        List<String> dummyUserList = java.util.Arrays.asList("user1@test.com", "user2@test.com", "admin@webmail.com");
        BDDMockito.given(mockUserAdminAgent.getUserList()).willReturn(dummyUserList);

        mockMvc.perform(MockMvcRequestBuilders.get("/delete_user"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/delete_user"))
                .andExpect(model().attributeExists("userList"))
                .andExpect(model().attribute("userList", dummyUserList.stream().sorted().toList()));
    }

    // 유저 삭제 do
    @Test
    void deleteUserDoTest() throws Exception {
        String[] testUserids = {"deleteuser@webmail.com"};

        UserAdminAgent mockAdminAgent = Mockito.mock(UserAdminAgent.class);
        BDDMockito.given(agentFactory.userAdminAgentCreate(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).willReturn(mockAdminAgent);

        BDDMockito.given(mockAdminAgent.deleteUsers(testUserids))
                .willReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/delete_user.do")
                .param("selectedUsers", testUserids)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // 이미지 업로드
    @Test
    void getImageSuccessTest() throws Exception {
        String fakeImageName = "test.jpg";

        // 가짜 이미지 바이트 데이터
        byte[] fakeImageBytes = "test-image-byte".getBytes();

        BDDMockito.given(imageManager.getImageBytes(anyString(), eq(fakeImageName))).willReturn(fakeImageBytes);

        mockMvc.perform(MockMvcRequestBuilders.get("/get_image/" + fakeImageName))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fakeImageBytes));
    }

}
