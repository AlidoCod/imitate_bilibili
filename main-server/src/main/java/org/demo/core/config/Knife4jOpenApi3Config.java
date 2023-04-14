package org.demo.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jOpenApi3Config {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(info());
    }

    private Info info() {
        Info info = new Info();
        info.setContact(contact());
        info.setDescription("a simple spring application demo which can help your build your project");
        info.setTitle("SpringDoc API");
        info.setVersion("0.0.1");
        return info;
    }

    private Contact contact() {
        Contact contact = new Contact();
        contact.setName("すき");
        contact.setEmail("194183997@qq.com");
        contact.setUrl("https://github.com/AlidoCod");
        return contact;
    }
}