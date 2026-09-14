package uz.example.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryOpenAPI() {

        Contact contact = new Contact()
                .name("Library Management System");

        Info info = new Info()
                .title("Library Management System API")
                .version("v1")
                .description(
                        "Kutubxona boshqaruv tizimi uchun REST API. " +
                                "Mualliflar, kategoriyalar, kitoblar, kitobxonlar, " +
                                "qarz berish va qaytarish jarayonlarini boshqaradi."
                )
                .contact(contact);

        return new OpenAPI()
                .info(info);
    }
}