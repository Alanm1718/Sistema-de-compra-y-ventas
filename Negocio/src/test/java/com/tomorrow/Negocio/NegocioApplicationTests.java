package com.tomorrow.Negocio;

import com.tomorrow.Negocio.LoginAdmin.AdminRepository;
import com.tomorrow.Negocio.LoginAdmin.AdminUsuario;
import com.tomorrow.Negocio.product.Product;
import com.tomorrow.Negocio.product.ProductRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NegocioApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	void pedidosPendientesRequierenAutenticacion() throws Exception {
		mockMvc.perform(get("/api/pedidos/pendientes"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/auth/session"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Transactional
	@Rollback
	void clientePuedeEnviarPedidoYAdministracionPuedeVerlo() throws Exception {
		String suffix = UUID.randomUUID().toString().replace("-", "");
		String clienteUsername = "cliente_" + suffix;
		String adminUsername = "admin_" + suffix;
		String productName = "Producto " + suffix;
		String password = "clave-segura-123";
		Product product = productRepository.saveAndFlush(
				new Product(productName, "Prueba", 2500, 10, 0, null));
		adminRepository.saveAndFlush(new AdminUsuario(adminUsername, passwordEncoder.encode(password), "ADMIN"));

		MvcResult csrfResult = mockMvc.perform(get("/api/v1/auth/csrf"))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode csrfBody = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
		String csrfToken = csrfBody.get("token").asText();
		Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		MvcResult registration = mockMvc.perform(post("/api/v1/auth/cliente/registro")
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of("username", clienteUsername, "password", password))))
				.andExpect(status().isCreated())
				.andReturn();
		MockHttpSession clienteSession = (MockHttpSession) registration.getRequest().getSession(false);
		mockMvc.perform(get("/api/v1/auth/session").session(clienteSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(clienteUsername));

		MvcResult orderResult = mockMvc.perform(post("/api/pedidos")
					.session(clienteSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of(
							"items", List.of(Map.of("productId", product.getId(), "cantidad", 2))))))
				.andExpect(status().isCreated())
				.andReturn();
		long pedidoId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
				.path("data").path("id").asLong();

		mockMvc.perform(get("/api/pedidos/cliente").session(clienteSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(productName)));

		MvcResult adminLogin = mockMvc.perform(post("/api/v1/auth/admin/login")
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of("username", adminUsername, "password", password))))
				.andExpect(status().isOk())
				.andReturn();
		MockHttpSession adminSession = (MockHttpSession) adminLogin.getRequest().getSession(false);

		mockMvc.perform(get("/api/pedidos/pendientes").session(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(clienteUsername)))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(productName)))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(password))));

		mockMvc.perform(get("/api/pedidos/pendientes").session(clienteSession))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/api/pedidos/{pedidoId}/aprobar", pedidoId)
					.session(adminSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/pedidos/preparacion").session(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(productName)));

		mockMvc.perform(put("/api/pedidos/{pedidoId}/listo", pedidoId)
					.session(adminSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/pedidos/listos").session(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(productName)));

		mockMvc.perform(get("/api/pedidos/cliente").session(clienteSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("LISTO")));

		mockMvc.perform(put("/api/pedidos/{pedidoId}/entregar", pedidoId)
					.session(adminSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of("formaPago", "EFECTIVO"))))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/pedidos/cliente").session(clienteSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("ENTREGADO")));

		mockMvc.perform(get("/api/v1/ventas").session(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(productName)))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("EFECTIVO")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"pedido\"")));

		org.junit.jupiter.api.Assertions.assertEquals(8,
				productRepository.findById(product.getId()).orElseThrow().getStock());

		mockMvc.perform(post("/api/v1/ventas")
					.session(adminSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of(
							"productId", product.getId(),
							"cantidad", 1,
							"formaPago", "TRANSFERENCIA"))))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/ventas").session(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("TRANSFERENCIA")));
		org.junit.jupiter.api.Assertions.assertEquals(7,
				productRepository.findById(product.getId()).orElseThrow().getStock());

		mockMvc.perform(put("/api/pedidos/{pedidoId}/entregar", pedidoId)
					.session(adminSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(Map.of("formaPago", "EFECTIVO"))))
				.andExpect(status().isBadRequest());
	}

}
