<?php
require_once('../lib/db.inc');

// Small protection against spam
if(@$_REQUEST['secret'] !== 'badsecurity')
	die('bah');

// Check that relevant parameters set
if(isset($_REQUEST['msg']))
	$msg = $_REQUEST['msg'];
else
	die('no message set');
	
if(isset($_REQUEST['version']))
	$version = $_REQUEST['version'];
else
	die('no version set');
	
if(isset($_REQUEST['trace']))
	$trace = $_REQUEST['trace'];
else
	die('no stacktrace set');
	
if(isset($_REQUEST['problem']))
	$prob = $_REQUEST['problem'];
else
	$prob = null;
	
// Stuff into database
$stmt = $db->prepare("INSERT INTO errors (version, message, stacktrace, problem) VALUES (?, ?, ?, ?)");
if($stmt->execute(array($version, $msg, $trace, $prob)))
	print('success');
else
	print('failed');
?>