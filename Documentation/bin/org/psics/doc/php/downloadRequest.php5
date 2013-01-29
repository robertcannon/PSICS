<?php

// Parse form fields.
$email = "";
$message = "";

if (isset($_POST["email"])) {
  $email = $_POST['email'];
 }
if (isset($_POST["message"])) {
  $message = $_POST['message'];
 }
 
$fromaddr = gethostbyaddr($_SERVER['REMOTE_ADDR']);
 
// Create subject.
$subject = "PSICS download: " . $email;

// Create body.
$body =  "\n";
$body .=  "<psics-download-request\n";
$body .= "   date=\"" . date("D M j G:i:s T Y") . "\"\n";
$body .= "   email=\"" . $email . "\"\n";
$body .= "   from=\"" . $fromaddr . "\">\n";
$body .= $message;
$body .=  "\n</psics-download-request>\n";
$body .=  "\n";

// send a notification to us that someone wants it
mail("downloads@psics.org", $subject, $body, "From: " . "notifications@psics.org" . "\n");

$temp_dir = "tmp";

$code = uniqid();

$fh = fopen("$temp_dir/$code", 'w');
fputs($fh, $email);
fclose($fh);

$subject = "PSICS Download";
// send a message to the user to let them get it
$body =  "\n";
$body .= "\n";
$body .= "Downloading PSICS\n\n";
$body .=  "Thank you for your interset in PSICS. You can download it from \n";
$body .= "http://www.psics.org/download.php5?a=" . $code;
$body .= "\n\n";

mail($email, $subject, $body, "From: " . "notifications@psics.org" . "\n");


// Now redirect to the specified redirect page.
header("Location: " . "thanks.html");
 
?>
 

