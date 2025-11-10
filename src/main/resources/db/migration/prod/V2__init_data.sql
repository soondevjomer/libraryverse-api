-- ============================================
-- V2__init_data.sql
-- Initial data for LibraryVerse
-- ============================================

-- application_user
INSERT INTO application_user (id, created_date, email, image, image_thumbnail, name, password, role, username) VALUES
(1,'2025-11-08 11:05:47.050852','janedoe@librarian.com','','','Jane Doe','$2a$10$ZxBYqQh0FQQ/2NFG0ZApE.CMWGuUV7JhJkSryy09JMrTt/hh6e1eS','LIBRARIAN','janedoe'),
(2,'2025-11-08 11:10:36.680810','mariasantosl@librarian.com','','','Maria L. Santos','$2a$10$CRzE/3/LAc8rbBge1iPBIOYWyo0h7wdJmEscaBFHNFwp0uVSsAUdK','LIBRARIAN','maria'),
(3,'2025-11-08 11:13:13.499087','roberto.dc@librarian.com','','','Roberto M. Dela Cruz','$2a$10$P2c5Bn8dec8Yke9VszTq5.fnZ8Bfi4J.2.Te9hRr8GJkaePAN2La2','LIBRARIAN','roberto'),
(4,'2025-11-08 11:17:10.132404','mdominguezliza@catologer.com','','','Liza M. Dominguez','$2a$10$YQF4HIXw7sE6y6upL96YU.4ui0KcAuHLP67QEWAcCIREnzkeBO0tu','LIBRARIAN','liza');

-- author
INSERT INTO author (author_id, name) VALUES
(1,'Neil Gaiman'),(2,'Ernest Hemingway'),(3,'Yann Martel'),(4,'Sy Montgomery'),(5,'Herman Melville'),
(6,'Sebastian Junger'),(7,'Rachel Carson'),(8,'Peter Wohlleben'),(9,'Robin Wall Kimmerer'),
(10,'Richard Powers'),(11,'Elizabeth Kolbert'),(12,'E. O. Wilson'),(13,'Yuval Noah Harari'),
(14,'George Orwell'),(15,'Daniel Kahneman'),(16,'Alex Michaelides'),(17,'Phil Knight'),
(18,'Paulo Coelho'),(19,'Elaine Castillo'),(20,'Gina Apostol'),(21,'Joanne Ramos'),
(22,'Hannah Tinti'),(23,'Lualhati Bautista');

-- genre
INSERT INTO genre (genre_id, name) VALUES
(1,'Comedy'),(2,'Romance'),(3,'Historical'),(4,'Fantasy'),(5,'Horror'),(6,'Mystery'),
(7,'Thriller'),(8,'Fiction'),(9,'Non-Fiction'),(10,'Literary fantasy'),(11,'modern myth'),
(12,'adventure'),(13,'classic'),(14,'philosophical fiction'),(15,'Nature'),(16,'popular science'),
(17,'Nonfiction'),(18,'maritime disaster narrative'),(19,'nature writing'),(20,'science'),
(21,'Indigenous knowledge'),(22,'nature essay'),(23,'environmental fiction'),(24,'literary'),
(25,'environmental journalism'),(26,'Environmental science'),(27,'activism classic'),
(28,'conservation'),(29,'history'),(30,'Anthropology'),(31,'dystopian'),(32,'political fiction'),
(33,'psychology'),(34,'behavioral economics'),(35,'psychological fiction'),(36,'Memoir'),
(37,'business'),(38,'allegory'),(39,'contemporary fiction'),(40,'diaspora'),
(41,'Historical fiction'),(42,'metafiction'),(43,'contemporary'),(44,'social dystopia'),
(45,'Literary thriller'),(46,'coming-of-age'),(47,'political literature');

-- publisher
INSERT INTO publisher (publisher_id, name) VALUES
(1,'William Morrow'),(2,'Scribner'),(3,'Alfred A. Knopf Canada'),(4,'Atria Books'),
(5,'Penguin'),(6,'W. W. Norton'),(7,'OUP ed.'),(8,'Various'),(9,'Milkweed Editions'),
(10,'Henry Hold & Co.'),(11,'Liveright'),(12,'Harper'),(13,'Signet'),
(14,'Farrar, Straus and Giroux'),(15,'Celadon Books'),(16,'HarperOne'),
(17,'Atlantic Books'),(18,'Soho Press'),(19,'Random House'),(20,'Dial Press'),
(21,'Anvil Publishing');

