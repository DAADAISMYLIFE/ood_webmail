/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

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

public class UserAdminAgent {

    private static final Logger log = LoggerFactory.getLogger(UserAdminAgent.class);

    private String server;
    private int port;
    private String ROOT_ID;
    private String ROOT_PASSWORD;
    private String ADMIN_ID;
    private final String EOL = "\r\n";
    private String cwd;
    private boolean isConnected = false;

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
        isConnected = connect(); // JMX 연결 초기화
    }

    // 공통 JMX 연결 메서드
    private MBeanServerConnection getJMXConnection() throws Exception {
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://" + server + ":" + port + "/jmxrmi";
        log.debug("Connecting to JMX URL: {}", jmxUrl);
        JMXServiceURL serviceUrl = new JMXServiceURL(jmxUrl);

        // JMX 관리자 id와 pw 맵핑
        Map<String, Object> env = new HashMap<>();
        env.put(JMXConnector.CREDENTIALS, new String[]{ROOT_ID, ROOT_PASSWORD});
        log.debug("Using credentials: user={}", ROOT_ID);

        // url과 관리자 정보로 JMX 연결
        JMXConnector connector = JMXConnectorFactory.connect(serviceUrl, env);
        return connector.getMBeanServerConnection();
    }

    // MBean 객체 생성
    private ObjectName getMBeanObject() throws Exception {
        ObjectName mbeanName = new ObjectName("org.apache.james:type=component,name=usersrepository");
        return mbeanName;
    }

    public boolean addUser(String userId, String password) {
        boolean status = false;

        log.debug("addUser() called with userId: {}, password: {}", userId, password);

        try {
            MBeanServerConnection connection = getJMXConnection();
            ObjectName mbeanName = getMBeanObject();
            connection.invoke(mbeanName, "addUser",
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
            MBeanServerConnection connection = getJMXConnection();
            ObjectName mbeanName = getMBeanObject();

            // listUsers 메서드 호출 (String[] 반환)
            String[] users = (String[]) connection.invoke(mbeanName, "listAllUsers",
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

    public boolean deleteUsers(String[] userList) {
        boolean status = true; // 모든 삭제 성공 시 true, 하나라도 실패 시 false

        if (!isConnected) {
            log.warn("Not connected to JMX server");
            return false;
        }

        try {
            MBeanServerConnection connection = getJMXConnection();
            ObjectName mbeanName = getMBeanObject();

            for (String userId : userList) {
                try {
                    connection.invoke(mbeanName, "deleteUser",
                            new Object[]{userId},
                            new String[]{String.class.getName()});
                    log.debug("User {} deleted successfully", userId);
                } catch (Exception ex) {
                    log.error("Failed to delete user {}: {}", userId, ex.getMessage(), ex);
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
            MBeanServerConnection connection = getJMXConnection();
            ObjectName mbeanName = getMBeanObject();

            // containsUser 메서드 호출
            Boolean exists = (Boolean) connection.invoke(mbeanName, "verifyExists",
                    new Object[]{userId},
                    new String[]{String.class.getName()});

            status = exists != null && exists;
            log.debug("User {} exists: {}", userId, status);
        } catch (Exception ex) {
            log.error("verify 예외: {}", ex.getMessage(), ex);
        }

        return status;
    }

    private boolean connect() {
        log.info("connect() called: root.id = {}, root.password = {}", ROOT_ID, ROOT_PASSWORD);

        try {
            // JMX 연결 테스트
            MBeanServerConnection connection = getJMXConnection();
            connection.getMBeanCount();
            log.debug("JMX connection established");
            return true;
        } catch (Exception ex) {
            log.error("connect 예외: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
