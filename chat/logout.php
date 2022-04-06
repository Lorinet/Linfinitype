<?php
session_start();
$_SESSION = array();
session_destroy();
?>
<!DOCTYPE html>
<html>
    <head>
    <title>Linfinitype</title>
    </head>
    <script>
        localStorage.clear();
        window.location.href = "login.php";
    </script>
</html>