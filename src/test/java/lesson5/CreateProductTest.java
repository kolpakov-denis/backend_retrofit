package lesson5;

import com.github.javafaker.Faker;
import lesson5.Api.ProductService;
import lesson5.DTO.Product;
import lesson5.Utilitiess.RetrofitUtils;
import okhttp3.ResponseBody;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import retrofit2.Response;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateProductTest extends BaseTest {

    static ProductService productService;
    static Product product = null;
    static Product product1 = null;
    static Faker faker = new Faker();

    @BeforeAll
    static void beforeAll() {
        productService = RetrofitUtils.getRetrofit()
                .create(ProductService.class);

    }

    //В первом тесте создаём случайный товар запросом POST, сохраняем ID в пропертю
    @Test
    @Order(1)
    void createProductInFoodCategoryTest() throws IOException, InterruptedException {
        product = new Product()
                .withTitle(faker.food().ingredient())
                .withCategoryTitle("Food")
                .withPrice((int) (Math.random() * 10000));
        Response<Product> response = productService.createProduct(product)
                .execute();
        assert response.body() != null;
        setSavedId(response.body().getId().toString());
        assertThat(response.isSuccessful(), CoreMatchers.is(true));

    }

    //Во втором тесте проверяем что при запросе созданного товара по ID, возвращается наш товар
    @Test
    @Order(2)
    void requestByIdTest() throws IOException {
        Response<Product> response = productService.getProductById(getSavedId()).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        Assertions.assertEquals(getSavedId(), response.body().getId());
    }

    //В третьем тесте, исправим цену у нашего товара запросом PUT
    @Test
    @Order(3)
    void editProductByPUTtingNewPriceTest() throws IOException {
            product1 = new Product()
                    .withId(getSavedId())
                    .withCategoryTitle("Food")
                    .withPrice(1000);
        Response<Product> response = productService.modifyProduct(product1)
                .execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        Assertions.assertEquals(1000, response.body().getPrice());

    }
    //Наконец, проверяем DELETE
    @Test
    @Order(4)
    void tearDown() throws IOException {
        Response<ResponseBody> response = productService.deleteProduct(getSavedId()).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
    }

}
