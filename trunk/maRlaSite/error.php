<?php
if(!isset($_REQUEST['id']))
{
	header('Location: error_list.php');
	die();
}

// Get the error
require_once('../lib/db.inc');
$stmt = $db->prepare('SELECT id, report_date, version, message, stacktrace, problem FROM errors WHERE id=?');
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
<table>
	<tr>
		<td>ID</td>
		<td><?=$error['id']?></td>
	</tr>
	<tr>
		<td>Revision</td>
		<td><?=htmlentities($error['version']); ?></td>
	</tr>
	<tr>
		<td>Report Date</td>
		<td><?=htmlentities($error['report_date']); ?></td>
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
