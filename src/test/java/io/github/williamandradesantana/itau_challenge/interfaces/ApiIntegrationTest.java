package io.github.williamandradesantana.itau_challenge.interfaces;

import com.jayway.jsonpath.JsonPath;
import io.github.williamandradesantana.itau_challenge.application.auth.IPasswordHasher;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.PostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
public abstract class ApiIntegrationTest extends PostgresIntegrationTest {

    protected static final String PASSWORD = "Senha@123";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @Autowired
    protected IPasswordHasher passwordHasher;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected MockMvc mockMvc;

    @BeforeEach
    protected void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @AfterEach
    protected void removeUsersCreatedByTheTest() {
        jdbcTemplate.update("delete from tb_user_permissions where user_id in (select id from tb_users where user_name not in ('admin', 'user'))");
        jdbcTemplate.update("delete from tb_users where user_name not in ('admin', 'user')");
    }


    protected String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected User createUser(String userName, String... permissionNames) {
        return createUser(userName, true, permissionNames);
    }

    protected User createUser(String userName, boolean enabled, String... permissionNames) {
        Set<Permission> permissions = Arrays.stream(permissionNames)
                .map(name -> permissionRepository.findByPermissionName(name).orElseThrow())
                .collect(Collectors.toSet());

        User user = User.create(userName, "Full " + userName, passwordHasher.hash(PASSWORD), permissions);
        user.setEnabled(enabled);
        userRepository.save(user);
        return user;
    }


    protected ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    protected ResultActions register(String userName, String fullName, String password) throws Exception {
        return postJson("/auth/register", """
                {"userName":"%s","fullName":"%s","password":"%s"}""".formatted(userName, fullName, password));
    }

    protected ResultActions login(String userName, String password) throws Exception {
        return postJson("/auth/login", """
                {"userName":"%s","password":"%s"}""".formatted(userName, password));
    }

    protected ResultActions refresh(String refreshToken) throws Exception {
        return postJson("/auth/refresh", """
                {"refreshToken":"%s"}""".formatted(refreshToken));
    }


    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String loginBody(String userName) throws Exception {
        return login(userName, PASSWORD).andReturn().getResponse().getContentAsString();
    }

    protected String accessTokenOf(String userName) throws Exception {
        return JsonPath.read(loginBody(userName), "$.accessToken");
    }

    protected String refreshTokenOf(String userName) throws Exception {
        return JsonPath.read(loginBody(userName), "$.refreshToken");
    }

    protected String adminToken() throws Exception {
        String userName = uniqueName("admin");
        createUser(userName, "ADMIN");
        return accessTokenOf(userName);
    }

    protected String commonToken() throws Exception {
        String userName = uniqueName("common");
        createUser(userName, "COMMON");
        return accessTokenOf(userName);
    }

    protected String messageOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.message");
    }

}
