<?php
require_once "config.php";
$username = $fullname = $password = $confirm_password = $email = "";
$username_err = $fullname_err = $password_err = $confirm_password_err = $email_err = "";
if ($_SERVER["REQUEST_METHOD"] == "POST")
{
    if (empty(trim($_POST["username"])))
    {
        $username_err = "Írj be egy felhasználónevet!";
    }
    else
    {
        $sql = "SELECT username FROM users WHERE username = ?";
        if ($stmt = mysqli_prepare($link, $sql))
        {
            mysqli_stmt_bind_param($stmt, "s", $param_username);
            $param_username = trim($_POST["username"]);
            if (mysqli_stmt_execute($stmt))
            {
                mysqli_stmt_store_result($stmt);
                if (mysqli_stmt_num_rows($stmt) == 1)
                {
                    $username_err = "Ez a felhasználónév már foglalt.";
                }
                else
                {
                    if(strpos($_POST["username"], '@') !== false || strpos($_POST["username"], '%') !== false || strpos($_POST["username"], '|') !== false || strpos($_POST["username"], '<') !== false || strpos($_POST["username"], '>') !== false || strpos($_POST["username"], '/') !== false || strpos($_POST["username"], '\\') !== false || strpos($_POST["username"], '#') !== false)
                    {
                        $username_err = "A felhasználónevek nem tartalmazhatnak speciális karaktereket!";
                    }
                    else
                    {
                        $username = trim($_POST["username"]);
                    }
                }
            }
            else
            {
                echo "Valami nem működött. Próbáld újra később.";
            }
            mysqli_stmt_close($stmt);
        }
    }
    if (empty(trim($_POST["email"])))
    {
        $email_err = "Írj be egy e-mail címet.";
    }
    else
    {
        $sql = "SELECT username FROM users WHERE email = ?";
        if ($stmt = mysqli_prepare($link, $sql))
        {
            mysqli_stmt_bind_param($stmt, "s", $param_email);
            $param_email = trim($_POST["email"]);
            if (mysqli_stmt_execute($stmt))
            {
                mysqli_stmt_store_result($stmt);
                if (mysqli_stmt_num_rows($stmt) == 1)
                {
                    $email_err = "Ez az e-mail cím már foglalt.";
                }
                else
                {
                    $email = trim($_POST["email"]);
                }
            }
            else
            {
                echo "Valami nem működött. Próbáld újra késóbb.";
            }
            mysqli_stmt_close($stmt);
        }
    }
    if (empty(trim($_POST["password"])))
    {
        $password_err = "Írj be egy jelszót.";
    }
    elseif (strlen(trim($_POST["password"])) < 6)
    {
        $password_err = "A jelszó legalább 6 karakter hosszú kell legyen.";
    }
    else
    {
        $password = trim($_POST["password"]);
    }
    if (empty(trim($_POST["confirm_password"])))
    {
        $confirm_password_err = "Írd be újra a jelszavadat.";
    }
    else
    {
        $confirm_password = trim($_POST["confirm_password"]);
        if (empty($password_err) && ($password != $confirm_password))
        {
            $confirm_password_err = "A két jelszó nem talál.";
        }
    }
    if(empty($_POST["fullname"]))
    {
        $fullname_err = "Írd be a teljes nevedet.";
    }
    else
    {
        $fullname = htmlspecialchars($_POST["fullname"]);
    }
    if (empty($username_err) && empty($fullname_err) && empty($password_err) && empty($confirm_password_err) && empty($email_err))
    {
        $sql = "INSERT INTO users (username, password, email, fullname) VALUES (?, ?, ?, ?)";
        if ($stmt = mysqli_prepare($link, $sql))
        {
            mysqli_stmt_bind_param($stmt, "ssss", $param_username, $param_password, $param_email, $param_fullname);
            $param_username = $username;
            $param_email = $email;
            $param_password = password_hash($password, PASSWORD_DEFAULT);
            $param_fullname = $fullname;
            if (mysqli_stmt_execute($stmt))
            {
                header("location: login.php");
            }
            else
            {
                echo "Valami nem működik. Próbáld újra később.";
            }
            mysqli_stmt_close($stmt);
        }
    }
    mysqli_close($link);
}
?>
<!DOCTYPE html>
<html lang="en">
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
            <div class="bigTitle" style="padding: 0px !important;">Fiók létrehozása</div>
            <br>
            <form action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]); ?>" method="post">
                <div class="form-group <?php echo (!empty($username_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="username" class="lightTextBox" placeholder="Felhasználónév" value="<?php echo $username; ?>">
                    <span class="help-block"><?php echo $username_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($fullname_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="fullname" class="lightTextBox" placeholder="Teljes név" value="<?php echo $fullname; ?>">
                    <span class="help-block"><?php echo $fullname_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($email_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="email" class="lightTextBox" placeholder="E-mail cím" value="<?php echo $email; ?>">
                    <span class="help-block"><?php echo $email_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($password_err)) ? 'has-error' : ''; ?>">
                    <input type="password" name="password" class="lightTextBox" placeholder="Jelszó" value="<?php echo $password; ?>">
                    <span class="help-block"><?php echo $password_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($confirm_password_err)) ? 'has-error' : ''; ?>">
                    <input type="password" name="confirm_password" class="lightTextBox" placeholder="Jelszó megerősítése" value="<?php echo $confirm_password; ?>">
                    <span class="help-block"><?php echo $confirm_password_err; ?></span>
                </div>
                <input type="submit" class="niceWideButton affirmative" style="width: 100%; margin: 5px 0px 15px 0px;" value="Feliratkozás">
                <p>Van már fiókod? <a href="login.php">Jelentkezz be</a>.</p>
            </form>
        </div>
    </div>
</body>

</html>