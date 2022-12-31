package com.example.productservice;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	public static final String NAME_IPHONE_13 = "iPhone 13";
	public static final String DESCRIPTION_IS_IPHONE_13 = "description is iPhone 13";
	public static final int PRICE_TEST = 1200;
	public static final String EXPECTED_RESULT ="\"name\":\""+NAME_IPHONE_13+"\",\"description\":\""+DESCRIPTION_IS_IPHONE_13+"\",\"price\":"+PRICE_TEST+"}]";
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
		dymDynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(status().isCreated());
		Assertions.assertEquals(1, productRepository.findAll().size());
	}

	@Test
	void shouldGetOneProduct() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(status().isOk())
				.andDo(print());
		ProductResponse productResponse = new ProductResponse().builder()
				.name(NAME_IPHONE_13)
				.description(DESCRIPTION_IS_IPHONE_13)
				.price(BigDecimal.valueOf(PRICE_TEST))
				.build();
		Assertions.assertTrue(result.andReturn().getResponse().getContentAsString().endsWith(EXPECTED_RESULT)); //not the best option!
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name(NAME_IPHONE_13)
				.description(DESCRIPTION_IS_IPHONE_13)
				.price(BigDecimal.valueOf(PRICE_TEST))
				.build();
	}
}
