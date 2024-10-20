-- MySQL dump 10.13  Distrib 9.0.1, for macos14.4 (arm64)
--
-- Host: 192.168.50.116    Database: personal_home_upload
-- ------------------------------------------------------
-- Server version	8.0.39-0ubuntu0.22.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `file_system`
--

DROP TABLE IF EXISTS `file_system`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_system`
(
    `id`           bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         varchar(255)     NOT NULL COMMENT '文件或文件夹名',
    `path`         varchar(1024)    NOT NULL COMMENT '路径',
    `size`         bigint unsigned           DEFAULT NULL COMMENT '文件大小(字节) 文件夹的话为空',
    `content_type` varchar(100)              DEFAULT NULL COMMENT '文件MIME类型，文件夹为空',
    `type`         tinyint unsigned NOT NULL COMMENT '类型: 文件(0)文件夹(1)',
    `parent_id`    bigint unsigned           DEFAULT NULL COMMENT '父文件夹id',
    `create_time`  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `file_system_create_time_index` (`create_time`),
    KEY `file_system_name_index` (`name`),
    KEY `file_system_parent_id_index` (`parent_id`),
    CONSTRAINT `file_system_file_system_id_fk` FOREIGN KEY (`parent_id`) REFERENCES `file_system` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='文件系统';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_system`
--

/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2024-10-20 17:51:04
