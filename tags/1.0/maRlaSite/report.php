<?php
require_once('../lib/db.inc');

// Small protection against spam. Also used to check the sever is valid
if(@$_REQUEST['secret'] !== 'badsecurity')
	die('maRla');

// Check that relevant parameters set
if(isset($_REQUEST['msg']))
	$msg = $_REQUEST['msg'];
else
	die('no message set');
	
if(isset($_REQUEST['version']))
	$version = $_REQUEST['version'];
else
	die('no version set');
	
if(isset($_REQUEST['os']))
	$os = $_REQUEST['os'];
else
	$os = null;

if(isset($_REQUEST['user']))
	$user = $_REQUEST['user'];
else
	$user = null;
	
if(isset($_REQUEST['trace']))
	$trace = $_REQUEST['trace'];
else
	die('no stacktrace set');
	
if(isset($_REQUEST['problem']))
	$prob = $_REQUEST['problem'];
else
	$prob = null;
	
if(isset($_REQUEST['config']))
	$config = $_REQUEST['config'];
else
	$config = null;
	
// Stuff into database
$stmt = $db->prepare("INSERT INTO errors (version, os, reporting_user, message, stacktrace, problem, config) VALUES (?, ?, ?, ?, ?, ?, ?)");
if($stmt->execute(array($version, $os, $user, $msg, $trace, $prob, $config)))
	print('success');
else
	print('failed');
?>