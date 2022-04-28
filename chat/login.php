<!DOCTYPE html>
<html lang="en">
<?php
session_start();
if (isset($_SESSION["loggedin"]) && $_SESSION["loggedin"] === true)
{
    header("location: postlogin.php");
    exit;
}
require_once "config.php";
$username = $password = "";
$username_err = $password_err = "";
$dnal = false;
if ($_SERVER["REQUEST_METHOD"] == "POST")
{
    if (empty(trim($_POST["username"])))
    {
        $username_err = "Enter your username!";
        $dnal = true;
    }
    else
    {
        $username = trim($_POST["username"]);
    }
    if (empty(trim($_POST["password"])))
    {
        $password_err = "Enter your password!";
        $dnal = true;
    }
    else
    {
        $password = trim($_POST["password"]);
    }
    if (empty($username_err) && empty($password_err))
    {
        $sql = "SELECT username, password FROM users WHERE username = ?";
        if ($stmt = mysqli_prepare($link, $sql))
        {
            mysqli_stmt_bind_param($stmt, "s", $param_username);
            $param_username = $username;
            if (mysqli_stmt_execute($stmt))
            {
                mysqli_stmt_store_result($stmt);
                if (mysqli_stmt_num_rows($stmt) == 1)
                {
                    mysqli_stmt_bind_result($stmt, $username, $hashed_password);
                    if (mysqli_stmt_fetch($stmt))
                    {
                        if (password_verify($password, $hashed_password))
                        {
                            session_start();
                            $_SESSION["loggedin"] = true;
                            $_SESSION["username"] = $username;
                            $_SESSION["password"] = $password;
                            header("location: postlogin.php");
                            exit;
                        }
                        else
                        {
                            $password_err = "Your password is incorrect.";
                            $dnal = true;
                        }
                    }
                }
                else
                {
                    $username_err = "Invalid username";
                    $dnal = true;
                }
            }
            else
            {
                echo "Something went wrong. Try again later.";
                $dnal = true;
            }
            mysqli_stmt_close($stmt);
        }
    }
    mysqli_close($link);
}
?>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Linfinitype</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
    <link rel="stylesheet" href="./style.css">
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Raleway:wght@400;600;700&family=Roboto:wght@400;500&display=swap">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.2/css/all.min.css">
</head>
<body>
    <div class="spacer"></div>
    <div class="container">
        <div class="centered">
            <div class="bigTitle" style="padding: 0px !important;">Sign in</div>
            <br>
            <form action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]); ?>" method="post" id="loginForm">
                <div class="form-group <?php echo (!empty($username_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="username" id="usernameBox" class="lightTextBox" placeholder="Username" value="<?php echo $username; ?>">
                    <span class="help-block"><?php echo $username_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($password_err)) ? 'has-error' : ''; ?>">
                    <input type="password" name="password" id="passwordBox" class="lightTextBox" placeholder="Password">
                    <span class="help-block"><?php echo $password_err; ?></span>
                </div>
                <input type="submit" class="niceWideButton affirmative" style="width: 100%; margin: 5px 0px 15px 0px;" value="Login">
                <p>Don't have an account? <a href="register.php">Sign up now!</a>.</p>
            </form>
        </div>
    </div>
    <script>
        var doNotAutologin = false;
        <?php
            if($dnal) echo 'doNotAutologin = true;';
        ?>
        var uname = localStorage.getItem("username");
        var passwd = localStorage.getItem("password");
        if(uname !== null && passwd !== null && !doNotAutologin)
        {
            document.getElementById("usernameBox").value = uname;
            document.getElementById("passwordBox").value = passwd;
            document.getElementById("loginForm").submit();
        }
    </script>
</body>
</html>