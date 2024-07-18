var currentUser = null;
var isConnected = false;
var audioMessagesCount = 0;
var isEnableAutomaticAudioPlay = true;

//var stompClient = new StompJs.Client({
//    //brokerURL: 'ws://katpoc.duckdns.org/prioritymsg-websocket'
//    brokerURL: 'ws://localhost:8080/prioritymsg-websocket'
//});
//
//stompClient.onConnect = (frame) => {
//    setConnected(true);
//    console.log('Connected: ', frame);
//    currentUser = frame.headers["user-name"];
//    $("#username").text(currentUser);
//    stompClient.subscribe('/topic/messages', (response) => {
//        $("#name").val("");
//        console.log("Response: ", response);
//        var message = JSON.parse(response.body)
//        addMessageEntry(message);
//    });
//
//    // Subscribe to read-aloud messages
//    stompClient.subscribe('/topic/messages/tts', (response) => {
//        $("#name").val("");
//        console.log("Response: ", response);
//        var message = JSON.parse(response.body)
//        addMessageEntry(message);
//    });
//};

//stompClient.onWebSocketError = (error) => {
//    console.error('Error with websocket', error);
//};
//
//stompClient.onStompError = (frame) => {
//    console.error('Broker reported error: ' + frame.headers['message']);
//    console.error('Additional details: ' + frame.body);
//};

function setConnected(connected) {
    isConnected = connected;
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
}

function toggleConnection() {
    isConnected ? disconnect() : connect();
}

function connect() {
    stompClient.activate();
    setConnected(true);
    $("#connect").removeClass("is-dark");
    $("#connect").addClass("is-primary");
    $("#connect").addClass("is-active");
    $("#connectedIcon").text('comment');
    $("#connectedIcon").addClass('connected_icon');
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");

    $("#connect").removeClass("is-primary");
    $("#connect").removeClass("is-active");
    $("#connectedIcon").removeClass('connected_icon');
    $("#connect").addClass("is-dark");
    $("#connectedIcon").text('comments_disabled');
}

function sendMessage(isPriority) {
    stompClient.publish({
        destination: "/app/msg/messages",
        body: JSON.stringify({'message': $("#name").val(), 'priority': isPriority})
    });
}

function sendAudioMessage(isPriority) {
    stompClient.publish({
        destination: "/app/msg/readAloud",
        body: JSON.stringify({'message': $("#name").val(), 'priority': isPriority})
    });
}

function addMessageEntry(message) {
    var username = message.username;
    var messageText = message.message;
    var time = message.time;
    var isPriority = message.priority;
    var isCurrentUser = currentUser === username;
    var hasAudio = message.audioInfo !== null;
    var audioElementId = "#tts" + audioMessagesCount;
    var audioElementControlId = "#tts" + audioMessagesCount + "Control";

    var messageEntry = '<div class="columns is-mobile">';
    var emptyColumn = '<div class="column is-one-fifth"></div>';
    var messageColumn = '<div class="column"><div class="notification pt-1 pb-2' + (isCurrentUser ? ' is-link' : (isPriority ? ' priority-notification' : '')) + '"><article class="media"><div class="media-content"><span class="is-size-7 has-text-grey' + (isCurrentUser ? 'dark' : 'light') + '">' + username + '</span><p>' + messageText + '</p></div><figure class="media-right">' + (isPriority ? '<div class="circle"></div>' : '') + (hasAudio ? '<audio id="' + audioElementId + '"><source src="' + message.audioInfo.audioFilePath + '" type="audio/wav"></audio><span class="icon is-small"><span id="' + audioElementControlId + '" class="material-symbols-rounded">auto_read_play</span></span>' : '') + '</figure></article></div></div>'

    messageEntry += (isCurrentUser ? emptyColumn + messageColumn : messageColumn + emptyColumn) + "</div>";

    // TODO: Add time as tooltip
    //messageEntry += "<td>" + time + "</td></tr>";
    console.log(messageEntry);

    $("#conversation").append(messageEntry);

    // Audio controls
    if (hasAudio) {
        $(document).ready(function() {
            var audioElementControl = $(audioElementControlId);
            audioElementControl.on("click", audioElementControlId, function() {
                console.log("Something was clicked: " + audioElementControlId, audioElementId);
                ttsAudioControl(audioElementId, audioElementControlId);
            });
            $( audioElementId ).on("ended", () => { audioElementControl.text("auto_read_play"); });
        });
        audioMessagesCount++;

        // Automatic play
        if (isEnableAutomaticAudioPlay) {
            console.log("Audio elementID: " + audioElementId);
            var audioPlayer = document.getElementById(audioElementId);
            console.log("Garbage: ", audioPlayer);
            audioPlayer.play().then(() => { $(audioElementControlId).text("auto_read_pause"); });
        }
    }

    $("html").animate({ scrollTop: $("html").prop("scrollHeight")}, 1000);
}

//function ttsAudioControl(audioElementId, audioElementControlId) {
//    var audio = document.getElementById(audioElementId);
//    var control = $(audioElementControlId);
//    console.log("The garbage");
//    if (audio.paused) {
//        audio.play().then(() => {
//            control.innerText = "auto_read_play";
//        });
//    }
//    else {
//        audio.pause();
//        control.innerText = "auto_read_pause";
//    }
//}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => toggleConnection());
    $( "#conversation" ).hide();
    $( "#send" ).click(() => sendMessage(false));
    $( "#sendMobile" ).click(() => sendMessage(false));
    $( "#sendPriority" ).click(() => sendMessage(true));
    $( "#sendAudio" ).click(() => sendAudioMessage(true));


    $( "#itemSearchBtn" ).click(() => {
        $.ajax({
            url: '/items?title=' + $("#itemTitle").val(),
            type: 'GET',
            success: function(response) {
                // Clear items
                $('#itemSearchResults').empty();
                let id = 0;

                for (let item of response) {
                    console.log(item);

                    $("#itemSearchResults").append('<article id="item' + id + '" class="media"><figure class="media-left"><p class="image is-64x64"><img src="' + item.p_u + '"/></p></figure><div class="media-content"><strong>' + item.ti + '</strong><br/>' + item.co + ' ' + item.ca + '</div></article>');
                    let itemId = "item" + (id++);

                    $(document).ready(function() {
                        $( itemId ).on("click", () => { console.log("Hi"); });
                    });
                }
            },
            error: function(error) {
                // Handle error here
            }
        });
    });
});


/*****************************************************************/
// Login and register logic
$(document).ready(function() {
    $('#registerForm').on('submit', function(event) {
        event.preventDefault();

        var username = $('#username').val();
        var password = $('#password').val();

        $.ajax({
            url: '/users/save',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                username: username,
                password: password
            }),
            success: function(response) {
                // Handle success here
            },
            error: function(error) {
                // Handle error here
            }
        });
    });
});
