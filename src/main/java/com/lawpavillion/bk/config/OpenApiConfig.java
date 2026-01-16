package com.lawpavillion.bk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryManagementOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Library Management System");
        contact.setEmail("support@library.com");

        Info info = new Info()
                .title("Library Management System API")
                .version("1.0.0")
                .description("REST API for managing library books with CRUD operations")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
