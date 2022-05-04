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