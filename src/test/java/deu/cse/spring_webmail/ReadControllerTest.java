/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail;

import deu.cse.spring_webmail.control.ReadController;
import deu.cse.spring_webmail.model.AgentFactory;
import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.mail.Message;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.Matchers.containsString;

/**
 *
 * @author qkekd
 */
@WebMvcTest(ReadController.class)
class ReadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentFactory agentFactory;

    @MockBean
    ServletContext ctx;

    @TempDir
    File tempDir;

    @Test
    void showMessageTest() throws Exception {
        String testMessageId = "test-ID";
        String testHost = "test@webmail.com";
        String testUserid = "admin@webmail.com";
        String testPassword = "admin_password";

        Pop3Agent mockPop3Agent = Mockito.mock(Pop3Agent.class);
        Message mockMessage = Mockito.mock(Message.class);

        // 1. getHeader로 message ID 반환
        Mockito.when(mockMessage.getHeader("Message-ID")).thenReturn(new String[]{"<test-ID>"});

        // 2. getMessages에서 메일 목록 리턴
        Mockito.when(mockPop3Agent.getMessages()).thenReturn(new Message[]{mockMessage});

        // agentFactory 클래스의 pop3AgentCreate 메서드가 실행될 경우 mockPop3Agent를 리턴함
        BDDMockito.given(agentFactory.pop3AgentCreate(
                testHost,
                testUserid,
                testPassword)
        ).willReturn(mockPop3Agent);

        mockMvc.perform(MockMvcRequestBuilders.get("/show_message")
                .param("id", testMessageId)
                .sessionAttr("host", testHost)
                .sessionAttr("userid", testUserid)
                .sessionAttr("password", testPassword)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("/read_mail/show_message"))
                .andExpect(model().attributeExists("msg"));
    }

    @Test
    void downloadTest() throws Exception {
        String testUserid = "admin@webmail.com";
        String testFileName = "file_name.txt";
        String testFileContent = "test content";

        // 실제 파일 생성 (테스트용)
        File userDir = new File(tempDir, testUserid);
        userDir.mkdirs();
        File testFile = new File(userDir, testFileName);
        Files.write(testFile.toPath(), testFileContent.getBytes());

        Mockito.when(ctx.getRealPath(anyString()))
                .thenReturn(tempDir.getAbsolutePath());

        mockMvc.perform(MockMvcRequestBuilders.get("/download")
                .param("userid", testUserid)
                .param("filename", testFileName))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(testFileName)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain")) // Files.probeContentType 결과
                .andExpect(content().string(testFileContent));

    }

    @Test
    void deleteMailDoSuccessTest() throws Exception {
        String testHost = "mail.test.com";
        String testUser = "user@test.com";
        String testPass = "password";

        Message mockMessage = Mockito.mock(Message.class);
        Mockito.when(mockMessage.getHeader("Message-ID")).thenReturn(new String[]{"<test-message-id>"});

        Pop3Agent mockAgent = Mockito.mock(Pop3Agent.class);
        Mockito.when(mockAgent.getMessages()).thenReturn(new Message[]{mockMessage});
        Mockito.when(mockAgent.deleteMessage(1, true)).thenReturn(true);

        Mockito.when(agentFactory.pop3AgentCreate(testHost, testUser, testPass)).thenReturn(mockAgent);

        mockMvc.perform(MockMvcRequestBuilders.get("/delete_mail.do")
                .param("messageId", "<test-message-id>")
                .sessionAttr("host", testHost)
                .sessionAttr("userid", testUser)
                .sessionAttr("password", testPass))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main_menu"))
                .andExpect(flash().attribute("msg", "메시지 삭제를 성공하였습니다."));
    }

    @Test
    void deleteMailDoFailTest() throws Exception {
        String testHost = "mail.test.com";
        String testUser = "user@test.com";
        String testPass = "password";

        Message mockMessage = Mockito.mock(Message.class);
        Mockito.when(mockMessage.getHeader("Message-ID")).thenReturn(new String[]{"<other-id>"});

        Pop3Agent mockAgent = Mockito.mock(Pop3Agent.class);
        Mockito.when(mockAgent.getMessages()).thenReturn(new Message[]{mockMessage});

        Mockito.when(agentFactory.pop3AgentCreate(testHost, testUser, testPass)).thenReturn(mockAgent);

        mockMvc.perform(MockMvcRequestBuilders.get("/delete_mail.do")
                .param("messageId", "<not-found-id>")
                .sessionAttr("host", testHost)
                .sessionAttr("userid", testUser)
                .sessionAttr("password", testPass))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main_menu"))
                .andExpect(flash().attribute("msg", "해당 메시지를 찾을 수 없습니다."));
    }
}
