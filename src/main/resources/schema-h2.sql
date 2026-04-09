-- H2 Database Schema for ReactorX
-- This file ensures tables are created before data initialization

-- Categories Table
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    image_url VARCHAR(500)
);

-- Users Table  
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products Table
CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2),
    mrp DECIMAL(10,2),
    discount_percentage INTEGER,
    rating DOUBLE PRECISION,
    review_count INTEGER,
    image_url VARCHAR(500),
    info CLOB,
    category_slug VARCHAR(255),
    sold_count VARCHAR(50),
    media_json CLOB,
    feature_icon_grid_json CLOB,
    hero_video_json CLOB,
    feature_stats_json CLOB,
    feature_sections_json CLOB,
    specs_v2_json CLOB,
    feature_banner_text_json CLOB,
    feature_banner_image_json CLOB,
    gallery_banners_json CLOB,
    switch_options_json CLOB,
    colors_json CLOB
);

-- Status Tags Table (for Product's ElementCollection)
CREATE TABLE IF NOT EXISTS product_status_tags (
    product_id BIGINT NOT NULL,
    status_tags VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_category_slug ON category(slug);
CREATE INDEX IF NOT EXISTS idx_product_category_slug ON product(category_slug);
CREATE INDEX IF NOT EXISTS idx_product_name ON product(name);
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
