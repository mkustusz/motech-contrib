package org.motechproject.commcare.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.commcare.domain.CommcareUser;
import org.motechproject.commcare.client.CommCareAPIHttpClient;

import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CommcareUserServiceImplTest {

    private CommcareUserServiceImpl userService;

    @Mock
    private CommCareAPIHttpClient commcareHttpClient;

    @Before
    public void setUp() {
        initMocks(this);
        userService = new CommcareUserServiceImpl(commcareHttpClient);
    }

    @Test
    public void testAllUsers() {
        when(commcareHttpClient.usersRequest()).thenReturn(usersResponse());

        List<CommcareUser> users = userService.getAllUsers();

        assertEquals(asList("3F2504E04F8911D39A0C0305E82C3301", "5d622c4336d118a9020d1c758e71de51",
                "5d622c4336d118a9020d1c758e71f368", "5d622c4336d118a9020d1c758e71ea2f"),
                extract(users, on(CommcareUser.class).getId()));
    }

    @Test
    public void testGetUserWhenUserExists() {
        String userId = "5d622c4336d118a9020d1c758e71de51";

        when(commcareHttpClient.usersRequest()).thenReturn(usersResponse());

        CommcareUser user = userService.getCommcareUserById(userId);

        assertNotNull(user);
    }

    @Test
    public void testGetUserWhenUserDoesNotExist() {
        String userId = "badId";

        when(commcareHttpClient.usersRequest()).thenReturn(usersResponse());

        CommcareUser user = userService.getCommcareUserById(userId);

        assertNull(user);
    }

    private String usersResponse() {
        return "{\"meta\": {\"limit\": 20, \"next\": null, \"offset\": 0, \"previous\": null, \"total_count\": 4}, \"objects\": [{\"default_phone_number\": \"2074503879\", \"email\": \"\", \"first_name\": \"\", \"groups\": [], \"id\": \"3F2504E04F8911D39A0C0305E82C3301\", \"last_name\": \"\", \"phone_numbers\": [\"2074503879\"], \"resource_uri\": \"\", \"user_data\": {\"chw_id\": \"13/43/DFA\"}, \"username\": \"ctsims@usm-motech.commcarehq.org\"}, {\"default_phone_number\": null, \"email\": \"\", \"first_name\": \"\", \"groups\": [], \"id\": \"5d622c4336d118a9020d1c758e71de51\", \"last_name\": \"\", \"phone_numbers\": [], \"resource_uri\": \"\", \"user_data\": {}, \"username\": \"demo_user@usm-motech.commcarehq.org\"}, {\"default_phone_number\": null, \"email\": \"\", \"first_name\": \"\", \"groups\": [], \"id\": \"5d622c4336d118a9020d1c758e71f368\", \"last_name\": \"\", \"phone_numbers\": [], \"resource_uri\": \"\", \"user_data\": {}, \"username\": \"russell@usm-motech.commcarehq.org\"}, {\"default_phone_number\": null, \"email\": \"\", \"first_name\": \"\", \"groups\": [], \"id\": \"5d622c4336d118a9020d1c758e71ea2f\", \"last_name\": \"\", \"phone_numbers\": [], \"resource_uri\": \"\", \"user_data\": {}, \"username\": \"russell2@usm-motech.commcarehq.org\"}]}";
    }
}
