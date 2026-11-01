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
        "com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyContactUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyContactByIdUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyByIdUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.ListCompanyContactsUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.ListCompaniesUseCase",
        "com.thiagsilvadev.erpapi.company.internal.usecase.MarkCompanyContactPrimaryUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CreateContactPersonUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CreateCustomerUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.CreateSupplierUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.GetContactPersonByIdUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.GetCustomerByIdUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.GetSupplierByIdUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.ListContactPersonsUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.ListCustomersUseCase",
        "com.thiagsilvadev.erpapi.customer.internal.usecase.ListSuppliersUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.CreateInvoiceUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.GetInvoiceByIdUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.invoice.ListInvoicesUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.GetLedgerEntryByIdUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.ListLedgerEntriesUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.payment.CreatePaymentUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.payment.GetPaymentByIdUseCase",
        "com.thiagsilvadev.erpapi.financial.internal.usecase.payment.ListPaymentsUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation.CreateReservationUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement.CreateStockMovementUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.CreateWarehouseUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.item.GetInventoryItemByIdUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation.GetReservationByIdUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement.GetStockMovementByIdUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.GetWarehouseByIdUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.item.ListInventoryItemsUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation.ListReservationsUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement.ListStockMovementsUseCase",
        "com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.ListWarehousesUseCase",
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
