package com.thiagsilvadev.erpapi.user.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@SpringBootTest
@ActiveProfiles("dbtest")
class UserEmailUniquenessPersistenceTest {

    private static final String EMAIL = "unique-user@acme.example";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void deleteSeededUsers() {
        userRepository.findByContactEmailValue(EMAIL).ifPresent(userRepository::delete);
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        persist(EMAIL);

        assertThatThrownBy(() -> persist(EMAIL)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldTranslateConstraintViolationWhenTheTransactionIsRolledBack() {
        persist(EMAIL);

        assertThatThrownBy(() -> inTransaction(() -> {
                    try {
                        userRepository.saveAndFlush(
                                User.create("Raced", "User", Contact.create(EMAIL, "11912345678"), null));
                    } catch (DataIntegrityViolationException exception) {
                        throw new EmailAlreadyExistsException(EMAIL);
                    }
                }))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void shouldRollBackTheTransactionWhenTheConstraintViolationIsSwallowed() {
        persist(EMAIL);

        assertThatThrownBy(() -> inTransaction(() -> {
                    try {
                        userRepository.saveAndFlush(
                                User.create("Raced", "User", Contact.create(EMAIL, "11912345678"), null));
                    } catch (DataIntegrityViolationException exception) {
                        // Swallowing poisons the transaction: the commit fails with UnexpectedRollbackException.
                    }
                }))
                .isInstanceOf(UnexpectedRollbackException.class);
        assertThat(userRepository.count()).isOne();
    }

    private void persist(String email) {
        inTransaction(() ->
                userRepository.saveAndFlush(User.create("Unique", "User", Contact.create(email, "11912345678"), null)));
    }

    private void inTransaction(Runnable action) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
    }
}
