package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;
import com.thiagsilvadev.erpapi.user.api.UserRole;

@ExtendWith(MockitoExtension.class)
class IamUserDetailsServiceTest {

    @Mock
    private IamCredentialRepository credentialRepository;

    private IamUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new IamUserDetailsService(credentialRepository, new RolePermissionResolver());
    }

    @Test
    void shouldLoadCredentialByNormalizedEmail() {
        UUID userId = UUID.randomUUID();
        IamCredential credential = IamCredential.create(userId, "user@example.com", "hash", UserRole.ADMIN);
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));

        IamUserDetails userDetails = (IamUserDetails) userDetailsService.loadUserByUsername(" User@Example.com ");

        assertThat(userDetails.getUserId()).isEqualTo(userId);
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getEmail()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hash");
        assertThat(userDetails.getRole()).isEqualTo("ADMIN");
        assertThat(userDetails.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("ROLE_ADMIN")
                .containsAll(CatalogPermissions.ALL)
                .containsAll(CompanyPermissions.ALL)
                .containsAll(CustomerPermissions.ALL)
                .containsAll(FinancialPermissions.ALL)
                .containsAll(InventoryPermissions.ALL)
                .containsAll(SalesPermissions.ALL)
                .containsAll(UserPermissions.ALL)
                .doesNotContain("*");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void shouldThrowWhenCredentialDoesNotExist() {
        when(credentialRepository.findByEmailValue("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldDisableInactiveCredential() {
        IamCredential credential =
                IamCredential.create(UUID.randomUUID(), "user@example.com", "hash", UserRole.MANAGER);
        credential.deactivate();
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));

        IamUserDetails userDetails = (IamUserDetails) userDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder(
                        "ROLE_MANAGER",
                        CatalogPermissions.READ,
                        CatalogPermissions.WRITE,
                        InventoryPermissions.READ,
                        InventoryPermissions.WRITE,
                        FinancialPermissions.READ);
        assertThat(userDetails.isEnabled()).isFalse();
    }
}
