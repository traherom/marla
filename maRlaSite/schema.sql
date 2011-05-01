-- phpMyAdmin SQL Dump
-- version 3.3.10
-- http://www.phpmyadmin.net
--
-- Host: moreharts.db
-- Generation Time: May 01, 2011 at 02:45 PM
-- Server version: 5.0.92
-- PHP Version: 5.3.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: 'marla'
--
CREATE DATABASE marla DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE marla;

-- --------------------------------------------------------

--
-- Table structure for table 'errors'
--

CREATE TABLE IF NOT EXISTS `errors` (
  id int(10) unsigned NOT NULL auto_increment,
  report_date timestamp NOT NULL default CURRENT_TIMESTAMP,
  reporting_user varchar(50) default NULL,
  version int(10) unsigned NOT NULL COMMENT 'SVN revision number of reporting client',
  os varchar(100) default NULL,
  message text NOT NULL,
  stacktrace text NOT NULL,
  config text,
  problem mediumtext COMMENT 'Problem save xml',
  resolved tinyint(1) NOT NULL default '0' COMMENT 'Marks if report is handled',
  PRIMARY KEY  (id),
  KEY version (version),
  KEY `date` (report_date)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2223 ;

-- --------------------------------------------------------

--
-- Table structure for table 'users'
--

CREATE TABLE IF NOT EXISTS users (
  `user` varchar(254) NOT NULL,
  `password` varchar(254) NOT NULL,
  PRIMARY KEY  (`user`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
