<?php
session_start();
if(!isset($_SESSION["loggedin"]) || $_SESSION["loggedin"] !== true)
{
    header("location: login.php");
    exit;
}
require_once "api.php";
if(!isset($_GET))
{
    header("location: home.php");
}
if(!isset($_GET["user"]))
{
    header("location: home.php");
}
$contact = $_GET["user"];
$isgrp = FALSE;
if($contact[0] === '@') $isgrp = TRUE;
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
    <script>
        function escapeHtml(text) 
        {
        return encodeURIComponent(text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;"));
        }
    </script>
</head>
<body>
    <span id="contactname" style="display: none;"><?php echo $contact; ?></span>
    <span id="username" style="display: none;"><?php echo $_SESSION["username"]; ?></span>
    <span id="isgroup" style="display: none;"><?php if($isgrp) echo "grp"; ?></span>
    <ul class="titleHeader center">
        <li class="backItem"><a href="./chats.php" class="backButton"><span>Vissza</span></a></li>
        <li class="userLabel"><?php echo $contact; ?></li>
    </ul>
    <div class="conversationContainer center" id="conversationContainer">
        <table id="messageTable" class="messageTable">
        </table>
        <ul class="replyArea">
            <li class="replyBoxContainer"><input type="text" placeholder="Üzenet" class="replyBox" name="message" id="replyBox" autofocus autocomplete="off"></li>
            <li class="chatButton"><i class="fas fa-paper-plane" onclick="send();"></i></li>
        </ul>
    </div>
    <script>
        function send()
        {
            var http = new XMLHttpRequest();
            http.open('POST', "api.php", true);
            http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            http.onreadystatechange = function()
            {
                loadMessages();
            };
            http.send('action=send&recipient=' + contact + '&message=' + escapeHtml(document.getElementById("replyBox").value));
            document.getElementById("replyBox").value = "";
        }
        var contact = document.getElementById("contactname").textContent;
        var username = document.getElementById("username").innerHTML;
        document.getElementById("replyBox").addEventListener("keyup", 
        function(event)
        {
            if (event.keyCode === 13)
            {
                event.preventDefault();
                send();
            }
        });
        var messageIndex = 0;
        table = document.getElementById("messageTable");
        table.innerHTML = "";
        table.appendChild(document.createElement("tbody"));
        var tbody = table.getElementsByTagName("tbody")[0];
        var forskipping = "";
        var flagSkipDown = true;
        function loadMessages()
        {
            var http = new XMLHttpRequest();
            http.open('POST', "api.php", true);
            http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            http.onreadystatechange = function()
            {
                if(http.readyState == 4 && http.status == 200)
                {
                    var mesg = JSON.parse(http.response);
                    if(mesg.length == messageIndex) return;
                    var clas;
                    var omi = messageIndex;
                    for(messageIndex; messageIndex < mesg.length; messageIndex++)
                    {
                        clas = "mich";
                        if(mesg[messageIndex].sender != username) clas = "fremd";
                        tbody.innerHTML += '<tr><td class="' + clas + 'u">' + mesg[messageIndex].sender + '</td><td class="' + clas + '">' + mesg[messageIndex].message + '</td><td style="display: none;">' + mesg[messageIndex].time_created + '</td></tr>\n';
                    }
                    var rows;
                    rows = table.rows;
                    for (omi; omi < rows.length; omi++)
                    {
                        if(rows[omi].getElementsByTagName("td")[0].innerHTML != forskipping)
                        {
                            forskipping = rows[omi].getElementsByTagName("td")[0].innerHTML;
                            if(window.getComputedStyle(rows[omi].getElementsByTagName("td")[0]).display != "none") rows[omi].getElementsByTagName("td")[1].style.borderTopLeftRadius = "2px";
                            if(omi > 0) rows[omi - 1].getElementsByTagName("td")[1].style.borderBottomLeftRadius = "2px";
                        }
                        else
                        {
                            rows[omi].getElementsByTagName("td")[0].style.display = "none";
                            rows[omi - 1].getElementsByTagName("td")[1].style.borderBottomLeftRadius = "0px";
                        }
                    }
                    rows[rows.length - 1].getElementsByTagName("td")[1].style.borderBottomLeftRadius = "2px";
                    messageIndex = mesg.length;
                    for(var i = 0; i < mesg.length; i++)
                    {
                        if(mesg[i].seen == false) alert(mesg[i].message);
                    }
                }
            }
            if(tbody.innerHTML != "" && flagSkipDown)
            {
                var objDiv = document.getElementById("conversationContainer");
                objDiv.scrollTop = objDiv.scrollHeight;
                flagSkipDown = false;
            }
            http.send('action=getmesg&recipient=' + contact);

        }
        loadMessages();
        setInterval(loadMessages, 1000);

        function reply(message)
        {
            document.getElementById("replyBox").value = message;
            send();
        }
    </script>
</body>
</html>