-- inventory
INSERT INTO inventory (inventory_id, available_stock, delivered, reserved_stock, shipped) VALUES
(1,30,0,0,0),(2,20,0,0,0),(3,41,0,0,0),(4,20,0,0,0),(5,18,0,0,0),(6,24,0,0,0),
(7,22,0,0,0),(8,31,0,0,0),(9,36,0,0,0),(10,25,0,0,0),(11,29,0,0,0),(12,32,0,0,0),
(13,25,0,0,0),(14,15,0,0,0),(15,42,0,0,0),(16,38,0,0,0),(17,26,0,0,0),(18,65,0,0,0),
(19,30,0,0,0),(20,18,0,0,0),(21,12,0,0,0),(22,30,0,0,0),(23,15,0,0,0),(24,24,0,0,0);

-- book_detail
INSERT INTO book_detail (
    book_detail_id,
    book_cover,
    book_thumbnail_cover,
    description,
    price,
    published_year,
    series_title,
    title,
    publisher_id
) VALUES
(1,'','','A man returns to his childhood memories where myth, danger and the sea meet.',650.00,2013,'','The Ocean at the End of the Lane',1),
(2,'','','A man returns to his childhood memories where myth, danger and the sea meet.',550.00,1952,'','The Old Man and the Sea',2),
(3,'','','A boy survives weeks at sea with a Bengal tiger; a story about belief and storytelling.',600.00,2001,'','Life of Pi',3),
(4,'','','Essays and reporting on octopus intelligence and their surprising behavior.',800.00,2015,'','The Soul of an Octopus',4),
(5,'','','The epic chase of the white whale and an exploration of obsession.',500.00,2009,'','Moby-Dick',5),
(6,'','','The true story of the 1991 storm and the fishermen caught in it.',500.00,1997,'','The Perfect Storm',6),
(7,'','','Classic natural history about ocean science and wonder.',700.00,1951,'','The Sea Around Us',7),
(8,'','','How trees communicate and form social networks in forests.',650.00,2016,'','The Hidden Life of Trees',8),
(9,'','','Blends Indigenous wisdom with botany and ecological insight.',900.00,2013,'','Braiding Sweetgrass',9),
(10,'','','Interlinked stories about people and trees across generations.',750.00,2018,'','The Overstory',6),
(11,'','','Investigative reporting on mass extinction events driven by humans.',700.00,2014,'','The Sixth Extinction',10),
(12,'','','Landmark investigation into pesticides and ecological harm.',700.00,1962,'','Silent Spring',8),
(13,'', '','Argument for devoting half the planet to conservation to save biodiversity.',700.00,2016,'','Half-Earth: Our Planet''s Fight for Life',11),
(14,'','','A sweeping history of Homo sapiens and how societies formed.',950.00,2025,'','Sapiens: A Brief History of Humankind',12),
(15,'','','Totalitarian surveillance state and the nature of truth and freedom.',400.00,1949,'','1984 (Signet Classic edition)',13),
(16,'','','Landmark book on human decision-making, heuristics and biases.',750.00,2011,'','Thinking, Fast and Slow',14),
(17,'','','A therapist unravels the mystery of a painter who stopped speaking after murdering her husband.',600.00,2019,'','The Silent Patient',15),
(18,'','','Founder Phil Knight’s candid memoir about starting and building Nike.',700.00,2016,'','A Memoir by the Creator of Nike',2),
(19,'','','A shepherd’s quest for personal legend and spiritual discovery.',400.00,2025,'','The Alchemist',16),
(20,'','','A multi-generational novel about Filipino immigrants, identity, and family life across the Philippines and the U.S.',720.00,2019,'','America Is Not the Heart',17),
(21,'','','A layered story in which a Filipino translator and an American filmmaker confront contested histories of the Philippine–American War.',760.00,2019,'','Insurrecto',18),
(22,'','','A thought-provoking novel about a luxury surrogacy facility and the women (many Filipina) who work there.',780.00,2019,'','The Farm',19),
(23,'','','Father-daughter road tale that mixes crime, myth and a man’s attempt to protect his child from his violent past.',650.00,2017,'','The Twelve Lives of Samuel Hawley',20),
(24,'','','Classic Philippine novel about family life and political awakening during Martial Law.',440.00,2023,'','Dekada ’70',21);

-- libraries (depends on application_user)
INSERT INTO libraries (library_id, address, contact_number, created_date, description, library_cover, library_thumbnail_cover, name, view_count, owner_id) VALUES
(1,'77 Katipunan Ave., Loyola Heights, Quezon City, Philippines','(02) 8921-5568','2025-11-08 11:05:47.067755','A cozy community library in Quezon City that focuses on Philippine literature, history, and cultural identity.','','','BayanWords Community Library',0,1),
(2,'18 Greenleaf Ave., Brgy. Quezon Hills, Quezon City, Metro Manila','(02) 8245-1134','2025-11-08 11:10:36.686039','eco / environment focused library — conservation, ecology, nature essays, and environmental fiction.','','','GreenCanopy Learning Library',0,2),
(3,'9th Floor, Metro Plaza, Gen. Malvar Street, Iloilo City, Philippines','(033) 335-7744','2025-11-08 11:13:13.504093','urban / business / contemporary fiction mix — café vibes and late-night study space.','','','CityLights Urban Library & Café',0,3),
(4,'45 Seaview Rd., Brgy. San Rafael, Zamboanga City, Philippines','(074) 442-9821','2025-11-08 11:17:10.136438','A cozy seaside library by the harbour that emphasises maritime & coastal life-themed reading, but also stocks general fiction and non-fiction.','','','Ocean Waves Readers’ Harbor',0,4);

