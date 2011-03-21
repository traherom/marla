<?php
require_once('lib.inc');

header('Content-type: application/rss+xml');
print('<?xml version="1.0" encoding="UTF-8" ?>');
?>
<rss version="2.0">
	<channel>
		<title>maRla Exception Feed</title>
		<link>http://marla.googlecode.com</link>
		<description>Exceptions reported by maRla clients</description>
		
		<?php
		$stmt = fetchErrors(isset($_REQUEST['resolved']),
					@$_REQUEST['dmin'], @$_REQUEST['dmax'],
					@$_REQUEST['rmin'], @$_REQUEST['rmax'],
					@$_REQUEST['contains'],
					25);
					
		while($row = $stmt->fetch(PDO::FETCH_ASSOC))
		{
			print("\n\t\t<item>");
			print("\n\t\t\t<title>maRla Exception - r" . htmlentities($row['version']) . " - " . htmlentities($row['message']) . "</title>");
			print("\n\t\t\t<link>http://www.moreharts.com/marla/error.php?id=" . $row['id'] . "</link>");
			print("\n\t\t\t<description>");
			print("Error occured in revision " . htmlentities($row['version']));
			print("<br /><strong>Stacktrace:</strong><br />");
			print(str_replace("\n", "<br />\n", head($row['stacktrace'], 3)));
			print("</description>");
			print("\n\t\t</item>");
		}
		?>
	</channel>
</rss>
