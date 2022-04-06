<?php

define('DB_SERVER', 'localhost');
define('DB_NAME', 'linfinitype_chat');
define('DB_USERNAME', 'root');
define('DB_PASSWORD', 'password');

$link = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_NAME);
?>
