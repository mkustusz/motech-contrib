package org.motechproject.mobileforms.validator;

import org.junit.Test;
import org.motechproject.mobileforms.domain.FormError;
import org.motechproject.mobileforms.validator.impl.RequiredValidator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RequiredValidatorTest {
    @Test
    public void shouldReturnNullIfFieldHasANonNullValue() {
        assertThat(new RequiredValidator().validate("some value", "name", String.class, null), is(equalTo(null)));
    }

    @Test
    public void shouldReturnFieldErrorIfANullValueIsPassed() {
        assertThat(new RequiredValidator().validate(null, "name", String.class, null), is(equalTo(new FormError("name", "is mandatory"))));
    }

    @Test
    public void shouldReturnFieldErrorIfAEmptyStringIsPassed() {
        assertThat(new RequiredValidator().validate("", "name", String.class, null), is(equalTo(new FormError("name", "is mandatory"))));
    }
}
