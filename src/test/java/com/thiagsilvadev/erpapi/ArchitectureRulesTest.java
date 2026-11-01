package com.thiagsilvadev.erpapi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class ArchitectureRulesTest {

    private static final String BASE_PACKAGE = "com.thiagsilvadev.erpapi";
    private static final Set<String> REPOSITORY_VERBS = Set.of("find", "exists", "save", "delete", "count");

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(BASE_PACKAGE);

    @Test
    void afterCommitSideEffectsMustUseTheSharedHelper() {
        List<String> violations = classes.stream()
                .filter(javaClass -> !javaClass.getPackageName().startsWith(BASE_PACKAGE + ".common.transaction"))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(dependency -> dependency
                        .getTargetClass()
                        .getName()
                        .startsWith("org.springframework.transaction.support.TransactionSynchronization"))
                .map(dependency -> dependency.getOriginClass().getName() + " -> "
                        + dependency.getTargetClass().getName())
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    void useCasesMustNotDependOnFacadeImplementations() {
        List<String> violations = classes.stream()
                .filter(javaClass -> javaClass.getPackageName().contains(".internal.usecase"))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(dependency ->
                        dependency.getTargetClass().getSimpleName().endsWith("FacadeImpl"))
                .map(dependency -> dependency.getOriginClass().getName() + " -> "
                        + dependency.getTargetClass().getName())
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    void mapstructMappersLiveInTheMapperPackage() {
        noClasses()
                .that()
                .areAnnotatedWith(Mapper.class)
                .should()
                .resideOutsideOfPackages("..internal.mapper..")
                .check(classes);
    }

    @Test
    void controllersMustNotAccessPersistence() {
        noClasses()
                .that()
                .resideInAPackage("..internal.controller..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..internal.repository..", "jakarta.persistence..", "org.springframework.jdbc..")
                .check(classes);
    }

    @Test
    void controllersMustNotImplementModuleFacades() {
        var violations = classes.stream()
                .filter(javaClass -> javaClass.getPackageName().contains(".internal.controller"))
                .filter(javaClass -> javaClass.getAllRawInterfaces().stream()
                        .anyMatch(
                                interfaceClass -> interfaceClass.getSimpleName().endsWith("Facade")))
                .map(JavaClass::getName)
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    void facadeContractsMustNotExposeRepositoryVocabularyOrOptionalResults() {
        var facades = classes.stream()
                .filter(JavaClass::isInterface)
                .filter(javaClass -> javaClass.getPackageName().endsWith(".api"))
                .filter(javaClass -> javaClass.getSimpleName().endsWith("Facade"))
                .toList();

        var repositoryNamedMethods = facades.stream()
                .flatMap(facade -> facade.getMethods().stream())
                .filter(method -> REPOSITORY_VERBS.stream().anyMatch(method.getName()::startsWith))
                .map(method -> method.getOwner().getSimpleName() + "." + method.getName())
                .toList();
        var optionalMethods = facades.stream()
                .flatMap(facade -> facade.getMethods().stream())
                .filter(method -> method.getRawReturnType().isEquivalentTo(Optional.class))
                .map(method -> method.getOwner().getSimpleName() + "." + method.getName())
                .toList();

        assertThat(repositoryNamedMethods).isEmpty();
        assertThat(optionalMethods).isEmpty();
    }
}
