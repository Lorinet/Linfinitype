<?php
session_start();
if(!isset($_SESSION["loggedin"]) || $_SESSION["loggedin"] !== true)
{
    header("location: login.php");
    exit;
}
require_once "api.php";
$contacts = getActiveContacts($_SESSION["username"]);
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
    <div class="homeMenuButton" style="text-align: right;" onclick="document.getElementById('newChatDialog').style.display = 'block';"><i class="fas fa-plus"></i></div>
    <div class="bigTitle" style="text-align: center;">Chats</div>
    <br>
    <div id="newChatDialog" style="display: none;">
        <div class="mask"></div>
        <div class="modal">
            <p class="modalTitle">New chat</p>
            <div class="modalSpacer"></div>
            <div class="modalButton" onclick="document.getElementById('messageDialog').style.display = 'block';document.getElementById('newChatDialog').style.display = 'none';">Find user</div>
            <div class="modalSpacer"></div>
            <div class="modalButton" onclick="document.getElementById('newChatDialog').style.display = 'none';">Back</div>
        </div>
    </div>
    <div id="messageDialog" style="display: none;">
        <div class="mask"></div>
        <div class="modal">
            <p class="modalTitle">Find user</p>
            <form action="conversation.php" id="messageForm" method="get">
                <input type="text" name="user" class="bigTextBox" placeholder="Username">
            </form>
            <div class="modalSpacer"></div>
            <div class="modalSpacer"></div>
            <div class="modalButton affirmative" onclick="document.getElementById('messageForm').submit();">Send message</div>
            <div class="modalSpacer"></div>
            <div class="modalButton" onclick="document.getElementById('messageDialog').style.display = 'none';">Back</div>
        </div>
    </div>
    <div>
        <?php
            foreach($contacts as $name)
            {
                echo '<a class="member" href="./conversation.php?user='.$name.'">'.$name.'</a>';
            }
        ?>
    </div>
    <script>
        function getActiveContacts()
        {
            return '<?php echo json_encode($contacts); ?>';
        }
    </script>
</body>
</html>