-- book (depends on book_detail, inventory, libraries)
INSERT INTO book (
  book_id,
  created_date,
  isbn,
  modified_date,
  view_count,
  book_detail_id,
  inventory_id,
  library_id
)
 VALUES
(1,'2025-11-08 11:39:37.393638','9780062255655','2025-11-08 13:28:47.029524',0,1,1,4),
(2,'2025-11-08 11:42:15.297518','9780684830490','2025-11-08 13:29:51.380520',0,2,2,4),
(3,'2025-11-08 11:47:48.080989','9780676978995','2025-11-08 13:31:06.703152',0,3,3,4),
(4,'2025-11-08 11:54:45.033023','9781451697728','2025-11-08 13:31:57.743824',0,4,4,4),
(5,'2025-11-08 12:09:15.386605','9780142437247','2025-11-08 13:32:39.737710',0,5,5,4),
(6,'2025-11-08 12:10:35.709222','9780393050325','2025-11-08 13:33:48.678776',0,6,6,4),
(7,'2025-11-08 12:12:48.957591','9780195147018','2025-11-08 14:21:54.348424',0,7,7,4),
(8,'2025-11-08 12:16:45.224065','9781863958738','2025-11-08 13:36:55.143754',0,8,8,2),
(9,'2025-11-08 12:17:58.198918','9781571313560','2025-11-08 14:21:17.038569',0,9,9,2),
(10,'2025-11-08 12:19:23.537315','9780393356687','2025-11-08 13:54:45.942083',0,10,10,2),
(11,'2025-11-08 12:20:42.848525','9780805092998','2025-11-08 13:58:36.425380',0,11,11,2),
(12,'2025-11-08 12:22:08.233792','9780618249060','2025-11-08 13:59:19.794599',0,12,12,2),
(13,'2025-11-08 12:23:36.897113','9781631490828','2025-11-08 14:06:30.844096',0,13,13,2),
(14,'2025-11-08 12:24:52.414003','9780062316097','2025-11-08 14:07:17.683036',0,14,14,2),
(15,'2025-11-08 12:28:48.795300','9780451524935','2025-11-08 14:16:27.432818',0,15,15,3),
(16,'2025-11-08 12:30:15.673266','9780374533557','2025-11-08 14:17:11.240094',0,16,16,3),
(17,'2025-11-08 12:31:13.363506','9781250301697','2025-11-08 14:18:08.294380',0,17,17,3),
(18,'2025-11-08 12:32:13.274508','9781501135927','2025-11-08 14:18:59.604440',0,18,18,3),
(19,'2025-11-08 12:34:06.588231','9780062315007','2025-11-08 14:19:46.124079',0,19,19,3),
(20,'2025-11-08 13:10:54.792113','9781786491350','2025-11-08 13:24:46.429360',0,20,20,1),
(21,'2025-11-08 13:12:12.881570','9781641290920','2025-11-08 13:25:55.613573',0,21,21,1),
(22,'2025-11-08 13:13:41.163752','9781984853752','2025-11-08 13:27:05.245709',0,22,22,1),
(23,'2025-11-08 13:14:49.853543','9780812989885','2025-11-08 13:20:46.417839',0,23,23,1),
(24,'2025-11-08 13:16:03.484767','9789712737817','2025-11-08 13:18:09.361616',0,24,24,1);

-- book_detail_author (depends on book_detail + author)
INSERT INTO book_detail_author (book_detail_id, author_id) VALUES
(24,23),(23,22),(20,19),(21,20),(22,21),(1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(8,8),
(10,10),(11,11),(12,7),(13,12),(14,13),(15,14),(16,15),(17,16),(18,17),(19,18),(9,9),(7,7);

-- book_detail_genre (depends on book_detail + genre)
INSERT INTO book_detail_genre (book_detail_id, genre_id) VALUES
(24,41),(24,47),(23,45),(23,46),(20,39),(20,40),(21,41),(21,42),(22,43),(22,44),
(1,10),(1,11),(2,12),(2,13),(3,12),(3,14),(4,15),(4,16),(5,13),(5,12),(6,17),(6,18),
(8,15),(8,16),(10,23),(10,24),(11,20),(11,25),(12,26),(12,27),(13,28),(13,20),
(14,29),(14,30),(15,31),(15,32),(16,33),(16,34),(17,35),(17,7),(18,36),(18,37),
(19,8),(19,38),(9,21),(9,22),(7,19),(7,20);
