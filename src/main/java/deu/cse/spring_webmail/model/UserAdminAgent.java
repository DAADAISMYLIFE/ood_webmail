/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.management.MalformedObjectNameException;

public class UserAdminAgent {

    private static final Logger log = LoggerFactory.getLogger(UserAdminAgent.class);

    private String server;
    private int port;
    private String ROOT_ID;
    private String ROOT_PASSWORD;
    private String ADMIN_ID;
    private String cwd;
    private boolean isConnected = false;
    private JMXConnector jmxConnector;
    private MBeanServerConnection jmxConnection;

    public UserAdminAgent() {
    }

    public UserAdminAgent(String server, int port, String cwd,
            String root_id, String root_pass, String admin_id) {
        log.debug("UserAdminAgent created: server = {}, port = {}", server, port);
        this.server = server;  // 예: localhost
        this.port = port;      // 예: 9999 (JMX 포트)
        this.cwd = cwd;
        this.ROOT_ID = root_id;
        this.ROOT_PASSWORD = root_pass;
        this.ADMIN_ID = admin_id;

        log.debug("isConnected = {}, root.id = {}", isConnected, ROOT_ID);

        try {
            String url = "service:jmx:rmi:///jndi/rmi://" + server + ":" + port + "/jmxrmi";
            JMXServiceURL serviceUrl = new JMXServiceURL(url);
            Map<String, Object> env = new HashMap<>();
            env.put(JMXConnector.CREDENTIALS, new String[]{ROOT_ID, ROOT_PASSWORD});

            // 커넥터 열기
            this.jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
            this.jmxConnection = jmxConnector.getMBeanServerConnection();
            this.isConnected = true;
            log.debug("JMX connection created");
        } catch (Exception ex) {
            log.error("Failed to establish JMX connection: {}", ex.getMessage(), ex);
            this.isConnected = false;
        }
    }

    // MBean 객체 생성
    private ObjectName getMBeanObject() throws MalformedObjectNameException {
        return new ObjectName("org.apache.james:type=component,name=usersrepository");
    }

    public boolean addUser(String userId, String password) {
        boolean status = false;

        log.debug("addUser() called with userId: {}, password: {}", userId, password);

        try {
            ObjectName mbeanName = getMBeanObject();
            jmxConnection.invoke(mbeanName, "addUser",
                    new Object[]{userId, password},
                    new String[]{String.class.getName(), String.class.getName()});

            status = true;
            log.debug("User {} added successfully", userId);

        } catch (Exception ex) {
            log.error("addUser 예외: {}", ex.getMessage(), ex);
            status = false;
        }

        return status;
    }

    public List<String> getUserList() {
        List<String> userList = new LinkedList<>();

        log.info("getUserList() called, root.id = {}, root.password = {}", ROOT_ID, ROOT_PASSWORD);

        if (!isConnected) {
            log.warn("Not connected to JMX server");
            return userList;
        }

        try {
            ObjectName mbeanName = getMBeanObject();

            // listUsers 메서드 호출 (String[] 반환)
            String[] users = (String[]) jmxConnection.invoke(mbeanName, "listAllUsers",
                    new Object[]{},
                    new String[]{});

            // String[]를 List<String>으로 변환, ADMIN_ID 제외
            for (String user : users) {
                if (!user.equals(ADMIN_ID)) {
                    userList.add(user);
                }
            }

            log.debug("Retrieved user list: {}", userList);
        } catch (Exception ex) {
            log.error("getUserList 예외: {}", ex.getMessage(), ex);
        }

        return userList;
    }

    private boolean invokeDeleteUserOperation(MBeanServerConnection jmxConnection, ObjectName mbeanName, String userId) {
        try {
            jmxConnection.invoke(mbeanName, "deleteUser",
                    new Object[]{userId},
                    new String[]{String.class.getName()});
            log.debug("User {} deleted successfully", userId);
            return true;
        } catch (Exception ex) {
            log.error("Failed to delete user {}: {}", userId, ex.getMessage(), ex);
            return false;
        }
    }

    public boolean deleteUsers(String[] userList) {
        boolean status = true;

        if (!isConnected) {
            log.warn("Not connected to JMX server");
            return false;
        }

        try {
            ObjectName mbeanName = getMBeanObject();

            for (String userId : userList) {
                boolean userDeletionSuccess = invokeDeleteUserOperation(jmxConnection, mbeanName, userId);

                if (!userDeletionSuccess) {
                    status = false;
                }
            }
        } catch (Exception ex) {
            log.error("deleteUsers 예외: {}", ex.getMessage(), ex);
            status = false;
        }

        return status;
    }

    public boolean verify(String userId) {
        boolean status = false;

        log.debug("verify() called with userId: {}", userId);

        try {
            ObjectName mbeanName = getMBeanObject();

            // containsUser 메서드 호출
            Boolean exists = (Boolean) jmxConnection.invoke(mbeanName, "verifyExists",
                    new Object[]{userId},
                    new String[]{String.class.getName()});

            status = exists != null && exists;
            log.debug("User {} exists: {}", userId, status);
        } catch (Exception ex) {
            log.error("verify 예외: {}", ex.getMessage(), ex);
        }

        return status;
    }

    public void close() {
        try {
            if (jmxConnector != null) {
                jmxConnector.close();
                log.debug("JMX connection closed");
            }
        } catch (IOException ex) {
            log.error("Error closing JMX connector: {}", ex.getMessage(), ex);
        }
    }

}
