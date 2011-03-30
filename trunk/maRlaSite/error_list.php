<?php
session_start();
if(!@$_SESSION['logged_in'])
{
	header('Location: login.php');
	die();
}

require_once('lib.inc');

if(isset($_REQUEST['resolve_all']))
{
	$stmt = fetchErrors(isset($_REQUEST['resolved']),
				@$_REQUEST['dmin'], @$_REQUEST['dmax'],
				@$_REQUEST['rmin'], @$_REQUEST['rmax'],
				@$_REQUEST['contains']);
	
	$ids = array();
	while($row = $stmt->fetch(PDO::FETCH_ASSOC))
		$ids[] = $row['id'];

	$stmt->closeCursor();

	resolveErrors($ids);

	header('Location: error_list.php');
}
?>
<!DOCTYPE html>
<html>
<head>
	<title>The maRla Project - Exception Report Listing</title>
</head>
<body>

<a href="logout.php">Logout</a>

<form method="get" action="error_list.php">
	<table>
		<tr>
			<td>Revision:</td>
			<td><input name="rmin" type="number" min="0" max="999" value="<?=htmlentities(@$_REQUEST['rmin']);?>" /></td>
			<td>-</td>
			<td><input name="rmax" type="number" min="0" max="999" value="<?=htmlentities(@$_REQUEST['rmax']);?>" /></td>
		</tr>
		<tr>
			<td>Date:</td>
			<td><input name="dmin" type="date" value="<?=htmlentities(@$_REQUEST['dmin']);?>" /></td>
			<td>-</td>
			<td><input name="dmax" type="date" value="<?=htmlentities(@$_REQUEST['dmax']);?>" /></td>
		</tr>
		<tr>
			<td>Contains:</td>
			<td colspan="3"><input name="contains" type="search" value="<?=htmlentities(@$_REQUEST['contains']);?>" /></td>
		</tr>
		<tr>
			<td>Include resolved?</td>
			<td><input name="resolved" type="checkbox" value="1" <?=(isset($_REQUEST['resolved']) ? 'checked' : '')?> /></td>
		</tr>
		<tr>
			<td></td>
			<td colspan="3">
				<input name="search" type="submit" value="Search" />
			</td>
		</tr>
	</table>
</form>

<p>
<?php
	echo('<a href="error_list.php?resolve_all');
	
	$search = implode('&amp;', $_REQUEST);
	if(!empty($search))
		echo("&amp;$search");
	
	echo('">Resolve All</a>');
?>
</p>

<table style="width=100%; height: 100%;">
	<tr>
		<th>ID</th>
		<th>Rev</th>
		<th>Date</th>
		<th>Message</th>
		<th>Trace</th>
	</tr>
	<?php
	$stmt = fetchErrors(isset($_REQUEST['resolved']),
				@$_REQUEST['dmin'], @$_REQUEST['dmax'],
				@$_REQUEST['rmin'], @$_REQUEST['rmax'],
				@$_REQUEST['contains']);
		
	while($row = $stmt->fetch(PDO::FETCH_ASSOC))
	{
		?>
		<tr>
			<td><a href="error.php?id=<?=htmlentities($row['id'])?>"><?=htmlentities($row['id'])?></a></td>
			<td><?=htmlentities($row['version']);?></td>
			<td><?=htmlentities($row['report_date']);?></td>
			<td><?=htmlentities($row['message']);?></td>
			<td><?php print(str_replace("\n", "<br />\n", head($row['stacktrace'], 3))); ?></td>
		</tr>
		<?php
	}
	?>
</table>
</body>
</html>
