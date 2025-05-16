/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail;

import deu.cse.spring_webmail.control.WriteController;
import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.model.SmtpAgent;

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

}
