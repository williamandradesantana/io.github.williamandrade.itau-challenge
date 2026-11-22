package io.github.williamandradesantana.itau_challenge.interfaces.auth;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.interfaces.ApiIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends ApiIntegrationTest {

    private static final String USERS = "/api/users/v1";
    private static final String ME = USERS + "/me";
    private static final String AUTHORIZATION = "Authorization";

    // ============================ GET /me ============================

    @Test
    @DisplayName("Test: me should return the data of the authenticated common user without the password")
    void test_MeShouldReturnTheDataOfTheAuthenticatedCommonUserWithoutThePassword() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(accessTokenOf(userName))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId().toString()))
            .andExpect(jsonPath("$.userName").value(userName))
            .andExpect(jsonPath("$.fullName").value("Full " + userName))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.roles[0]").value("COMMON"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Test: me should also work for an admin")
    void test_MeShouldAlsoWorkForAnAdmin() throws Exception {
        String userName = uniqueName("boss");
        createUser(userName, "ADMIN");

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(accessTokenOf(userName))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("Test: me without a token should answer 401")
    void test_MeWithoutAToken_ShouldAnswer401() throws Exception {
        mockMvc.perform(get(ME)).andExpect(status().isUnauthorized());
    }

    // ============================ GET /api/users/v1 ============================

    @Test
    @DisplayName("Test: list as admin should answer 200 with the page structure and without passwords")
    void test_ListAsAdmin_ShouldAnswer200WithThePageStructureAndWithoutPasswords() throws Exception {
        mockMvc.perform(get(USERS).header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].userName").isNotEmpty())
            .andExpect(jsonPath("$.content[0].password").doesNotExist())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").isNumber())
            .andExpect(jsonPath("$.totalPages").isNumber())
            .andExpect(jsonPath("$.hasNext").isBoolean());
    }

    @Test
    @DisplayName("Test: list should include the created users in order of user name")
    void test_ListShouldIncludeTheCreatedUsersInOrderOfUserName() throws Exception {
        String ccc = uniqueName("ccc");
        String aaa = uniqueName("aaa");
        String bbb = uniqueName("bbb");
        createUser(ccc, "COMMON");
        createUser(aaa, "COMMON");
        createUser(bbb, "COMMON");

        String body = mockMvc.perform(get(USERS + "?size=100").header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<String> names = JsonPath.read(body, "$.content[*].userName");
        assertTrue(names.containsAll(List.of(aaa, bbb, ccc)), () -> "users returned: " + names);
        assertTrue(names.indexOf(aaa) < names.indexOf(bbb) && names.indexOf(bbb) < names.indexOf(ccc),
            () -> "users must come ordered by user name: " + names);
    }

    @Test
    @DisplayName("Test: list should paginate")
    void test_ListShouldPaginate() throws Exception {
        for (int i = 0; i < 3; i++) createUser(uniqueName("user" + i), "COMMON");
        String token = adminToken();

        mockMvc.perform(get(USERS + "?page=0&size=2").header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("Test: a page beyond the last one should come empty")
    void test_APageBeyondTheLastOneShouldComeEmpty() throws Exception {
        mockMvc.perform(get(USERS + "?page=500&size=10").header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("Test: list with invalid pagination should answer 400")
    void test_ListWithInvalidPagination_ShouldAnswer400() throws Exception {
        String token = adminToken();

        for (String query : new String[]{"?size=0", "?size=101", "?size=-1", "?page=-1"}) {
            mockMvc.perform(get(USERS + query).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Test: list with a non numeric parameter should answer 400 naming the parameter")
    void test_ListWithANonNumericParameter_ShouldAnswer400NamingTheParameter() throws Exception {
        String token = adminToken();

        ResultActions size = mockMvc.perform(get(USERS + "?size=abc").header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isBadRequest());
        ResultActions page = mockMvc.perform(get(USERS + "?page=x").header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isBadRequest());

        assertEquals("Invalid value for parameter 'size'", messageOf(size));
        assertEquals("Invalid value for parameter 'page'", messageOf(page));
    }

    @Test
    @DisplayName("Test: list as a common user should answer 403 with a json body")
    void test_ListAsACommonUser_ShouldAnswer403WithAJsonBody() throws Exception {
        ResultActions result = mockMvc.perform(get(USERS).header(AUTHORIZATION, bearer(commonToken())))
            .andExpect(status().isForbidden());

        assertEquals("Access denied", messageOf(result));
    }

    @Test
    @DisplayName("Test: list without a token should answer 401")
    void test_ListWithoutAToken_ShouldAnswer401() throws Exception {
        mockMvc.perform(get(USERS)).andExpect(status().isUnauthorized());
    }

    // ============================ GET /api/users/v1/{id} ============================

    @Test
    @DisplayName("Test: get one as admin should return the user")
    void test_GetOneAsAdmin_ShouldReturnTheUser() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");

        mockMvc.perform(get(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId().toString()))
            .andExpect(jsonPath("$.userName").value(userName))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.roles[0]").value("COMMON"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Test: get one should also return a disabled user, marked as disabled")
    void test_GetOneShouldAlsoReturnADisabledUser() throws Exception {
        User user = createUser(uniqueName("off"), false, "COMMON");

        mockMvc.perform(get(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("Test: get one of an unknown id should answer 404")
    void test_GetOneOfAnUnknownId_ShouldAnswer404() throws Exception {
        ResultActions result = mockMvc.perform(get(USERS + "/" + java.util.UUID.randomUUID()).header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isNotFound());

        assertTrue(messageOf(result).contains("User not found"));
    }

    @Test
    @DisplayName("Test: get one with an id that is not a uuid should answer 400")
    void test_GetOneWithAnIdThatIsNotAUuid_ShouldAnswer400() throws Exception {
        ResultActions result = mockMvc.perform(get(USERS + "/not-a-uuid").header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isBadRequest());

        assertEquals("Invalid value for parameter 'id'", messageOf(result));
    }

    @Test
    @DisplayName("Test: get one as a common user or without a token should answer 403 and 401")
    void test_GetOneAsACommonUserOrWithoutAToken_ShouldAnswer403And401() throws Exception {
        User user = createUser(uniqueName("bob"), "COMMON");

        mockMvc.perform(get(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(commonToken())))
            .andExpect(status().isForbidden());
        mockMvc.perform(get(USERS + "/" + user.getId())).andExpect(status().isUnauthorized());
    }

    // ============================ PATCH /api/users/v1/{id} (disable) ============================

    @Test
    @DisplayName("Test: disable as admin should answer 200, mark the user as disabled and block the login")
    void test_DisableAsAdmin_ShouldAnswer200MarkTheUserAsDisabledAndBlockTheLogin() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");
        String token = adminToken();
        login(userName, PASSWORD).andExpect(status().isOk());

        mockMvc.perform(patch(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        mockMvc.perform(get(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));
        login(userName, PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: disable should be idempotent and affect only the given user")
    void test_DisableShouldBeIdempotentAndAffectOnlyTheGivenUser() throws Exception {
        User target = createUser(uniqueName("target"), "COMMON");
        User other = createUser(uniqueName("other"), "COMMON");
        String token = adminToken();

        mockMvc.perform(patch(USERS + "/" + target.getId()).header(AUTHORIZATION, bearer(token))).andExpect(status().isOk());
        mockMvc.perform(patch(USERS + "/" + target.getId()).header(AUTHORIZATION, bearer(token))).andExpect(status().isOk());

        mockMvc.perform(get(USERS + "/" + other.getId()).header(AUTHORIZATION, bearer(token)))
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Test: disable of an unknown id should answer 404")
    void test_DisableOfAnUnknownId_ShouldAnswer404() throws Exception {
        mockMvc.perform(patch(USERS + "/" + java.util.UUID.randomUUID()).header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test: disable as a common user or without a token should answer 403 and 401")
    void test_DisableAsACommonUserOrWithoutAToken_ShouldAnswer403And401() throws Exception {
        User user = createUser(uniqueName("bob"), "COMMON");

        mockMvc.perform(patch(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(commonToken())))
            .andExpect(status().isForbidden());
        mockMvc.perform(patch(USERS + "/" + user.getId())).andExpect(status().isUnauthorized());
        assertTrue(userRepository.findById(user.getId()).orElseThrow().isEnabled(), () -> "a denied request must not change the user");
    }

    // ============================ DELETE /api/users/v1/{id} ============================

    @Test
    @DisplayName("Test: delete as admin should answer 204, remove the user and block the login")
    void test_DeleteAsAdmin_ShouldAnswer204RemoveTheUserAndBlockTheLogin() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");
        String token = adminToken();

        mockMvc.perform(delete(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        mockMvc.perform(get(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(token)))
            .andExpect(status().isNotFound());
        login(userName, PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: delete should not affect other users")
    void test_DeleteShouldNotAffectOtherUsers() throws Exception {
        User target = createUser(uniqueName("target"), "COMMON");
        User other = createUser(uniqueName("other"), "COMMON");
        String token = adminToken();

        mockMvc.perform(delete(USERS + "/" + target.getId()).header(AUTHORIZATION, bearer(token))).andExpect(status().isNoContent());

        mockMvc.perform(get(USERS + "/" + other.getId()).header(AUTHORIZATION, bearer(token))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test: delete of an unknown id should answer 404")
    void test_DeleteOfAnUnknownId_ShouldAnswer404() throws Exception {
        mockMvc.perform(delete(USERS + "/" + java.util.UUID.randomUUID()).header(AUTHORIZATION, bearer(adminToken())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test: delete as a common user or without a token should answer 403 and 401")
    void test_DeleteAsACommonUserOrWithoutAToken_ShouldAnswer403And401() throws Exception {
        User user = createUser(uniqueName("bob"), "COMMON");

        mockMvc.perform(delete(USERS + "/" + user.getId()).header(AUTHORIZATION, bearer(commonToken())))
            .andExpect(status().isForbidden());
        mockMvc.perform(delete(USERS + "/" + user.getId())).andExpect(status().isUnauthorized());
        assertTrue(userRepository.findById(user.getId()).isPresent(), () -> "a denied request must not delete the user");
    }
}
