-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 18-02-2026 a las 00:02:54
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `auto`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `adjunto`
--

CREATE TABLE `adjunto` (
  `id` bigint(20) NOT NULL,
  `nombreArchivo` varchar(255) DEFAULT NULL,
  `rutaArchivo` varchar(255) DEFAULT NULL,
  `mensaje_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `adjunto`
--

INSERT INTO `adjunto` (`id`, `nombreArchivo`, `rutaArchivo`, `mensaje_id`) VALUES
(1, 'WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 'uploads/correos/20/1765055361733_WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 20),
(2, 'WhatsApp Image 2025-11-18 at 6.33.55 PM.jpeg', 'uploads/correos/24/1765059148896_WhatsApp Image 2025-11-18 at 6.33.55 PM.jpeg', 24),
(3, 'MY. AYALA INFORME SLP. HERNANDEZ RODELO .docx', 'uploads/correos/26/1765059440445_MY. AYALA INFORME SLP. HERNANDEZ RODELO .docx', 26),
(4, 'IMG_3642.jpeg', 'uploads\\correos\\28\\1765059663922_IMG_3642.jpeg', 28),
(5, 'WhatsApp Image 2025-11-18 at 6.33.55 PM.jpeg', 'uploads/correos/30/1765061331229_WhatsApp Image 2025-11-18 at 6.33.55 PM.jpeg', 30),
(6, '1000069874.jpg', 'uploads\\correos\\31\\1765061508904_1000069874.jpg', 31),
(7, 'MALLAS DE TURNOS - CHATICO.xlsx', 'uploads\\correos\\33\\1765063858423_MALLAS DE TURNOS - CHATICO.xlsx', 33),
(8, 'MALLAS DE TURNOS - CHATICO.xlsx', 'uploads\\correos\\34\\1765064240860_MALLAS DE TURNOS - CHATICO.xlsx', 34),
(9, 'WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 'uploads/correos/38/1765067604189_WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 38),
(10, 'WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 'uploads/correos/39/1765067606072_WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 39),
(11, 'WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 'uploads/correos/40/1765067607852_WhatsApp Image 2025-11-18 at 6.38.55 PM.jpeg', 40),
(12, 'inventario_filtrado (1).xlsx', 'uploads/correos/49/1765069555426_inventario_filtrado (1).xlsx', 49),
(13, 'Captura de pantalla 2025-12-06 183801.png', 'uploads/correos/51/1765499099265_Captura de pantalla 2025-12-06 183801.png', 51),
(14, 'plantilla_inventario.xlsx', 'uploads/correos/66/1765587876511_plantilla_inventario.xlsx', 66),
(15, 'plantilla_inventario.xlsx', 'uploads/correos/67/1765587876581_plantilla_inventario.xlsx', 67),
(16, 'plantilla_inventario.xlsx', 'uploads/correos/68/1765587876615_plantilla_inventario.xlsx', 68);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ci_sessions`
--

CREATE TABLE `ci_sessions` (
  `id` varchar(128) NOT NULL,
  `ip_address` varchar(45) NOT NULL,
  `timestamp` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `data` blob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `ci_sessions`
--

INSERT INTO `ci_sessions` (`id`, `ip_address`, `timestamp`, `data`) VALUES
('ci_session:07741116f7e00029300a2ce89c2593db', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132383933373b5f63695f70726576696f75735f75726c7c733a36303a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f6c6f67696e223b),
('ci_session:17124b147fd165a65b37f15a681a0152', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133313430313b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f64617368626f617264223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:186e9f4b29332cf6933e207b889070d1', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133383131383b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:293a7c86b1987575cdb13f37361c0059', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132383431303b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2231223b733a363a224e6f6d627265223b733a353a2277656e6479223b733a363a22436f7272656f223b733a31353a2277656e647940676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b4e3b7d),
('ci_session:2a44c5ac2d1d589c28bd2be04310dff3', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323838393735353b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73742f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f726f6c65732f6564697461722f32223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2231223b733a363a224e6f6d627265223b733a353a2277656e6479223b733a363a22436f7272656f223b733a31353a2277656e647940676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b4e3b7d),
('ci_session:35788648de77aec36f9fc88ee3a3c444', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323839313033353b5f63695f70726576696f75735f75726c7c733a36303a22687474703a2f2f6c6f63616c686f73742f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7472616e73706f727465223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2231223b733a363a224e6f6d627265223b733a353a2277656e6479223b733a363a22436f7272656f223b733a31353a2277656e647940676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b4e3b7d),
('ci_session:36f0025029799de536bc32b3c2debd0d', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133343630303b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:40ee6ada7e406cd15a804a3514d82916', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323136383334383b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f6e6f76656461646573223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d737563636573737c733a32393a224e6f76656461642063726561646120636f7272656374616d656e74652e223b5f5f63695f766172737c613a313a7b733a373a2273756363657373223b733a333a226f6c64223b7d),
('ci_session:50941b26181b2671bb8be0c253a11121', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133393033373b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:5effb598d09fafb239a94d5b7da50b81', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323136383832353b5f63695f70726576696f75735f75726c7c733a36353a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f696e76656e746172696f223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2235223b733a363a224e6f6d627265223b733a383a226665726e616e646f223b733a363a22436f7272656f223b733a31383a226665726e616e646f40676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2232223b7d),
('ci_session:62c9aa4c3e5ae7a3ac5ba2c1bc903b84', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133393033373b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:689daee33f495c5fce1bd668e30a021a', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133333133303b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:6dac5840416252c1301490e45dba9911', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133333534353b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:78e2abc30c0a0f4135848eb5cbc10037', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133383536353b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:8669baa5b82d2c63fa82218df77fd318', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132383035303b5f63695f70726576696f75735f75726c7c733a36393a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f732f6372656172223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2231223b733a363a224e6f6d627265223b733a353a2277656e6479223b733a363a22436f7272656f223b733a31353a2277656e647940676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b4e3b7d),
('ci_session:8bc576a776df30561366a2ee2ce0b44e', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132363838363b),
('ci_session:a011943a367637144e25c8a04d46b3e0', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132393830383b5f63695f70726576696f75735f75726c7c733a36333a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:abc046377cac7466fd748c58f4edc286', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323136373636353b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7665686963756c6f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d737563636573737c733a32393a22566568c3ad63756c6f2063726561646f20657869746f73616d656e7465223b5f5f63695f766172737c613a313a7b733a373a2273756363657373223b733a333a226f6c64223b7d),
('ci_session:ae141e2a1816c32b9477fc5f1231a149', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323136373335393b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f64617368626f617264223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:bcc5168988180e55512211df60f5493d', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323136383832353b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7665686963756c6f73223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2235223b733a363a224e6f6d627265223b733a383a226665726e616e646f223b733a363a22436f7272656f223b733a31383a226665726e616e646f40676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2232223b7d),
('ci_session:cc7675d18d9efd54c7af02fe8726e3c2', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323133343237383b5f63695f70726576696f75735f75726c7c733a36393a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f7573756172696f732f6372656172223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:d57b1e314be08c9c1318850ca672a8b6', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132373734333b5f63695f70726576696f75735f75726c7c733a36343a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f64617368626f617264223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2231223b733a363a224e6f6d627265223b733a353a2277656e6479223b733a363a22436f7272656f223b733a31353a2277656e647940676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b4e3b7d),
('ci_session:e09a88e1e755954bf7afc10293b3b033', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323136383031303b5f63695f70726576696f75735f75726c7c733a37303a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f6e6f766564616465732f6372656172223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2233223b733a363a224e6f6d627265223b733a343a2272616661223b733a363a22436f7272656f223b733a31393a22727361656e7a33323640676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b733a313a2231223b7d),
('ci_session:f2f2f81d83bee5093f8b7d8d27ec13ff', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323839313033353b5f63695f70726576696f75735f75726c7c733a35393a22687474703a2f2f6c6f63616c686f73742f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f64617368626f617264223b7573756172696f7c613a353a7b733a393a2269645573756172696f223b733a313a2231223b733a363a224e6f6d627265223b733a353a2277656e6479223b733a363a22436f7272656f223b733a31353a2277656e647940676d61696c2e636f6d223b733a31313a22436f6e7472617365c3b161223b733a343a2231323334223b733a393a22526f6c5f6964526f6c223b4e3b7d),
('ci_session:f8b4acbe32658552d258a20b37262d86', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735333035303135363b5f63695f70726576696f75735f75726c7c733a35353a22687474703a2f2f6c6f63616c686f73742f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f6c6f67696e223b),
('ci_session:ff3f096d7110bbf218d55579bbc54545', '::1', 4294967295, 0x5f5f63695f6c6173745f726567656e65726174657c693a313735323132373434303b5f63695f70726576696f75735f75726c7c733a36303a22687474703a2f2f6c6f63616c686f73743a383038312f6175746f5f636865636b5f6c6973742f7075626c69632f696e6465782e7068702f6c6f67696e223b6572726f727c733a33323a224e6f6d627265206f20636f6e7472617365c3b16120696e636f72726563746f73223b5f5f63695f766172737c613a313a7b733a353a226572726f72223b733a333a226f6c64223b7d);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `concesionario`
--

CREATE TABLE `concesionario` (
  `idConcesionario` int(11) NOT NULL,
  `Nombre` varchar(60) DEFAULT NULL,
  `Ubicacion` varchar(60) DEFAULT NULL,
  `Ciudad` varchar(40) DEFAULT NULL,
  `Direccion` varchar(60) DEFAULT NULL,
  `Contacto` varchar(60) DEFAULT NULL,
  `Telefono` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `conductor`
--

CREATE TABLE `conductor` (
  `id_conductor` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `licencia` varchar(50) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `vehiculo_id` int(11) DEFAULT NULL,
  `username` varchar(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `conductor`
--

INSERT INTO `conductor` (`id_conductor`, `nombre`, `licencia`, `telefono`, `vehiculo_id`, `username`) VALUES
(2, 'David', NULL, NULL, NULL, 'david@gmail.com'),
(3, 'jairo', NULL, NULL, NULL, 'jairo@gmail.com'),
(4, 'jhosman', 'PENDIENTE', 'SIN REGISTRO', NULL, 'jhosman@gmail.com');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `correciones`
--

CREATE TABLE `correciones` (
  `idCorreciones` int(11) NOT NULL,
  `FechaCorreccion` datetime DEFAULT NULL,
  `Proceso` varchar(100) DEFAULT NULL,
  `Responsable` varchar(60) DEFAULT NULL,
  `Observaciones` text DEFAULT NULL,
  `EvidenciaEntrega` blob DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `envio`
--

CREATE TABLE `envio` (
  `id` bigint(20) NOT NULL,
  `destino` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `eta` varchar(255) DEFAULT NULL,
  `origen` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario`
--

CREATE TABLE `inventario` (
  `IdInventario` int(11) NOT NULL,
  `Chasis` varchar(255) DEFAULT NULL,
  `Marca` varchar(255) DEFAULT NULL,
  `Modelo` varchar(255) DEFAULT NULL,
  `Anio` int(11) DEFAULT NULL,
  `Color` varchar(255) DEFAULT NULL,
  `Motor` varchar(255) DEFAULT NULL,
  `UbicacionActual` varchar(255) DEFAULT NULL,
  `EstadoLogistico` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `inventario`
--

INSERT INTO `inventario` (`IdInventario`, `Chasis`, `Marca`, `Modelo`, `Anio`, `Color`, `Motor`, `UbicacionActual`, `EstadoLogistico`, `activo`) VALUES
(8, NULL, 'toyota', 'camioneta', 2025, NULL, NULL, 'Bogotá', NULL, 0),
(9, '147258', 'chevrolet', 'camioneta', 2025, 'gris', 'v8', 'Bogotá', 'espera', 0),
(10, NULL, 'mazda', '323', 1999, NULL, NULL, 'Bogotá', NULL, 0),
(11, 'CHS-TYT-001', 'Toyota', 'Corolla', 2022, 'Blanco', '1.8 Gasolina', 'Bogotá – Bodega Norte', 'Disponible', 1),
(12, 'CHS-CHV-002', 'Chevrolet', 'Onix', 2021, 'Gris', '1.0 Turbo', 'Medellín – Sucursal', 'En tránsito', 1),
(13, 'CHS-FRD-003', 'Ford', 'Ranger', 2023, 'Azul', '2.0 Diésel', 'Cali – Puerto Seco', 'En inspección', 1),
(14, 'CHS-HND-004', 'Honda', 'Civic', 2020, 'Negro', '2.0 Gasolina', 'Barranquilla – Patio', 'Entregado', 1),
(15, 'CHS-BMW-005', 'BMW', 'X3', 2024, 'Plateado', '2.0 Turbo', 'Cartagena – Zona Franca', 'Pendiente envío', 1),
(16, 'CHS-TYT-006', 'Toyota', 'Hilux', 2023, 'Rojo', '2.8 Diésel', 'Villavicencio – Patio', 'En tránsito', 1),
(17, 'CHS-NIS-007', 'Nissan', 'Versa', 2021, 'Blanco', '1.6 Gasolina', 'Bogotá – Bodega Central', 'Disponible', 1),
(18, 'CHS-KIA-008', 'Kia', 'Sportage', 2022, 'Gris', '2.0 Gasolina', 'Bucaramanga – Sucursal', 'En inspección', 1),
(19, 'CHS-HYU-009', 'Hyundai', 'Tucson', 2024, 'Negro', '2.0 Híbrido', 'Medellín – Zona Franca', 'Pendiente envío', 1),
(20, 'CHS-MZD-010', 'Mazda', 'CX-5', 2023, 'Azul', '2.5 Gasolina', 'Cali – Bodega Sur', 'Disponible', 1),
(21, 'CHS-AUD-011', 'Audi', 'A4', 2022, 'Plateado', '2.0 Turbo', 'Bogotá – Concesionario', 'Entregado', 1),
(22, 'CHS-MRC-012', 'Mercedes-Benz', 'GLC 300', 2024, 'Blanco', '2.0 Turbo', 'Cartagena – Puerto', 'En tránsito', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `mensaje`
--

CREATE TABLE `mensaje` (
  `id` bigint(20) NOT NULL,
  `asunto` varchar(255) DEFAULT NULL,
  `carpeta` varchar(30) DEFAULT NULL,
  `contenido` text DEFAULT NULL,
  `eliminado` bit(1) DEFAULT NULL,
  `fechaEnvio` datetime(6) DEFAULT NULL,
  `id_padre` bigint(20) DEFAULT NULL,
  `leido` bit(1) NOT NULL,
  `destinatario_id` bigint(20) DEFAULT NULL,
  `remitente_id` int(11) DEFAULT NULL,
  `destinatario_externo` varchar(255) DEFAULT NULL,
  `destinatarioExterno` varchar(255) DEFAULT NULL,
  `idPadre` bigint(20) DEFAULT NULL,
  `eliminadoPermanente` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `mensaje`
--

INSERT INTO `mensaje` (`id`, `asunto`, `carpeta`, `contenido`, `eliminado`, `fechaEnvio`, `id_padre`, `leido`, `destinatario_id`, `remitente_id`, `destinatario_externo`, `destinatarioExterno`, `idPadre`, `eliminadoPermanente`) VALUES
(1, 'hola', 'sent', 'a', b'0', '2025-12-06 00:53:12.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(2, 'prueba entre usuarios', 'sent', 'a', b'0', '2025-12-06 01:12:45.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(3, 'hola', 'sent', 'a', b'0', '2025-12-06 01:13:09.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(4, 'Correo de prueba', 'sent', 'hola', b'0', '2025-12-06 01:14:48.000000', NULL, b'1', NULL, 1, 'leymaaan222@gmail.com', NULL, NULL, b'0'),
(5, 'hola', 'sent', 'hola', b'0', '2025-12-06 01:16:32.000000', NULL, b'1', NULL, 1, 'leymaaan222@gmail.com', NULL, NULL, b'0'),
(6, 'hola', 'sent', '', b'0', '2025-12-06 01:17:01.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(7, 'hola', 'sent', 'a', b'0', '2025-12-06 01:17:22.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(8, 'prueba entre usuarios', 'sent', 'HOLA', b'0', '2025-12-06 01:17:42.000000', NULL, b'1', NULL, 1, 'leymaaan222@gmail.com', NULL, NULL, b'0'),
(9, '', 'sent', '', b'0', '2025-12-06 01:19:21.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(10, 'Masivo', 'sent', 'jalkdas', b'0', '2025-12-06 01:19:51.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(11, 'Masivo', 'sent', 'jalkdas', b'0', '2025-12-06 01:19:53.000000', NULL, b'1', NULL, 1, 'leymaaan222@gmail.com', NULL, NULL, b'0'),
(12, 'hola', 'sent', 'a', b'0', '2025-12-06 01:33:04.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(13, 'jol', 'sent', '', b'0', '2025-12-06 01:34:44.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(14, 'prueba entre usuarios', 'sent', '', b'0', '2025-12-06 01:42:13.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(15, 'hola', 'sent', 'responda', b'0', '2025-12-06 01:43:03.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(16, 'prueba entre usuarios', 'sent', '', b'0', '2025-12-06 02:20:50.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(17, 'archivo', 'sent', 'arhivo¡', b'0', '2025-12-06 02:21:45.000000', NULL, b'1', NULL, 1, 'davidjhosmanq12@gmail.com', NULL, NULL, b'0'),
(18, 'jola', 'sent', 'asasda', b'0', '2025-12-06 21:01:47.000000', NULL, b'1', NULL, 1, NULL, 'davidjhosmanq12@gmail.com', NULL, b'0'),
(19, 'Re: jola', 'inbox', 'hola', b'1', '2025-12-06 21:02:37.000000', NULL, b'1', 1, 1, NULL, NULL, 18, b'1'),
(20, 'prueba entre usuarios', 'sent', 'a', b'0', '2025-12-06 21:09:18.000000', NULL, b'1', NULL, 1, NULL, 'davidjhosmanq12@gmail.com', NULL, b'0'),
(21, 'Re: prueba entre usuarios', 'inbox', 'Hola\r\n\r\nEl El sáb, dic 6, 2025 a la(s) 4:09 p.m., <autochecklistoficial@gmail.com>\r\nescribió:\r\n\r\n> a\r\n', b'0', '2025-12-06 21:09:43.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'0'),
(22, 'Prueba', 'inbox', '', b'1', '2025-12-06 21:44:24.000000', NULL, b'1', 1, 8, NULL, NULL, NULL, b'0'),
(23, 'Delivery Status Notification (Failure)', 'inbox', '', b'0', '2025-12-06 21:44:26.000000', NULL, b'0', NULL, 22, NULL, NULL, NULL, b'0'),
(24, 'Prueba de chat', 'sent', 'prueba', b'0', '2025-12-06 22:12:26.000000', NULL, b'0', NULL, 8, NULL, 'leymaaan222@gmail.com', NULL, b'0'),
(25, 'prueba entre usuarios', 'inbox', 'a', b'0', '2025-12-06 22:16:05.000000', NULL, b'1', 21, 1, NULL, NULL, NULL, b'0'),
(26, 'Prueba de envio con archivo adjunto', 'sent', 'Hola, puedes responder si te llego? ', b'0', '2025-12-06 22:17:16.000000', NULL, b'1', NULL, 1, NULL, 'leymaaan222@gmail.com', NULL, b'0'),
(27, 'Hola', 'inbox', 'hola', b'0', '2025-12-06 22:20:59.000000', NULL, b'1', 21, 1, NULL, NULL, NULL, b'0'),
(28, 'Re: prueba entre usuarios', 'inbox', '', b'0', '2025-12-06 22:20:19.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'0'),
(29, 'Re: Hola', 'inbox', 'Si llegas\r\n\r\nEl El sáb, dic 6, 2025 a la(s) 5:21 p.m., <autochecklistoficial@gmail.com>\r\nescribió:\r\n\r\n> hola\r\n', b'1', '2025-12-06 22:21:08.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'1'),
(30, 'prueba Jhosman', 'sent', 'Hola responde el correo', b'0', '2025-12-06 22:48:49.000000', NULL, b'1', NULL, 1, NULL, 'jquimbayroa@gmail.com', NULL, b'0'),
(31, 'Re: prueba Jhosman', 'inbox', '', b'1', '2025-12-06 22:51:30.000000', NULL, b'1', 1, 23, NULL, NULL, NULL, b'1'),
(32, 'Correo para ver si llega', 'inbox', 'Xd\r\n', b'1', '2025-12-06 23:25:11.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'1'),
(33, 'Prueba nuevamente', 'inbox', '', b'1', '2025-12-06 23:29:53.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'1'),
(34, 'Esto es una prueba ptra vdz', 'inbox', '', b'1', '2025-12-06 23:36:46.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'1'),
(35, 'rafaga', 'inbox', 'hOLA', b'0', '2025-12-06 23:51:55.000000', NULL, b'0', 21, 8, NULL, NULL, NULL, b'0'),
(36, 'rafaga', 'inbox', 'hOLA', b'0', '2025-12-06 23:51:57.000000', NULL, b'0', 21, 8, NULL, NULL, NULL, b'0'),
(37, 'rafaga', 'inbox', 'hOLA', b'0', '2025-12-06 23:51:59.000000', NULL, b'0', 21, 8, NULL, NULL, NULL, b'0'),
(38, 'Prueba Jhosman Quimbay ', 'sent', 'Hola Esto es una prueba', b'0', '2025-12-07 00:33:21.000000', NULL, b'0', NULL, 1, NULL, 'p.gutierrez.dnc@gmail.com', NULL, b'0'),
(39, 'Prueba Jhosman Quimbay ', 'sent', 'Hola Esto es una prueba', b'0', '2025-12-07 00:33:24.000000', NULL, b'0', NULL, 1, NULL, 'p.gutierrez.dnc@gmail.com', NULL, b'0'),
(40, 'Prueba Jhosman Quimbay ', 'sent', 'Hola Esto es una prueba', b'0', '2025-12-07 00:33:26.000000', NULL, b'1', NULL, 1, NULL, 'p.gutierrez.dnc@gmail.com', NULL, b'0'),
(41, 'hola', 'inbox', 'Prueba', b'1', '2025-12-07 01:03:02.000000', NULL, b'1', 1, 17, NULL, NULL, NULL, b'1'),
(42, 'hola', 'inbox', 'Prueba', b'1', '2025-12-07 01:03:04.000000', NULL, b'0', 1, 17, NULL, NULL, NULL, b'1'),
(43, 'hola', 'inbox', 'Prueba', b'1', '2025-12-07 01:03:06.000000', NULL, b'1', 1, 17, NULL, NULL, NULL, b'1'),
(44, 'Delivery Status Notification (Failure)', 'inbox', '', b'0', '2025-12-07 01:03:03.000000', NULL, b'0', NULL, 22, NULL, NULL, NULL, b'0'),
(45, 'Delivery Status Notification (Failure)', 'inbox', '', b'0', '2025-12-07 01:03:05.000000', NULL, b'0', NULL, 22, NULL, NULL, NULL, b'0'),
(46, 'Delivery Status Notification (Failure)', 'inbox', '', b'0', '2025-12-07 01:03:07.000000', NULL, b'0', NULL, 22, NULL, NULL, NULL, b'0'),
(47, 'Re: hola', 'inbox', 'Hola perrita', b'0', '2025-12-07 01:03:59.000000', NULL, b'0', 17, 1, NULL, NULL, 43, b'0'),
(48, 'Delivery Status Notification (Failure)', 'inbox', '', b'0', '2025-12-07 01:04:01.000000', NULL, b'0', NULL, 22, NULL, NULL, NULL, b'0'),
(49, 'Correo de Prueba Para las mas Perrita', 'sent', 'Esto es una prueba', b'0', '2025-12-07 01:05:53.000000', NULL, b'1', NULL, 1, NULL, 'stngmz1@gmail.com', NULL, b'0'),
(50, 'Re: Correo de Prueba Para las mas Perrita', 'inbox', 'Eres el mejor\r\n\r\nEl sáb, 6 de dic de 2025, 20:05, <autochecklistoficial@gmail.com> escribió:\r\n\r\n> Esto es una prueba\r\n', b'1', '2025-12-07 01:07:07.000000', NULL, b'1', 1, 24, NULL, NULL, NULL, b'1'),
(51, 'hola', 'sent', 'Prueba 2', b'0', '2025-12-12 00:24:55.000000', NULL, b'1', NULL, 1, NULL, 'leymaaan222@gmail.com', NULL, b'0'),
(52, 'hola', 'sent', 'hola', b'0', '2025-12-12 00:26:52.000000', NULL, b'1', NULL, 1, NULL, 'leymaaan222@gmail.com', NULL, b'0'),
(53, 'hola', 'inbox', 'responde\r\n', b'0', '2025-12-12 00:47:25.000000', NULL, b'0', 21, 1, NULL, NULL, NULL, b'0'),
(54, 'Re: hola', 'inbox', 'Hola\r\n\r\nEl jue, 11 de dic de 2025, 19:24, <autochecklistoficial@gmail.com> escribió:\r\n\r\n> Prueba 2\r\n', b'0', '2025-12-12 00:25:24.000000', NULL, b'1', 1, 25, NULL, NULL, NULL, b'0'),
(55, 'Re: hola', 'inbox', '👋👋👋\r\n\r\nEl jue, 11 de dic de 2025, 19:26, <autochecklistoficial@gmail.com> escribió:\r\n\r\n> hola\r\n', b'0', '2025-12-12 00:27:17.000000', NULL, b'1', 1, 25, NULL, NULL, NULL, b'0'),
(56, 'Re: hola', 'inbox', 'Hola\r\n\r\nEl El jue, dic 11, 2025 a la(s) 7:47 p.m., <autochecklistoficial@gmail.com>\r\nescribió:\r\n\r\n> responde\r\n>\r\n', b'0', '2025-12-12 00:48:13.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'0'),
(57, 'Alerta de seguridad', 'inbox', '[image: Google]\r\nSe ha quitado una contraseña de aplicación que usas para iniciar sesión\r\n\r\n\r\nautochecklistoficial@gmail.com\r\nSi no has quitado tú esa contraseña, alguien podría estar usando tu cuenta.\r\nRevisa y protege tu cuenta ahora.\r\nComprobar actividad\r\n<https://accounts.google.com/AccountChooser?Email=autochecklistoficial@gmail.com&continue=https://myaccount.google.com/alert/nt/1765502006512?rfn%3D269%26rfnc%3D1%26eid%3D5820374100528057425%26et%3D0>\r\nTambién puedes ver toda la actividad de seguridad en\r\nhttps://myaccount.google.com/notifications\r\nTe hemos enviado este correo electrónico para informarte de cambios\r\nimportantes en tu cuenta y en los servicios de Google.\r\n© 2025 Google LLC, 1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\r\n', b'0', '2025-12-12 01:13:26.000000', NULL, b'0', NULL, 26, NULL, NULL, NULL, b'0'),
(58, 'Alerta de seguridad', 'inbox', '[image: Google]\r\nSe ha creado una contraseña de aplicación para iniciar sesión en tu cuenta\r\n\r\n\r\nautochecklistoficial@gmail.com\r\nSi no has generado tú esa contraseña para AutoCheckList, alguien podría\r\nestar usando tu cuenta. Revisa y protege tu cuenta ahora.\r\nComprobar actividad\r\n<https://accounts.google.com/AccountChooser?Email=autochecklistoficial@gmail.com&continue=https://myaccount.google.com/alert/nt/1765502012897?rfn%3D20%26rfnc%3D1%26eid%3D-7153197127839995370%26et%3D0>\r\nTambién puedes ver toda la actividad de seguridad en\r\nhttps://myaccount.google.com/notifications\r\nTe hemos enviado este correo electrónico para informarte de cambios\r\nimportantes en tu cuenta y en los servicios de Google.\r\n© 2025 Google LLC, 1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\r\n', b'0', '2025-12-12 01:13:32.000000', NULL, b'0', NULL, 26, NULL, NULL, NULL, b'0'),
(59, 'RESPONDA', 'inbox', 'HOLA', b'0', '2025-12-13 00:08:15.000000', NULL, b'0', 24, 1, NULL, NULL, NULL, b'0'),
(60, 'hola', 'inbox', 'a', b'0', '2025-12-13 00:08:59.000000', NULL, b'0', 21, 1, NULL, NULL, NULL, b'0'),
(61, 'hola', 'inbox', 'a', b'0', '2025-12-13 00:09:01.000000', NULL, b'0', 21, 1, NULL, NULL, NULL, b'0'),
(62, 'Re: RESPONDA', 'inbox', 'Respondo\r\n\r\nEl vie, 12 de dic de 2025, 19:08, <autochecklistoficial@gmail.com> escribió:\r\n\r\n> HOLA\r\n', b'0', '2025-12-13 00:08:50.000000', NULL, b'1', 1, 24, NULL, NULL, NULL, b'0'),
(63, 'Hola', 'inbox', '\r\n', b'0', '2025-12-13 00:14:22.000000', NULL, b'1', 1, 21, NULL, NULL, NULL, b'0'),
(64, 'Alerta de seguridad', 'inbox', '[image: Google]\r\nSe ha quitado una contraseña de aplicación que usas para iniciar sesión\r\n\r\n\r\nautochecklistoficial@gmail.com\r\nSi no has quitado tú esa contraseña, alguien podría estar usando tu cuenta.\r\nRevisa y protege tu cuenta ahora.\r\nComprobar actividad\r\n<https://accounts.google.com/AccountChooser?Email=autochecklistoficial@gmail.com&continue=https://myaccount.google.com/alert/nt/1765585131973?rfn%3D269%26rfnc%3D1%26eid%3D-743673944551568165%26et%3D0>\r\nTambién puedes ver toda la actividad de seguridad en\r\nhttps://myaccount.google.com/notifications\r\nTe hemos enviado este correo electrónico para informarte de cambios\r\nimportantes en tu cuenta y en los servicios de Google.\r\n© 2025 Google LLC, 1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\r\n', b'0', '2025-12-13 00:18:51.000000', NULL, b'0', NULL, 26, NULL, NULL, NULL, b'0'),
(65, 'Alerta de seguridad', 'inbox', '[image: Google]\r\nSe ha creado una contraseña de aplicación para iniciar sesión en tu cuenta\r\n\r\n\r\nautochecklistoficial@gmail.com\r\nSi no has generado tú esa contraseña para AutoCheckList, alguien podría\r\nestar usando tu cuenta. Revisa y protege tu cuenta ahora.\r\nComprobar actividad\r\n<https://accounts.google.com/AccountChooser?Email=autochecklistoficial@gmail.com&continue=https://myaccount.google.com/alert/nt/1765585140696?rfn%3D20%26rfnc%3D1%26eid%3D7406453136504186587%26et%3D0>\r\nTambién puedes ver toda la actividad de seguridad en\r\nhttps://myaccount.google.com/notifications\r\nTe hemos enviado este correo electrónico para informarte de cambios\r\nimportantes en tu cuenta y en los servicios de Google.\r\n© 2025 Google LLC, 1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\r\n', b'0', '2025-12-13 00:19:00.000000', NULL, b'0', NULL, 26, NULL, NULL, NULL, b'0'),
(66, 'Prueba Correo', 'sent', 'Hola esto es una prueba 12/12/2025', b'0', '2025-12-13 01:04:33.000000', NULL, b'0', NULL, 1, NULL, 'macabrerasena@gmail.com', NULL, b'0'),
(67, 'Prueba Correo', 'sent', 'Hola esto es una prueba 12/12/2025', b'0', '2025-12-13 01:04:36.000000', NULL, b'0', NULL, 1, NULL, 'ing.ymahecha2025@gmail.com', NULL, b'0'),
(68, 'Prueba Correo', 'inbox', 'Hola esto es una prueba 12/12/2025', b'0', '2025-12-13 01:04:36.000000', NULL, b'1', 8, 1, NULL, NULL, NULL, b'0');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notificacion`
--

CREATE TABLE `notificacion` (
  `id_notificacion` bigint(20) NOT NULL,
  `titulo` varchar(100) NOT NULL,
  `mensaje` text NOT NULL,
  `fecha` timestamp NOT NULL DEFAULT current_timestamp(),
  `conductor_id` bigint(20) DEFAULT NULL,
  `leida` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `notificacion`
--

INSERT INTO `notificacion` (`id_notificacion`, `titulo`, `mensaje`, `fecha`, `conductor_id`, `leida`) VALUES
(1, 'hl', 'sad', '2025-09-28 22:34:03', NULL, 0),
(2, 'prueba', 'hola esto es una prueba', '2025-09-29 03:34:36', 2, 1),
(3, 'migajera', 'Daniela se le corre el shampoo y le faltan unos cuantos tornillos', '2025-09-29 06:40:12', 3, 0),
(4, 'aviso', 'notificado', '2025-09-30 04:49:52', 4, 1),
(5, 'rafael', 'asklmflaskfmañ', '2025-09-30 05:21:37', 3, 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `novedad`
--

CREATE TABLE `novedad` (
  `id_novedad` bigint(20) NOT NULL,
  `descripcion` text NOT NULL,
  `fecha_reporte` varchar(50) DEFAULT NULL,
  `conductor_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `novedades`
--

CREATE TABLE `novedades` (
  `idNovedades` bigint(20) NOT NULL,
  `TipoNovedad` varchar(100) NOT NULL,
  `Descripcion` text DEFAULT NULL,
  `Evidencia` varchar(255) DEFAULT NULL,
  `Estado` varchar(50) NOT NULL,
  `Id_Usuario` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `novedades`
--

INSERT INTO `novedades` (`idNovedades`, `TipoNovedad`, `Descripcion`, `Evidencia`, `Estado`, `Id_Usuario`) VALUES
(1, 'vehiculo imcompleto', 'vehiculo con piezas faltantes', '1752168010_85fa934e39208b0545be.png', 'en bodega', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedido`
--

CREATE TABLE `pedido` (
  `id_pedido` bigint(20) NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `conductor_id` bigint(20) DEFAULT NULL,
  `vehiculo_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `permiso`
--

CREATE TABLE `permiso` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol`
--

CREATE TABLE `rol` (
  `idRol` int(11) NOT NULL,
  `Cargo` varchar(30) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `rol`
--

INSERT INTO `rol` (`idRol`, `Cargo`, `nombre`) VALUES
(1, 'Administrador', 'ADMIN'),
(2, 'Conductor', 'CONDUCTOR'),
(3, 'Gerente ', 'GERENTE'),
(4, NULL, 'CONDUCTOR');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ruta`
--

CREATE TABLE `ruta` (
  `id_ruta` bigint(20) NOT NULL,
  `origen` varchar(150) NOT NULL,
  `destino` varchar(150) NOT NULL,
  `horario` varchar(100) DEFAULT NULL,
  `estado` varchar(50) DEFAULT 'pendiente',
  `conductor_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `transporte`
--

CREATE TABLE `transporte` (
  `idTransporte` int(11) NOT NULL,
  `idVehiculo` int(11) DEFAULT NULL,
  `FechaAsignacion` datetime DEFAULT NULL,
  `FechaEntrega` datetime DEFAULT NULL,
  `EstadoEntrega` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `transporte`
--

INSERT INTO `transporte` (`idTransporte`, `idVehiculo`, `FechaAsignacion`, `FechaEntrega`, `EstadoEntrega`) VALUES
(1, 1, '2025-07-10 00:00:00', '2025-07-23 00:00:00', 'en proceso');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id_usuario` int(11) NOT NULL,
  `Nombre` varchar(100) NOT NULL,
  `Correo` varchar(150) NOT NULL,
  `contrasena` varchar(255) DEFAULT NULL,
  `Rol_idRol` int(11) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id_usuario`, `Nombre`, `Correo`, `contrasena`, `Rol_idRol`, `activo`) VALUES
(1, 'wendy', 'wendy@gmail.com', '1234', 1, 1),
(2, 'Gerente General', 'gerente@autochecklist.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lKOKMjQNaG1wg4x6W', 2, 1),
(3, 'rafa', 'rsaenz326@gmail.com', '123', 1, 1),
(4, 'ricardo', 'ricardo@gmail.com', '1234', 1, 1),
(8, 'stiven', 'stiven@gmail.com', '1234', 3, 1),
(17, 'David', 'david@gmail.com', '1234', 2, 1),
(18, 'jairo', 'jairo@gmail.com', '1234', 2, 1),
(19, 'jhosman', 'jhosman@gmail.com', '1234', 2, 1),
(20, 'Admin Test', 'admin@test.com', 'password123', 1, 1),
(21, 'jhosman quimbay', 'davidjhosmanq12@gmail.com', NULL, NULL, 0),
(22, 'Mail Delivery Subsystem', 'mailer-daemon@googlemail.com', NULL, NULL, 0),
(23, 'Jhon QUIMBAY', 'jquimbayroa@gmail.com', NULL, NULL, 0),
(24, 'Stiven Arellano', 'stngmz1@gmail.com', NULL, NULL, 0),
(25, 'Andres Maurio Ayala Leyva', 'leymaaan222@gmail.com', NULL, NULL, 0),
(26, 'Google', 'no-reply@accounts.google.com', NULL, NULL, 0),
(27, 'jhosman', 'davidr@gmail.com', '1234', 3, 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `vehiculo`
--

CREATE TABLE `vehiculo` (
  `idVehiculo` int(11) NOT NULL,
  `Chasis` varchar(100) NOT NULL,
  `Inventario_IdInventario` int(11) DEFAULT NULL,
  `Modelo` varchar(100) NOT NULL,
  `activo` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `vehiculo`
--

INSERT INTO `vehiculo` (`idVehiculo`, `Chasis`, `Inventario_IdInventario`, `Modelo`, `activo`) VALUES
(5, '8054568888', NULL, 'toyota', b'0'),
(7, '294010', NULL, 'toyota', b'0');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `adjunto`
--
ALTER TABLE `adjunto`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKhf1hlkivl4nw4yq28a5ps8sxa` (`mensaje_id`);

--
-- Indices de la tabla `ci_sessions`
--
ALTER TABLE `ci_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ci_sessions_timestamp` (`timestamp`);

--
-- Indices de la tabla `concesionario`
--
ALTER TABLE `concesionario`
  ADD PRIMARY KEY (`idConcesionario`);

--
-- Indices de la tabla `conductor`
--
ALTER TABLE `conductor`
  ADD PRIMARY KEY (`id_conductor`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `fk_conductor_vehiculo` (`vehiculo_id`);

--
-- Indices de la tabla `correciones`
--
ALTER TABLE `correciones`
  ADD PRIMARY KEY (`idCorreciones`);

--
-- Indices de la tabla `envio`
--
ALTER TABLE `envio`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `inventario`
--
ALTER TABLE `inventario`
  ADD PRIMARY KEY (`IdInventario`),
  ADD UNIQUE KEY `Chasis` (`Chasis`);

--
-- Indices de la tabla `mensaje`
--
ALTER TABLE `mensaje`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_mensaje_remitente` (`remitente_id`),
  ADD KEY `FK_mensaje_destinatario` (`destinatario_id`);

--
-- Indices de la tabla `notificacion`
--
ALTER TABLE `notificacion`
  ADD PRIMARY KEY (`id_notificacion`),
  ADD KEY `fk_notificacion_conductor` (`conductor_id`);

--
-- Indices de la tabla `novedad`
--
ALTER TABLE `novedad`
  ADD PRIMARY KEY (`id_novedad`),
  ADD KEY `fk_novedad_conductor` (`conductor_id`);

--
-- Indices de la tabla `novedades`
--
ALTER TABLE `novedades`
  ADD PRIMARY KEY (`idNovedades`),
  ADD KEY `IdUsuario` (`Id_Usuario`);

--
-- Indices de la tabla `pedido`
--
ALTER TABLE `pedido`
  ADD PRIMARY KEY (`id_pedido`),
  ADD KEY `conductor_id` (`conductor_id`),
  ADD KEY `vehiculo_id` (`vehiculo_id`);

--
-- Indices de la tabla `permiso`
--
ALTER TABLE `permiso`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `rol`
--
ALTER TABLE `rol`
  ADD PRIMARY KEY (`idRol`);

--
-- Indices de la tabla `ruta`
--
ALTER TABLE `ruta`
  ADD PRIMARY KEY (`id_ruta`),
  ADD KEY `fk_ruta_conductor` (`conductor_id`);

--
-- Indices de la tabla `transporte`
--
ALTER TABLE `transporte`
  ADD PRIMARY KEY (`idTransporte`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id_usuario`),
  ADD KEY `Rol_idRol` (`Rol_idRol`);

--
-- Indices de la tabla `vehiculo`
--
ALTER TABLE `vehiculo`
  ADD PRIMARY KEY (`idVehiculo`),
  ADD KEY `Inventario_IdInventario` (`Inventario_IdInventario`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `adjunto`
--
ALTER TABLE `adjunto`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT de la tabla `concesionario`
--
ALTER TABLE `concesionario`
  MODIFY `idConcesionario` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `conductor`
--
ALTER TABLE `conductor`
  MODIFY `id_conductor` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `correciones`
--
ALTER TABLE `correciones`
  MODIFY `idCorreciones` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `envio`
--
ALTER TABLE `envio`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `inventario`
--
ALTER TABLE `inventario`
  MODIFY `IdInventario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT de la tabla `mensaje`
--
ALTER TABLE `mensaje`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;

--
-- AUTO_INCREMENT de la tabla `notificacion`
--
ALTER TABLE `notificacion`
  MODIFY `id_notificacion` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `novedad`
--
ALTER TABLE `novedad`
  MODIFY `id_novedad` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `novedades`
--
ALTER TABLE `novedades`
  MODIFY `idNovedades` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `pedido`
--
ALTER TABLE `pedido`
  MODIFY `id_pedido` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `permiso`
--
ALTER TABLE `permiso`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `rol`
--
ALTER TABLE `rol`
  MODIFY `idRol` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `ruta`
--
ALTER TABLE `ruta`
  MODIFY `id_ruta` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `transporte`
--
ALTER TABLE `transporte`
  MODIFY `idTransporte` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT de la tabla `vehiculo`
--
ALTER TABLE `vehiculo`
  MODIFY `idVehiculo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `adjunto`
--
ALTER TABLE `adjunto`
  ADD CONSTRAINT `FKhf1hlkivl4nw4yq28a5ps8sxa` FOREIGN KEY (`mensaje_id`) REFERENCES `mensaje` (`id`);

--
-- Filtros para la tabla `conductor`
--
ALTER TABLE `conductor`
  ADD CONSTRAINT `fk_conductor_vehiculo` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`idVehiculo`);

--
-- Filtros para la tabla `mensaje`
--
ALTER TABLE `mensaje`
  ADD CONSTRAINT `FK_mensaje_remitente` FOREIGN KEY (`remitente_id`) REFERENCES `usuario` (`id_usuario`);

--
-- Filtros para la tabla `notificacion`
--
ALTER TABLE `notificacion`
  ADD CONSTRAINT `fk_notificacion_conductor` FOREIGN KEY (`conductor_id`) REFERENCES `conductor` (`id_conductor`);

--
-- Filtros para la tabla `novedad`
--
ALTER TABLE `novedad`
  ADD CONSTRAINT `fk_novedad_conductor` FOREIGN KEY (`conductor_id`) REFERENCES `conductor` (`id_conductor`);

--
-- Filtros para la tabla `novedades`
--
ALTER TABLE `novedades`
  ADD CONSTRAINT `novedades_ibfk_1` FOREIGN KEY (`Id_Usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `pedido`
--
ALTER TABLE `pedido`
  ADD CONSTRAINT `pedido_ibfk_1` FOREIGN KEY (`conductor_id`) REFERENCES `conductor` (`id_conductor`),
  ADD CONSTRAINT `pedido_ibfk_2` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`idVehiculo`);

--
-- Filtros para la tabla `ruta`
--
ALTER TABLE `ruta`
  ADD CONSTRAINT `fk_ruta_conductor` FOREIGN KEY (`conductor_id`) REFERENCES `conductor` (`id_conductor`);

--
-- Filtros para la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD CONSTRAINT `usuario_ibfk_1` FOREIGN KEY (`Rol_idRol`) REFERENCES `rol` (`idRol`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `vehiculo`
--
ALTER TABLE `vehiculo`
  ADD CONSTRAINT `vehiculo_ibfk_1` FOREIGN KEY (`Inventario_IdInventario`) REFERENCES `inventario` (`IdInventario`) ON DELETE SET NULL ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
