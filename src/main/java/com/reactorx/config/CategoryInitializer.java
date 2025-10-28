package com.reactorx.config;

import com.reactorx.entity.Category;
import com.reactorx.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    // Define the core categories based on your product data slugs and frontend needs
    private final List<Category> initialCategories = Arrays.asList(
            new Category(null, "Keyboards", "keyboard", "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8Z2FtaW5nJTIwa2V5Ym9hcmR8ZW58MHx8MHx8fDA%3D&auto=format&fit=crop&q=60&w=700"),
            new Category(null, "Mouse and Mousepads", "mouse", "https://images.unsplash.com/photo-1628832307345-7404b47f1751?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8Z2FtaW5nJTIwbW91c2V8ZW58MHx8MHx8fDA%3D&auto=format&fit=crop&q=60&w=700"),
            new Category(null, "Ergonomic Chairs", "chair", "https://assets3.razerzone.com/FLgrYrKmDiQrUrAzhSDivRSp0Jg=/1500x1000/https%3A%2F%2Fmedias-p1.phoenix.razer.com%2Fsys-master-phoenix-images-container%2Fha3%2Fh00%2F9249242578974%2F211021-enki-black-1500x1000-1.jpg"),
            new Category(null, "Controllers", "controller", "https://assets3.razerzone.com/rveF8DzBs3lBvDIHwsrDxmoUBpM=/1500x1000/https%3A%2F%2Fmedias-p1.phoenix.razer.com%2Fsys-master-phoenix-images-container%2Fh63%2Fh47%2F9810086559774%2F240828-wolverine-v3-pro-black-1500x1000-1.jpg"),
            new Category(null, "Monitors", "monitor", "https://pixiogaming.com/cdn/shop/articles/PX279PN_Desktop_1.jpg?v=1724704163&width=3840"),
            // Separate slugs used in products.json
            new Category(null, "Audio Devices", "audio", "https://assets3.razerzone.com/Lu4eFyqkgHGmXpxjTDNoBXrXjuI=/767x511/https%3A%2F%2Fmedias-p1.phoenix.razer.com%2Fsys-master-phoenix-images-container%2Fh6f%2Fh28%2F9392050307102%2F220421-leviathan-v2-1500x1000-6.jpg"),
            new Category(null, "Webcams & Mics", "camera", "https://assets3.razerzone.com/8eAGb-hYEt3_InnCkHYkLel7XEk=/1500x1000/https%3A%2F%2Fmedias-p1.phoenix.razer.com%2Fsys-master-phoenix-images-container%2Fh09%2Fh86%2F9974722428958%2F251015-kiyo-v2-b-1500x1000-1.jpg"),
            new Category(null, "Lighting & Coolers", "lighting", "https://assets3.razerzone.com/MOuemPIJIY6XoD0-4MQjMTEpsog=/767x511/https%3A%2F%2Fmedias-p1.phoenix.razer.com%2Fsys-master-phoenix-images-container%2Fh2d%2Fh99%2F9747905609758%2F240306-aether-lamp-pro-1500x1000-1.jpg")
    );

    // This category is for the combined frontend link
    private final List<Category> frontendCategories = Arrays.asList(
            new Category(null, "Audio, Video & Lights", "audio-video", "https://via.placeholder.com/150?text=AV")
    );


    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            System.out.println("Loading initial category data...");

            // Save core categories
            categoryRepository.saveAll(initialCategories);

            // Save the front-end specific composite category (if needed by frontend lookup logic)
            categoryRepository.saveAll(frontendCategories);

            System.out.println("Loaded " + categoryRepository.count() + " categories.");
        } else {
            System.out.println("Database already contains category data. Skipping initialization.");
        }
    }
}