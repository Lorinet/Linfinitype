<?php
/*
 * Linfinitype Chat Service
 * Copyright (C) 2022 Kovacs Lorand; Linfinity Technologies
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Lesser Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

require_once "config.php";
$username = $fullname = $password = $confirm_password = $email = "";
$username_err = $fullname_err = $password_err = $confirm_password_err = $email_err = "";
if ($_SERVER["REQUEST_METHOD"] == "POST")
{
    if (empty(trim($_POST["username"])))
    {
        $username_err = "Enter a username!";
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
                    $username_err = "Username already in use.";
                }
                else
                {
                    if(strpos($_POST["username"], '@') !== false || strpos($_POST["username"], '%') !== false || strpos($_POST["username"], '|') !== false || strpos($_POST["username"], '<') !== false || strpos($_POST["username"], '>') !== false || strpos($_POST["username"], '/') !== false || strpos($_POST["username"], '\\') !== false || strpos($_POST["username"], '#') !== false)
                    {
                        $username_err = "Username cannot contain special characters.";
                    }
                    else
                    {
                        $username = trim($_POST["username"]);
                    }
                }
            }
            else
            {
                echo "Something went wrong. Please try again later!";
            }
            mysqli_stmt_close($stmt);
        }
    }
    if (empty(trim($_POST["email"])))
    {
        $email_err = "Enter e-mail address!";
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
                    $email_err = "This e-mail address is already in use.";
                }
                else
                {
                    $email = trim($_POST["email"]);
                }
            }
            else
            {
                echo "VSomething got fucked up. Try again later!";
            }
            mysqli_stmt_close($stmt);
        }
    }
    if (empty(trim($_POST["password"])))
    {
        $password_err = "Enter a password!";
    }
    elseif (strlen(trim($_POST["password"])) < 6)
    {
        $password_err = "The password must be at least 6 characters long.";
    }
    else
    {
        $password = trim($_POST["password"]);
    }
    if (empty(trim($_POST["confirm_password"])))
    {
        $confirm_password_err = "Confirm your password!";
    }
    else
    {
        $confirm_password = trim($_POST["confirm_password"]);
        if (empty($password_err) && ($password != $confirm_password))
        {
            $confirm_password_err = "The passwords do not match.";
        }
    }
    if(empty($_POST["fullname"]))
    {
        $fullname_err = "Enter your full name.";
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
                echo "Something is not right. Try again later!";
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
            <div class="bigTitle" style="padding: 0px !important;">Sign up</div>
            <br>
            <form action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]); ?>" method="post">
                <div class="form-group <?php echo (!empty($username_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="username" class="lightTextBox" placeholder="Username" value="<?php echo $username; ?>">
                    <span class="help-block"><?php echo $username_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($fullname_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="fullname" class="lightTextBox" placeholder="Full name" value="<?php echo $fullname; ?>">
                    <span class="help-block"><?php echo $fullname_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($email_err)) ? 'has-error' : ''; ?>">
                    <input type="text" name="email" class="lightTextBox" placeholder="E-mail address" value="<?php echo $email; ?>">
                    <span class="help-block"><?php echo $email_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($password_err)) ? 'has-error' : ''; ?>">
                    <input type="password" name="password" class="lightTextBox" placeholder="Password" value="<?php echo $password; ?>">
                    <span class="help-block"><?php echo $password_err; ?></span>
                </div>
                <div class="form-group <?php echo (!empty($confirm_password_err)) ? 'has-error' : ''; ?>">
                    <input type="password" name="confirm_password" class="lightTextBox" placeholder="Confirm password" value="<?php echo $confirm_password; ?>">
                    <span class="help-block"><?php echo $confirm_password_err; ?></span>
                </div>
                <input type="submit" class="niceWideButton affirmative" style="width: 100%; margin: 5px 0px 15px 0px;" value="Register">
                <p>Have you got an account? <a href="login.php">Log in here</a>.</p>
            </form>
        </div>
    </div>
</body>

</html>