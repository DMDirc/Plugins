socket = null;

function getAddress() {
    var loc = window.location;
    var uri = loc.protocol === "https:" ? "wss:" : "ws:";
    uri += "//" + loc.host + "/ws";
    return uri;
}

function connect() {
    socket = new WebSocket(getAddress());

    socket.onmessage = function(event) {
      console.log(event.data);
    }
}

function send(data) {
    socket.send(JSON.stringify(data));
}