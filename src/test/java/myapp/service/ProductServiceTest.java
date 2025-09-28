package myapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;
import myapp.domain.Product;
import myapp.domain.enumeration.ProductStatus;
import myapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceTest {

    private Validator validator;

    @BeforeAll
    public void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // Helper method
    private Product createProductSample(
        String title,
        String keywords,
        String description,
        Integer rating,
        Integer quantityInStock,
        String dimensions,
        BigDecimal price,
        ProductStatus status,
        Double weight,
        Instant dateAdded,
        Instant dateModified
    ) {
        return new Product()
            .id(1L)
            .title(title)
            .keywords(keywords)
            .description(description)
            .rating(rating)
            .quantityInStock(quantityInStock)
            .dimensions(dimensions)
            .price(price)
            .status(status)
            .weight(weight)
            .dateAdded(dateAdded)
            .dateModified(dateModified);
    }

    // 1. Title
    @ParameterizedTest(name = "Título = \"{0}\" → válido = {1}")
    @MethodSource("titleProvider")
    void testTitleValidation(String title, boolean expectedValid) {
        Product product = createProductSample(
            title,
            null,
            null,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    private static Stream<Arguments> titleProvider() {
        return Stream.of(
            Arguments.of("", false), // vazio
            Arguments.of("NE", false), // < 3 chars
            Arguments.of("NES", true), // == 3 chars
            Arguments.of("ValidTitle", true), // normal
            Arguments.of("T".repeat(100), true), // limite superior válido
            Arguments.of("T".repeat(101), false) // inválido
        );
    }

    // 2. Keywords
    static Stream<Arguments> keywordsProvider() {
        return Stream.of(
            Arguments.of(null, true), // null → válido
            Arguments.of("", true), // vazio → válido
            Arguments.of("k".repeat(1), true),
            Arguments.of("k".repeat(200), true),
            Arguments.of("k".repeat(201), false)
        );
    }

    @ParameterizedTest(name = "Keywords=\"{0}\" → válido={1}")
    @MethodSource("keywordsProvider")
    void testKeywordsValidation(String keywords, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            keywords,
            null,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 3. Description
    @ParameterizedTest(name = "Descrição length={0} → válido={1}")
    @CsvSource({ "0, true", "49, false", "50, true", "51, true" })
    void testDescriptionValidation(int length, boolean expectedValid) {
        String description = (length == 0) ? null : "d".repeat(length);
        Product product = createProductSample(
            "Valid Title",
            null,
            description,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 4. Rating
    @ParameterizedTest(name = "Avaliação={0} → válido={1}")
    @CsvSource({ ", true", "0, false", "1, true", "2, true", "9, true", "10, true", "11, false" })
    void testRatingValidation(Integer rating, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            rating,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 5. Price
    @ParameterizedTest(name = "Preço={0} → válido={1}")
    @CsvSource({ ", false", "-1, false", "0, false", "1, true", "2, true", "9998, true", "9999, true", "10000, false" })
    void testPriceValidation(BigDecimal price, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            10,
            "10x10x10",
            price,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 6. Quantity in stock
    @ParameterizedTest(name = "Quantidade={0} → válido={1}")
    @CsvSource({ ", false", "-1, false", "0, true", "1, true" })
    void testQuantityValidation(Integer quantity, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            quantity,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 7. Status
    @ParameterizedTest(name = "Status={0} → válido={1}")
    @CsvSource({ ", false", "IN_STOCK, true", "OUT_OF_STOCK, true", "PREORDER, true", "DISCONTINUED, true" })
    void testStatusValidation(ProductStatus status, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            status,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    @Test
    void testStatusInvalidStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ProductStatus.valueOf("INVALID"));
    }

    // 8. Weight
    @ParameterizedTest(name = "Peso={0} → válido={1}")
    @CsvSource({ ", true", "-0.1, false", "0, true", "0.1, true", "50, true" })
    void testWeightValidation(Double weight, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            weight,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 9. Dimensions
    static Stream<Arguments> dimensionsProvider() {
        return Stream.of(
            Arguments.of(null, true),
            Arguments.of("", true),
            Arguments.of("d".repeat(1), true),
            Arguments.of("d".repeat(50), true),
            Arguments.of("d".repeat(51), false)
        );
    }

    @ParameterizedTest(name = "Dimensions=\"{0}\" → válido={1}")
    @MethodSource("dimensionsProvider")
    void testDimensionsValidation(String dimensions, boolean expectedValid) {
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            10,
            dimensions,
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 10. DateAdded
    @ParameterizedTest(name = "Data Adição nula? {0} → válido={1}")
    @CsvSource({ "true, false", "false, true" })
    void testDateAddedValidation(boolean isNull, boolean expectedValid) {
        Instant dateAdded = isNull ? null : Instant.now();
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            dateAdded,
            null
        );
        validateAndAssert(product, expectedValid);
    }

    // 11. DateModified
    @ParameterizedTest(name = "Data Modificação nula? {0} → válido={1}")
    @CsvSource({ "true, true", "false, true" })
    void testDateModifiedValidation(boolean isNull, boolean expectedValid) {
        Instant dateModified = isNull ? null : Instant.now();
        Product product = createProductSample(
            "Valid Title",
            null,
            null,
            5,
            10,
            "10x10x10",
            BigDecimal.TEN,
            ProductStatus.IN_STOCK,
            1.0,
            Instant.now(),
            dateModified
        );
        validateAndAssert(product, expectedValid);
    }

    // ----------------------
    // Utility
    // ----------------------
    private void validateAndAssert(Product product, boolean expectedValid) {
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        if (expectedValid) {
            assertTrue(violations.isEmpty(), "Esperado válido, mas violações: " + violations);
            when(productRepository.save(product)).thenReturn(product);
            Product saved = productService.save(product);
            assertEquals(product, saved);
        } else {
            assertFalse(violations.isEmpty(), "Esperado inválido, mas não encontrou violações");
        }
    }
}
