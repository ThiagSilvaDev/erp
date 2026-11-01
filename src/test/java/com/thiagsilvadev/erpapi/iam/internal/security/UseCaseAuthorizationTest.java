package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class UseCaseAuthorizationTest {

    private static final String[] SECURED_USE_CASES = {
        "com.thiagsilvadev.erpapi.catalog.internal.usecase.category.CategoryQueryUseCase",
        "com.thiagsilvadev.erpapi.catalog.internal.usecase.category.CreateCategoryUseCase",
        "com.thiagsilvadev.erpapi.catalog.internal.usecase.product.CreateProductUseCase",
        "com.thiagsilvadev.erpapi.catalog.internal.usecase.product.ProductQueryUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.CompanyContactQueryUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.CompanyQueryUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyContactUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.MarkCompanyContactPrimaryUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.ContactPersonQueryUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CreateContactPersonUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CreateCustomerUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CreateSupplierUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CustomerQueryUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.SupplierQueryUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.CreateInvoiceUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.GetInvoiceByIdUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.ListInvoicesUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.GetLedgerEntryByIdUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.ListLedgerEntriesUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.payment.CreatePaymentUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.payment.GetPaymentByIdUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.payment.ListPaymentsUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.item.InventoryItemQueryUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation.CreateReservationUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation.ReservationQueryUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement.CreateStockMovementUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement.StockMovementQueryUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.CreateWarehouseUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.WarehouseQueryUseCase",
        "com.thiagsilvadev.erpapi.kpi.internal.usecase.GetExecutiveKpisUseCase",
        "com.thiagsilvadev.erpapi.sales.internal.usecase.CancelSalesOrderUseCase",
        "com.thiagsilvadev.erpapi.sales.internal.usecase.ConfirmSalesOrderUseCase",
        "com.thiagsilvadev.erpapi.sales.internal.usecase.CreateSalesOrderUseCase",
        "com.thiagsilvadev.erpapi.sales.internal.usecase.GetSalesOrderByIdUseCase",
        "com.thiagsilvadev.erpapi.sales.internal.usecase.ListSalesOrdersUseCase",
        "com.thiagsilvadev.erpapi.user.internal.usecase.CreateUserUseCase",
        "com.thiagsilvadev.erpapi.user.internal.usecase.UserQueryUseCase"
    };

    @Test
    void shouldProtectEveryPublicUseCaseMethod() {
        assertThat(Stream.of(SECURED_USE_CASES)
                        .flatMap(className -> Stream.of(type(className).getDeclaredMethods()))
                        .filter(method -> Modifier.isPublic(method.getModifiers()))
                        .toList())
                .allSatisfy(method -> assertThat(method.getAnnotation(PreAuthorize.class))
                        .as(
                                "%s#%s must be guarded by @PreAuthorize",
                                method.getDeclaringClass().getSimpleName(), method.getName())
                        .isNotNull());
    }

    @Test
    void shouldKeepLoginUseCasePublic() throws Exception {
        Method execute = Class.forName("com.thiagsilvadev.erpapi.iam.internal.usecase.LoginUseCase")
                .getDeclaredMethod("execute", Class.forName("com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest"));

        assertThat(execute.getAnnotation(PreAuthorize.class)).isNull();
    }

    private static Class<?> type(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Missing use case class " + className, e);
        }
    }
}
