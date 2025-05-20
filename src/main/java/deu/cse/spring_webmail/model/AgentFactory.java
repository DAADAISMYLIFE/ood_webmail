/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import org.springframework.stereotype.Component;

/**
 *
 * @author qkekd
 */
@Component
public class AgentFactory {
    
    public Pop3Agent pop3AgentCreate(String host, String userid, String password) {
        return new Pop3Agent(host, userid, password);
    }

    public UserAdminAgent userAdminAgentCreate(String server, int port, String cwd,
            String rootId, String rootPass, String adminId) {
        return new UserAdminAgent(server, port, cwd, rootId, rootPass, adminId);
    }
    
     public SmtpAgent smtpAgentCreate(String host, String userid) {
        return new SmtpAgent(host, userid);
    }
}
