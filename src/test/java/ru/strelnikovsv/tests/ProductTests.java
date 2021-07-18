package ru.strelnikovsv.tests;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.strelnikovsv.dto.Category;
import ru.strelnikovsv.dto.ErrorMessage;
import ru.strelnikovsv.dto.Product;
import ru.strelnikovsv.enums.CategoryType;
import ru.strelnikovsv.service.CategoryService;
import ru.strelnikovsv.service.ProductService;
import ru.strelnikovsv.utils.RetrofitUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
class ProductTests {
    final Integer wrongProductId = -1;
    Integer productId;

    static Retrofit client;
    static ProductService productService;
    static CategoryService categoryService;

    Product product;

    Faker faker = new Faker();

    @BeforeAll
    static void BeforeAll() {
        client = RetrofitUtils.getRetrofit();
        productService = client.create(ProductService.class);
        categoryService = client.create(CategoryService.class);
    }

    @BeforeEach
    void setUp() throws IOException {
        product = new Product()
                .withTitle(faker.food().dish())
                .withPrice((int) (((Math.random() + 1)) * 100))
                .withCategoryTitle("Food");

        Response<Product> response = productService.createProduct(product).execute();
        productId = Objects.requireNonNull(response.body()).getId();
    }

    @Test
    @DisplayName("Получение списка продуктов")
    void getProductTest() throws IOException {
        Response<List<Product>> response = productService.getProducts().execute();
        log.info(Objects.requireNonNull(response.body()).toString());
        assertThat(response.body().size(), CoreMatchers.not(0));
    }

    @Test
    @DisplayName("Получение продукта по ID")
    void getProductByIdPositiveTest() throws IOException {
        Response<Product> response = productService.getProduct(productId).execute();
        log.info(Objects.requireNonNull(response.body()).toString());
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    @DisplayName("Получение продукта по ошибочному ID")
    void getProductByIdNegativeTest() throws IOException {
        Response<Product> response = productService.getProduct(wrongProductId).execute();
        assertThat(response.code(), equalTo(404));
        ErrorMessage errorMessage = RetrofitUtils.convertBody(response, ErrorMessage.class);
        if (errorMessage != null) {
            assertThat(errorMessage.getMessage(), equalTo("Unable to find product with id: " + wrongProductId));
        }
    }

    @Test
    @DisplayName("Создание продукта")
    void postProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        log.info(Objects.requireNonNull(response.body()).toString());
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    @DisplayName("Получение категорий по ID")
    void getCategoryByIdTest() throws IOException {
        Integer id = CategoryType.FOOD.getId();
        Response<Category> response = categoryService.getCategory(id).execute();
        log.info(Objects.requireNonNull(response.body()).toString());
        assertThat(response.body().getTitle(), equalTo(CategoryType.FOOD.getTitle()));
        assertThat(response.body().getId(), equalTo(id));
    }

    @Test
    @DisplayName("Создание пустого продукта")
    void createNewProductEmptyFieldsTest() throws IOException {
        Response<Product> response = productService.createProduct(new Product()).execute();
        log.info(Objects.requireNonNull(response.errorBody()).string());
        assertThat(response.code(), equalTo(500));
    }

    @Test
    @DisplayName("Создание продукта c длинным именем")
    void createNewProductLongTitleTest() throws IOException {
        Response<Product> response = productService
                .createProduct(new Product().withTitle(faker.lorem().fixedString(5000)))
                .execute();
        log.info(Objects.requireNonNull(response.errorBody()).string());
        assertThat(response.code(), equalTo(500));
    }

    @Test
    @DisplayName("Удаление продукта")
    void deleteProductTest() throws IOException {
        Response<ResponseBody> response = productService.deleteProduct(productId).execute();
        productId = null;
        assertNull(response.errorBody());
    }

    @Test
    @DisplayName("Удаление несуществующего продукта")
    void deleteNotExistsProductTest() throws IOException {
        Response<ResponseBody> response = productService.deleteProduct(wrongProductId).execute();
        log.info(Objects.requireNonNull(response.errorBody()).string());
        assertThat(response.code(), equalTo(500));
    }

    @Test
    @DisplayName("Обновление продукта")
    void updateProductTest() throws IOException {
        Product newProduct = new Product()
                .withId(productId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        Response<Product> response = productService.updateProduct(newProduct).execute();
        log.info(Objects.requireNonNull(response.body()).toString());
        assertThat(response.body().getId(), equalTo(productId));
        assertThat(response.body().getPrice(), equalTo(newProduct.getPrice()));
        assertThat(response.body().getTitle(), equalTo(newProduct.getTitle()));
        assertThat(response.body().getCategoryTitle(), equalTo(newProduct.getCategoryTitle()));
    }

    @Test
    @DisplayName("Обновление продукта с ошибочным ID")
    void updateProductTestNegative() throws IOException {
        Product newProduct = new Product()
                .withId(wrongProductId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        Response<Product> response = productService.updateProduct(newProduct).execute();
        assertThat(response.code(), equalTo(400));
        ErrorMessage errorMessage = RetrofitUtils.convertBody(response, ErrorMessage.class);
        if (errorMessage != null) {
            assertThat(errorMessage.getMessage(), equalTo("Product with id: " + wrongProductId + " doesn't exist"));
        }
    }

    @AfterEach
    void tearDown() {
        if (productId != null)
            try {
                Response<ResponseBody> response = productService.deleteProduct(productId).execute();
                log.info(Objects.requireNonNull(response.body()).toString());
                assertThat(response.code(), equalTo(200));
            } catch (IOException e) {
                log.error("productId is null");
            }
    }

}

