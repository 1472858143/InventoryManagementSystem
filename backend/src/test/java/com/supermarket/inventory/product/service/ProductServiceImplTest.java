package com.supermarket.inventory.product.service;

import com.supermarket.inventory.category.entity.Category;
import com.supermarket.inventory.category.mapper.CategoryMapper;
import com.supermarket.inventory.product.dto.ProductCreateRequest;
import com.supermarket.inventory.product.entity.Product;
import com.supermarket.inventory.product.mapper.ProductMapper;
import com.supermarket.inventory.product.model.ProductView;
import com.supermarket.inventory.product.service.impl.ProductServiceImpl;
import com.supermarket.inventory.product.vo.ProductDetailResponse;
import com.supermarket.inventory.product.vo.ProductListItemResponse;
import com.supermarket.inventory.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private StockService stockService;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productMapper, categoryMapper, stockService);
    }

    @Test
    void createProductShouldAutoGenerateProductCodeWhenMissing() {
        AtomicReference<Product> insertedProduct = stubEnabledCategoryAndSuccessfulInsert();

        ProductDetailResponse response = productService.createProduct(new ProductCreateRequest(
            null,
            "矿泉水",
            1L,
            "瓶",
            decimal("1.20"),
            decimal("2.50")
        ));

        assertNotNull(response.productCode());
        assertTrue(response.productCode().matches("P\\d{18}"));
        assertEquals(response.productCode(), insertedProduct.get().getProductCode());
        verify(stockService).initializeStockForProduct(10L);
    }

    @Test
    void createProductShouldRetryOnceWhenGeneratedProductCodeCollides() {
        AtomicReference<Product> insertedProduct = new AtomicReference<>();
        stubEnabledCategory();
        doThrow(new DuplicateKeyException("duplicate product_code"))
            .doAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                product.setId(10L);
                insertedProduct.set(product);
                return 1;
            })
            .when(productMapper).insert(any(Product.class));
        when(productMapper.findByIdWithCategory(10L)).thenAnswer(invocation -> buildProductView(insertedProduct.get(), 0));

        ProductDetailResponse response = productService.createProduct(new ProductCreateRequest(
            "",
            "矿泉水",
            1L,
            "瓶",
            decimal("1.20"),
            decimal("2.50")
        ));

        assertNotNull(response.productCode());
        assertTrue(response.productCode().matches("P\\d{18}"));
        verify(productMapper, times(2)).insert(any(Product.class));
        verify(stockService).initializeStockForProduct(10L);
    }

    @Test
    void createProductShouldKeepProvidedProductCode() {
        AtomicReference<Product> insertedProduct = stubEnabledCategoryAndSuccessfulInsert();
        when(productMapper.findByProductCode("P_TEST_001")).thenReturn(null);

        ProductDetailResponse response = productService.createProduct(new ProductCreateRequest(
            "P_TEST_001",
            "矿泉水",
            1L,
            "瓶",
            decimal("1.20"),
            decimal("2.50")
        ));

        assertEquals("P_TEST_001", response.productCode());
        assertEquals("P_TEST_001", insertedProduct.get().getProductCode());
    }

    @Test
    void listProductsShouldExposeNonNullSalesCount() {
        ProductView view = new ProductView();
        view.setId(1L);
        view.setProductCode("P001");
        view.setProductName("矿泉水");
        view.setCategoryId(1L);
        view.setCategoryName("饮料");
        view.setUnit("瓶");
        view.setPurchasePrice(decimal("1.20"));
        view.setSalePrice(decimal("2.50"));
        view.setStatus(1);
        view.setCreateTime(LocalDateTime.now());
        setSalesCountIfAvailable(view, 7);
        when(productMapper.findAllWithCategory()).thenReturn(List.of(view));

        List<ProductListItemResponse> products = productService.listProducts();

        assertEquals(1, products.size());
        assertEquals(7, readRecordComponent(products.get(0), "salesCount"));
    }

    private AtomicReference<Product> stubEnabledCategoryAndSuccessfulInsert() {
        AtomicReference<Product> insertedProduct = new AtomicReference<>();
        stubEnabledCategory();
        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(10L);
            insertedProduct.set(product);
            return 1;
        }).when(productMapper).insert(any(Product.class));
        when(productMapper.findByIdWithCategory(10L)).thenAnswer(invocation -> buildProductView(insertedProduct.get(), 0));
        return insertedProduct;
    }

    private void stubEnabledCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("饮料");
        category.setStatus(1);
        when(categoryMapper.findById(1L)).thenReturn(category);
    }

    private ProductView buildProductView(Product product, Integer salesCount) {
        ProductView view = new ProductView();
        view.setId(product.getId());
        view.setProductCode(product.getProductCode());
        view.setProductName(product.getProductName());
        view.setCategoryId(product.getCategoryId());
        view.setCategoryName("饮料");
        view.setUnit(product.getUnit());
        view.setPurchasePrice(product.getPurchasePrice());
        view.setSalePrice(product.getSalePrice());
        view.setStatus(product.getStatus());
        view.setCreateTime(LocalDateTime.now());
        setSalesCountIfAvailable(view, salesCount);
        return view;
    }

    private void setSalesCountIfAvailable(ProductView view, Integer salesCount) {
        try {
            Field field = ProductView.class.getDeclaredField("salesCount");
            field.setAccessible(true);
            field.set(view, salesCount);
        } catch (NoSuchFieldException ignored) {
            // The red test should fail later when the response has no salesCount component.
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private Object readRecordComponent(Object record, String componentName) {
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            if (component.getName().equals(componentName)) {
                try {
                    return component.getAccessor().invoke(record);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            }
        }
        fail(record.getClass().getSimpleName() + " should expose " + componentName);
        return null;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
