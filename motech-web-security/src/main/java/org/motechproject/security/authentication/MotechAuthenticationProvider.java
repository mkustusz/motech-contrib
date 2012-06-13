package org.motechproject.security.authentication;

import org.apache.commons.lang.StringUtils;
import org.motechproject.security.domain.MotechWebUser;
import org.motechproject.security.repository.AllMotechWebUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class MotechAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    public static final String PLEASE_ENTER_PASSWORD = "Please enter password.";
    public static final String USER_NOT_FOUND = "The username or password you entered is incorrect. Please enter the correct credentials.";
    public static final String USER_NOT_ACTIVATED = "The user has been registered but not activated. Please contact your local administrator.";

    private AllMotechWebUsers allMotechWebUsers;

    @Autowired
    public MotechAuthenticationProvider(AllMotechWebUsers allMotechWebUsers) {
        this.allMotechWebUsers = allMotechWebUsers;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        String password = (String) authentication.getCredentials();
        if (StringUtils.isEmpty(password)) {
            throw new BadCredentialsException(PLEASE_ENTER_PASSWORD);
        }
        if (!password.equals(userDetails.getPassword())) {
            throw new BadCredentialsException(USER_NOT_FOUND);
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        MotechWebUser webUser = allMotechWebUsers.findByUserName(username);
        if (webUser == null) {
            throw new BadCredentialsException(USER_NOT_FOUND);
        } else if (!webUser.isActive()) {
            throw new BadCredentialsException(USER_NOT_ACTIVATED);
        } else {
            authentication.setDetails(webUser);
            return new User(webUser.getUserName(), webUser.getPassword(), webUser.isActive(), true, true, true, webUser.getAuthorities());
        }
    }
}
