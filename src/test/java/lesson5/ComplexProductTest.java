package lesson5;

import com.github.javafaker.Faker;
import lesson5.api.ProductService;
import lesson5.dto.Product;
import lesson5.utils.RetrofitUtils;
import okhttp3.ResponseBody;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import retrofit2.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComplexProductTest extends BaseTest {

    static ProductService productService;
    static Product product = null;
    static Product product1 = null;
    static Faker faker = new Faker();

    String resource = "mybatis-config.xml";
    InputStream inputStream;

    {
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    db.dao.ProductsMapper productsMapper = sqlSession.getMapper(db.dao.ProductsMapper.class);
    db.model.ProductsExample productsExample = new db.model.ProductsExample();

    @BeforeAll
    public static void beforeAll() {
        productService = RetrofitUtils.getRetrofit()
                .create(ProductService.class);

    }

    //В первом тесте создаём случайный товар запросом POST, сохраняем ID в пропертю
    @Test
    @Order(1)
    void createProductInFoodCategoryTest() throws IOException {
        product = new Product()
                .withTitle(faker.food().ingredient())
                .withCategoryTitle("Food")
                .withPrice((int) (Math.random() * 10000));
        Response<Product> response = productService.createProduct(product)
                .execute();
        assert response.body() != null;
        setSavedId(response.body().getId().toString());
        setSavedTitle(response.body().getTitle());
        assertThat(response.isSuccessful(), CoreMatchers.is(true));


    }

    //Во втором тесте проверяем что при запросе созданного товара по ID, возвращается наш товар
    //Потом проверяем что в БД сохранился товар с таким же ID (PK) и в нужной категории
    @Test
    @Order(2)
    void requestByIdTest() throws IOException {
        Response<Product> response = productService.getProductById(getSavedId()).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        Assertions.assertEquals(getSavedId(), response.body().getId());
        db.model.Products selected = productsMapper.selectByPrimaryKey(Long.valueOf(getSavedId()));
        Assertions.assertEquals(selected.getId(), Long.valueOf(getSavedId()));
        Assertions.assertEquals(selected.getCategory_id(), 1);
    }

    //В третьем тесте, исправим цену у нашего товара запросом PUT
    //Также проверяем, что цена товара изменилась в БД
    @Test
    @Order(3)
    void editProductByPUTtingNewPriceTest() throws IOException {
        product1 = new Product()
                .withId(getSavedId())
                .withCategoryTitle("Food")
                .withTitle(getSavedTitle())
                .withPrice(1000);
        Response<Product> response = productService.modifyProduct(product1)
                .execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        Assertions.assertEquals(1000, response.body().getPrice());
        db.model.Products selected = productsMapper.selectByPrimaryKey(Long.valueOf(getSavedId()));
        Assertions.assertEquals(selected.getPrice(), 1000);
        Assertions.assertEquals(selected.getTitle(), getSavedTitle());
        Assertions.assertEquals(selected.getCategory_id(), 1);


    }
    //Наконец, проверяем DELETE
    //Проверяем, что товар также исчез из БД
    @Test
    @Order(4)
    void tearDown() throws IOException {
        Response<ResponseBody> response = productService.deleteProduct(getSavedId()).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        db.model.Products selected = productsMapper.selectByPrimaryKey(Long.valueOf(getSavedId()));
        Assertions.assertNull(selected);
    }

}