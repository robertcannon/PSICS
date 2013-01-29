<?php
 
 $filecode= 'missing';
    if (isset($_GET['a'])) {
        $filecode = $_GET['a'];
    }
   
$temp_dir = "tmp";
 
$filepath = "$temp_dir/$filecode";
 
if (file_exists($filepath) &&  (time() - filemtime($filepath)) < 60 * 60 * 24 * 7) {
//   unlink($filepath);

   print file_get_contents("download.html");
   
  
} else {
	if (file_exists($filepath)) {
		unlink($filepath);
	}
	
	?> 
	
	<html><head></head>
	<body>
		<p>
			Sorry. This link has expired. You can download PSICS by filling in the 
			<a href="predownload.html">download form</a>.
		</p>
	
	</body>
	</html>
	<?php
}
?>
 
  
 

