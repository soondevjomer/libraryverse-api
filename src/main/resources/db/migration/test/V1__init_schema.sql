-- Flyway baseline dev: initial schema for LibraryVerse

CREATE TABLE application_user (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  created_date DATETIME(6),
  email VARCHAR(255) NOT NULL,
  image VARCHAR(255),
  image_thumbnail VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('LIBRARIAN','READER'),
  username VARCHAR(255) NOT NULL,
  UNIQUE KEY UK_email (email),
  UNIQUE KEY UK_username (username)
);

CREATE TABLE author (
  author_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE genre (
  genre_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE publisher (
  publisher_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE inventory (
  inventory_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  available_stock INT,
  delivered INT,
  reserved_stock INT,
  shipped INT
);

CREATE TABLE libraries (
  library_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  address VARCHAR(255),
  contact_number VARCHAR(255),
  created_date DATETIME(6),
  description TEXT,
  library_cover VARCHAR(255),
  library_thumbnail_cover VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  view_count BIGINT,
  owner_id BIGINT UNIQUE,
  CONSTRAINT fk_libraries_owner FOREIGN KEY (owner_id)
      REFERENCES application_user (id)
);

CREATE TABLE customer (
  customer_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  address VARCHAR(255),
  contact_number VARCHAR(255),
  created_date DATETIME(6),
  user_id BIGINT NOT NULL UNIQUE,
  CONSTRAINT fk_customer_user FOREIGN KEY (user_id)
      REFERENCES application_user (id)
);

CREATE TABLE book_detail (
  book_detail_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  book_cover VARCHAR(255),
  book_thumbnail_cover VARCHAR(255),
  description TEXT,
  price DECIMAL(38,2),
  published_year BIGINT,
  series_title VARCHAR(255),
  title VARCHAR(255) NOT NULL,
  publisher_id BIGINT,
  CONSTRAINT fk_bookdetail_publisher FOREIGN KEY (publisher_id)
      REFERENCES publisher (publisher_id)
);

CREATE TABLE book (
  book_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  created_date DATETIME(6),
  isbn VARCHAR(255) NOT NULL,
  modified_date DATETIME(6),
  view_count BIGINT,
  book_detail_id BIGINT UNIQUE,
  inventory_id BIGINT UNIQUE,
  library_id BIGINT,
  CONSTRAINT fk_book_bookdetail FOREIGN KEY (book_detail_id)
      REFERENCES book_detail (book_detail_id),
  CONSTRAINT fk_book_inventory FOREIGN KEY (inventory_id)
      REFERENCES inventory (inventory_id),
  CONSTRAINT fk_book_library FOREIGN KEY (library_id)
      REFERENCES libraries (library_id)
);

CREATE TABLE book_detail_author (
  book_detail_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  CONSTRAINT fk_bda_bookdetail FOREIGN KEY (book_detail_id)
      REFERENCES book_detail (book_detail_id),
  CONSTRAINT fk_bda_author FOREIGN KEY (author_id)
      REFERENCES author (author_id)
);

CREATE TABLE book_detail_genre (
  book_detail_id BIGINT NOT NULL,
  genre_id BIGINT NOT NULL,
  CONSTRAINT fk_bdg_bookdetail FOREIGN KEY (book_detail_id)
      REFERENCES book_detail (book_detail_id),
  CONSTRAINT fk_bdg_genre FOREIGN KEY (genre_id)
      REFERENCES genre (genre_id)
);

CREATE TABLE cart (
  cart_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  quantity INT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id)
      REFERENCES application_user (id),
  CONSTRAINT fk_cart_book FOREIGN KEY (book_id)
      REFERENCES book (book_id)
);

CREATE TABLE orders (
  order_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_date DATETIME(6),
  payment_method ENUM('BANK_TRANSFER','COD','DIGITAL_WALLET'),
  payment_status ENUM('FAILED','PAID','PENDING','REFUNDED','UNPAID'),
  total_amount DECIMAL(38,2),
  customer_id BIGINT,
  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
      REFERENCES customer (customer_id)
);

CREATE TABLE store_order (
  store_order_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_date DATETIME(6),
  order_status ENUM('CANCELLED','DELIVERED','PENDING','SHIPPED'),
  payment_status ENUM('FAILED','PAID','PENDING','REFUNDED','UNPAID'),
  subtotal DECIMAL(38,2),
  library_id BIGINT,
  order_id BIGINT,
  CONSTRAINT fk_storeorder_library FOREIGN KEY (library_id)
      REFERENCES libraries (library_id),
  CONSTRAINT fk_storeorder_order FOREIGN KEY (order_id)
      REFERENCES orders (order_id)
);

CREATE TABLE order_item (
  order_item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  bought_at_price DECIMAL(38,2),
  quantity INT NOT NULL,
  book_book_id BIGINT,
  store_order_store_order_id BIGINT,
  CONSTRAINT fk_orderitem_book FOREIGN KEY (book_book_id)
      REFERENCES book (book_id),
  CONSTRAINT fk_orderitem_storeorder FOREIGN KEY (store_order_store_order_id)
      REFERENCES store_order (store_order_id)
);

CREATE TABLE tag (
  tag_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255)
);