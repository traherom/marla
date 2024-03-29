<?php
require_once('../lib/db.inc');

function fetchErrors($resolved, $dmin, $dmax, $rmin, $rmax, $contains, $limit=null)
{
	global $db;
	
	$query = "SELECT id, report_date, reporting_user, version, os, message, stacktrace
					FROM errors
					WHERE 1=1 ";

	if(!$resolved)
		$query .= ' AND resolved=0';
	
	if(!empty($dmin))
		$query .= ' AND report_date >= ' . $db->quote($dmin);
	if(!empty($dmax))
		$query .= ' AND report_date <= ' . $db->quote($dmax);
		
	if(!empty($rmin))
		$query .= " AND version >= " . $db->quote($rmin);
	if(!empty($rmax))
		$query .= " AND version <= " . $db->quote($rmax);
	
	if(!empty($contains))
		$query .= " AND stacktrace LIKE '%" . addcslashes(addslashes($contains), "%_") . "%'";
		
	$query .= " ORDER BY report_date DESC";
	
	if($limit != null)
		$query .= " LIMIT " . addslashes($limit);
	
	return $db->query($query);
}

function resolveErrors($ids)
{
	global $db;
	$stmt = $db->prepare('UPDATE errors SET resolved=1 WHERE id=?');
	foreach($ids as $id)
	{
		$stmt->execute(array($id));
		$stmt->closeCursor();
	}
}

function head($str, $lineCount)
{
	$lines = explode("\n", $str, $lineCount + 1);
	
	$top = '';
	for($i = 0; $i < $lineCount; $i++)
		$top .= $lines[$i] . "\n";
	
	return $top;
}
?>
