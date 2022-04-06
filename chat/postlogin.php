<?php
session_start();
if(isset($_SESSION["loggedin"]))
{
    echo '
    <!DOCTYPE html>
    <html>
        <script>
            localStorage.setItem("username", "'.$_SESSION["username"].'");
            localStorage.setItem("password", "'.$_SESSION["password"].'");
            window.location.href = "chats.php";
        </script>
    </html>
    ';
    unset($_SESSION["password"]);
}
else
{
    header("location: login.php");
    exit;
}
?>