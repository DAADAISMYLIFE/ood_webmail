/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.ControllerTest;

import deu.cse.spring_webmail.control.WriteController;
import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.model.SmtpAgent;
import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.service.AddrbookService;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

/**
 *
 * @author qkekd
 */
@WebMvcTest(WriteController.class)
class WriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentFactory agentFactory;

    @MockBean
    private AddrbookService service;

    private final String sessionUid = "userid";

    @Test
    void wirteMailTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/write_mail"))
                .andExpect(status().isOk())
                .andExpect(view().name("write_mail/write_mail"));
    }

    @Test
    void writeMailDoSuccessTest() throws Exception {
        // Mock 파일 생성
        MockMultipartFile file = new MockMultipartFile(
                "file1", "test.txt", "text/plain", "sample content".getBytes());

        String host = "smtp.test.com";
        String userid = "user@test.com";

        SmtpAgent mockSmtpAgent = Mockito.mock(SmtpAgent.class);
        BDDMockito.given(agentFactory.smtpAgentCreate(host, userid))
                .willReturn(mockSmtpAgent);
        BDDMockito.given(mockSmtpAgent.sendMessage()).willReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/write_mail.do")
                .file(file)
                .param("to", "to@example.com")
                .param("cc", "")
                .param("subj", "테스트 제목")
                .param("body", "본문")
                .sessionAttr("host", host)
                .sessionAttr("userid", userid)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"))
                .andExpect(flash().attribute("msg", "메일 전송이 성공했습니다."));
    }

    @Test
    void writeMailDoFailTest() throws Exception {
        //  Mock 파일 생성
        MockMultipartFile file = new MockMultipartFile(
                "file1", "test.txt", "text/plain", "sample content".getBytes());

        String host = "smtp.test.com";
        String userid = "user@test.com";

        SmtpAgent mockSmtpAgent = Mockito.mock(SmtpAgent.class);
        BDDMockito.given(agentFactory.smtpAgentCreate(host, userid))
                .willReturn(mockSmtpAgent);
        BDDMockito.given(mockSmtpAgent.sendMessage()).willReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/write_mail.do")
                .file(file)
                .param("to", "to@example.com")
                .param("cc", "")
                .param("subj", "테스트 제목")
                .param("body", "본문")
                .sessionAttr("host", host)
                .sessionAttr("userid", userid)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"))
                .andExpect(flash().attribute("msg", "메일 전송이 실패했습니다."));
    }

    @Test
    void listAddrbookWithoutKeywordTest() throws Exception {
        String userid = "user@test.com";
        List<Addrbook> dummyList = List.of(
                new Addrbook("friend1@test.com", "친구1", "010-1234-5678"),
                new Addrbook("friend2@test.com", "친구2", "010-8765-4321")
        );

        BDDMockito.given(service.getAddrbookList(userid)).willReturn(dummyList);

        mockMvc.perform(MockMvcRequestBuilders.get("/addrbook/list")
                .sessionAttr(sessionUid, userid)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("addrbook/list"))
                .andExpect(model().attribute("addrbookList", dummyList));
    }

    @Test
    void listAddrbookWithKeywordTest() throws Exception {
        String userid = "user@test.com";
        String keyword = "친구";
        List<Addrbook> dummyList = List.of(
                new Addrbook("friend1@test.com", "친구1", "010-1234-5678")
        );

        BDDMockito.given(service.searchAddrbookList(userid, keyword)).willReturn(dummyList);

        mockMvc.perform(MockMvcRequestBuilders.get("/addrbook/list")
                .param("keyword", keyword)
                .sessionAttr(sessionUid, userid)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("addrbook/list"))
                .andExpect(model().attribute("addrbookList", dummyList))
                .andExpect(model().attribute("keyword", keyword));
    }

    @Test
    void showAdduserPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/addrbook/adduser"))
                .andExpect(status().isOk())
                .andExpect(view().name("addrbook/adduser"));
    }

    @Test
    void adduserAddrbookSuccessTest() throws Exception {
        String userid = "user@test.com";

        BDDMockito.given(service.registerAddrbook(userid, "email@test.com", "홍길동", "010-0000-0000"))
                .willReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/addrbook/adduser")
                .param("email", "email@test.com")
                .param("name", "홍길동")
                .param("phone", "010-0000-0000")
                .sessionAttr(sessionUid, userid)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/addrbook/list"));
    }

    @Test
    void adduserAddrbookFailTest() throws Exception {
        String userid = "user@test.com";

        BDDMockito.given(service.registerAddrbook(userid, "email@test.com", "홍길동", "010-0000-0000"))
                .willReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/addrbook/adduser")
                .param("email", "email@test.com")
                .param("name", "홍길동")
                .param("phone", "010-0000-0000")
                .sessionAttr(sessionUid, userid)
        )
                .andExpect(status().isOk()) // redirect 안하고 그대로 에러 페이지 보여줄 수도 있음
                .andExpect(view().name("addrbook/error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void adduserAddrbookMissingFieldsTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/addrbook/adduser")
                .param("email", "")
                .param("name", "")
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAddrbookSuccessTest() throws Exception {
        String userid = "user@test.com";
        String email = "friend@test.com";

        BDDMockito.given(service.deleteAddrbook(userid, email)).willReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/addrbook/delete")
                .param("email", email)
                .sessionAttr(sessionUid, userid)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/addrbook/list"));
    }

    @Test
    void deleteAddrbookFailTest() throws Exception {
        String userid = "user@test.com";
        String email = "friend@test.com";

        BDDMockito.given(service.deleteAddrbook(userid, email)).willReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/addrbook/delete")
                .param("email", email)
                .sessionAttr(sessionUid, userid)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("addrbook/error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

}
