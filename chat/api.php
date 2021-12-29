<?php
require_once "config.php";

// DEBUG STUFF
/*ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);*/

class message_obj
{
    public $sender;
    public $recipient;
    public $message;
    public $time_created;
    public $seen;
    function __construct($send, $recp, $mesg, $timc, $sn)
    {
        $this->sender = $send;
        $this->recipient = $recp;
        $this->message = $mesg;
        $this->time_created = $timc;
        $this->seen = $sn;
    }
}

function getAllUsers()
{
    global $link;
    $users = [];
    $query = "SELECT username FROM users";
    $result = mysqli_query($link, $query);
    while($row = mysqli_fetch_assoc($result))
    {
        array_push($users, $row["username"]);
    }
    return $users;
}

function getAllMessages($user, $recipient)
{
    global $link;
    $messages = [];
    $query = "SELECT sender, recipient, message, time_created, seen FROM messages WHERE (recipient = '".$recipient."' AND sender = '".$user."') OR (sender = '".$recipient."' AND recipient = '".$user."') ORDER BY time_created ASC";
    $result = mysqli_query($link, $query);
    while($row = mysqli_fetch_assoc($result))
    {
        array_push($messages, new message_obj($row["sender"], $row["recipient"], $row["message"], $row["time_created"], $row["seen"]));
    }
    $query = "UPDATE messages SET seen = TRUE WHERE (recipient = '".$recipient."' AND sender = '".$user."') OR (sender = '".$recipient."' AND recipient = '".$user."')";
    mysqli_query($link, $query);
    return $messages;
}

function getActiveContacts($recipient)
{
    global $link;
    $contacts = [];
    $query = "SELECT sender FROM messages WHERE recipient = '".$recipient."'";
    $result = mysqli_query($link, $query);
    while($row = mysqli_fetch_assoc($result))
    {
        if(!in_array($row["sender"], $contacts)) array_push($contacts, $row["sender"]);
    }
    $query = "SELECT recipient FROM messages WHERE sender = '".$recipient."'";
    $result = mysqli_query($link, $query);
    while($row = mysqli_fetch_assoc($result))
    {
        if(!in_array($row["recipient"], $contacts) && $row["recipient"][0] !== '@') array_push($contacts, $row["recipient"]);
    }
    return $contacts;
}

function sendMessage($sender, $recipient, $message)
{
    global $link;
    if(empty($message)) return;
    $query = "INSERT INTO messages (sender, recipient, message, time_created, seen) VALUES ('".$sender."', '".$recipient."', '".$message."', now(), FALSE)";
    mysqli_query($link, $query) or die(mysqli_error($link));
}

function deleteAll($sender, $recipient)
{
    global $link;
    $query = "DELETE FROM messages WHERE sender = '".$sender."' AND recipient = '".$recipient."'";
    mysqli_query($link, $query);
    $query = "DELETE FROM messages WHERE recipient = '".$sender."' AND sender = '".$recipient."'";
    mysqli_query($link, $query);
}

function userExists($user)
{
    global $link;
    $query = "SELECT * FROM users WHERE username = '".$user."'";
    $result = mysqli_query($link, $query);
    if($row = mysqli_fetch_assoc($result)) return true;
    else return false;
}

function getUserFullName($user)
{
    global $link;
    if(userExists($user))
    {
        $query = "SELECT fullname FROM users WHERE username = '".$user."'";
        $result = mysqli_query($link, $query);
        if($row = mysqli_fetch_assoc($result)) return $row["fullname"];
    }
    return "User not found.";
}

function getUserEmailAddress($user)
{
    global $link;
    if(userExists($user))
    {
        $query = "SELECT email FROM users WHERE username = '".$user."'";
        $result = mysqli_query($link, $query);
        if($row = mysqli_fetch_assoc($result)) return $row["email"];
    }
    return "";
}

if($_SERVER['REQUEST_METHOD'] === 'POST')
{
    session_start();
    if(!isset($_SESSION["loggedin"]) || $_SESSION["loggedin"] !== true)
    {
        header("location: login.php");
        exit;
    }   
    if($_POST["action"] == "send")
    {
        sendMessage($_SESSION["username"], $_POST["recipient"], htmlspecialchars($_POST["message"]));
    }
    if($_POST["action"] == "delete")
    {
        deleteAll($_SESSION["username"], $_POST["recipient"]);
        header("location: ./conversation.php?user=".$_POST["recipient"]);
    }
    if($_POST["action"] == "getmesg")
    {
        header("Content-Type: application/json");
        echo json_encode(getAllMessages($_SESSION["username"], $_POST["recipient"]));
    }
}
if($_SERVER['REQUEST_METHOD'] === 'GET')
{
    session_start();
    if($_GET["action"] == "userexists")
    {
        echo json_encode(userExists($_POST["user"]));
    }
    if($_GET["action"] == "contacts")
    {
        echo json_encode(getActiveContacts($_GET["username"]));
    }
}

?>
