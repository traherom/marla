<?php
session_start();
if(!@$_SESSION['logged_in'])
{
	header('Location: login.php');
	die();
}

if(!isset($_REQUEST['id']))
{
	header('Location: error_list.php');
	die();
}

// Toggle resolved status?
require_once('../lib/db.inc');
if(isset($_REQUEST['resolve']))
{
	$stmt = $db->prepare('UPDATE errors SET resolved=1 XOR resolved WHERE id=?');
	$stmt->execute(array($_REQUEST['id']));
}

// Get the error
$stmt = $db->prepare('SELECT id, report_date, resolved, version, os, message, reporting_user, stacktrace, problem FROM errors WHERE id=?');
$stmt->execute(array($_REQUEST['id']));
$error = $stmt->fetch(PDO::FETCH_ASSOC);

// Download problem?
if(isset($_REQUEST['download']))
{
	header('Content-type: application/octet-stream');
	header('Content-disposition: attachment; filename=error.marla');
	print(stripslashes($error['problem']));
	die();
}
?>
<!DOCTYPE html>
<html>
<head>
	<title>The maRla Project - Error #<?=$error['id']?></title>
</head>
<body>
<p><a href="error_list.php">&lt;&lt;&lt; Back to list</a> | <a href="logout.php">Logout</a></p>

<table>
	<tr>
		<td>ID</td>
		<td><?=$error['id']?></td>
	</tr>
	<tr>
		<td>Resolved?</td>
		<td>
			<?=($error['resolved'] ? 'Yes' : 'No') ?>
			<a href="error.php?id=<?=$error['id']?>&amp;resolve">Toggle Status</a>
		</td>
	</tr>
	<tr>
		<td>Revision</td>
		<td><?=htmlentities($error['version']); ?></td>
	</tr>
	<tr>
		<td>OS</td>
		<td><?=htmlentities($error['os']); ?></td>
	</tr>
	<tr>
		<td>Report Date</td>
		<td><?=htmlentities($error['report_date']); ?></td>
	</tr>
	<tr>
		<td>User Name</td>
		<td><?=htmlentities($error['reporting_user']); ?></td>
	</tr>
	<tr>
		<td>Message</td>
		<td><?=htmlentities($error['message']); ?></td>
	</tr>
	<tr>
		<td>Stack Trace</td>
		<td style="white-space: pre;"><?=htmlentities($error['stacktrace']); ?></td>
	</tr>
	<tr>
		<td>Problem</td>
		<td>
			<?php
			if($error['problem'])
				print('<a href="error.php?id=' . $error['id'] . '&amp;download">Download</a>');
			else
				print('None uploaded');
			?>
		</td>
	</tr>
</body>
</html>
