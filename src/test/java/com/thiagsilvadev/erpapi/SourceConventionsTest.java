package com.thiagsilvadev.erpapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Guards the source-level conventions that bytecode-level rules cannot see: no fully qualified names
 * inline in Java code, no FQN-based SpEL type references, and no I/O inside mappers.
 *
 * <p>These rules cannot be expressed with ArchUnit because a fully qualified name leaves no trace in
 * the compiled bytecode once it is resolved.
 */
class SourceConventionsTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Pattern INLINE_FQN =
            Pattern.compile("(?<![\\w.\"])(?:java|jakarta|javax|org|com)\\.[a-z][\\w]*(?:\\.[\\w]+)*\\.[A-Z][\\w]*");
    private static final Pattern SPEL_TYPE_REFERENCE = Pattern.compile("T\\(com\\.thiagsilvadev");
    private static final Pattern MODEL_PLAIN_NULL_CHECK = Pattern.compile("Objects\\.requireNonNull\\(");

    @Test
    void sourcesMustNotUseInlineFullyQualifiedNames() {
        assertThat(violationsOf(INLINE_FQN, true)).isEmpty();
    }

    @Test
    void authorizationExpressionsMustNotReferenceTypes() {
        assertThat(violationsOf(SPEL_TYPE_REFERENCE, false)).isEmpty();
    }

    @Test
    void apiResponsesMustUseIndividualAnnotationsAndSharedErrorReferences() {
        Pattern mapping = Pattern.compile("@(?:Get|Post|Put|Patch|Delete|Request)Mapping\\b");
        Pattern errorResponse = Pattern.compile("@ApiResponse\\(responseCode = \"[45][0-9]{2}\"[^)]*\\)");
        sources()
                .filter(path -> path.toString().contains("/internal/controller/"))
                .filter(path -> path.getFileName().toString().endsWith("Api.java"))
                .forEach(path -> {
                    String source = read(path);
                    assertThat(source).as("Individual responses in %s", path).doesNotContain("@ApiResponses");
                    String declaration = source.substring(source.indexOf("interface "));
                    for (String method : declaration.split(";")) {
                        if (mapping.matcher(method).find()) {
                            assertThat(method)
                                    .as("Documented responses in %s", path)
                                    .contains("@ApiResponse(");
                        }
                    }
                    errorResponse.matcher(source).results().forEach(match -> assertThat(match.group())
                            .as("Shared error reference in %s", path)
                            .contains("ref = "));
                });
    }

    @Test
    void entityModelsMustUseDomainAssertions() {
        List<String> violations = new ArrayList<>();
        sources()
                .filter(path -> path.toString().contains("/internal/model/"))
                .forEach(path -> collect(path, MODEL_PLAIN_NULL_CHECK, true, violations));

        assertThat(violations).isEmpty();
    }

    @Test
    void mappersMustNotResolveDependenciesThemselves() {
        List<String> violations = new ArrayList<>();
        sources()
                .filter(path -> read(path).contains("@Mapper"))
                .filter(path -> !path.toString().contains("/internal/mapper/"))
                .forEach(path -> violations.add(path + " must live in the internal.mapper package"));
        sources()
                .filter(path -> read(path).contains("@Mapper"))
                .filter(path -> read(path).contains("Facade") || read(path).contains("Repository"))
                .forEach(path -> violations.add(path + " injects a facade or repository; use a response assembler"));

        assertThat(violations).isEmpty();
    }

    private static List<String> violationsOf(Pattern pattern, boolean stripLiterals) {
        List<String> violations = new ArrayList<>();
        sources().forEach(path -> collect(path, pattern, stripLiterals, violations));
        return violations;
    }

    private static void collect(Path path, Pattern pattern, boolean stripLiterals, List<String> violations) {
        List<String> lines = read(path).lines().toList();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).stripLeading();
            if (line.startsWith("import ") || line.startsWith("package ")) {
                continue;
            }
            String candidate = stripLiterals ? stripLiteralsAndComments(line) : stripComments(line);
            if (pattern.matcher(candidate).find()) {
                violations.add(path + ":" + (index + 1) + " " + line.strip());
            }
        }
    }

    private static String stripComments(String line) {
        return line.contains("//") ? line.substring(0, line.indexOf("//")) : line;
    }

    private static String stripLiteralsAndComments(String line) {
        return stripComments(line).replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Stream<Path> sources() {
        try {
            return Files.walk(MAIN_SOURCES)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
