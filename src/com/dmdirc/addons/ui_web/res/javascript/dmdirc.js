var enabled = true;
var clientID = Math.ceil(Math.random() * 1000000000);
var activeWindow = null;
var wus_open = false;
var windows = new Hash();
var speeds = new Hash();
var interval;

speeds.set('wus', 1000);

function dmdirc_start() {
    if (interval != null) {
        clearInterval(interval);
    }
    
    doUpdate();
}

function setSpeed(what, speed) {
    document.getElementById(what + '_' + speeds.get(what)).style.textDecoration = 'none';
    speeds.set(what, speed);
    document.getElementById(what + '_' + speed).style.textDecoration = 'underline';
}

function treeview_add(name, id, type, parent) {
    var parentNode;

    if (parent == null) {
        parentNode = document.getElementById('treeview');
    } else {
        parentNode = document.getElementById(parent);

        if (parentNode.getElementsByTagName('ul').length == 0) {
            var newUL = document.createElement('ul');
            parentNode.appendChild(newUL);
            parentNode = newUL;
        } else {
            parentNode = parentNode.getElementsByTagName('ul')[0];
        }
    }

    var newNode = document.createElement('li');
    newNode.id = id;
    newNode.className = type;

    var wrapperNode = document.createElement('div');
    wrapperNode.innerHTML = name;
    wrapperNode.style.cursor = 'pointer';
    wrapperNode.onclick = function() {treeview_click(newNode);};
    newNode.appendChild(wrapperNode);

    parentNode.appendChild(newNode);
}

function treeview_click(element) {
    window_show(element.id);
}

function treeview_setactive(id) {
    if (activeWindow != null) {
        document.getElementById(activeWindow).style.fontWeight = 'normal';
    }

    activeWindow = id;
    document.getElementById(activeWindow).style.fontWeight = 'bold';
}

function wus_show() {
    if (!enabled) {
        return;
    }
    
    if (document.getElementById('wus') == null) {
        var nsd = document.createElement('div');
        nsd.style.position = 'absolute';
        nsd.style.top = '50%';
        nsd.style.height = '400px';
        nsd.style.width = '600px';
        nsd.style.left = '50%';
        nsd.style.marginLeft = '-300px';
        nsd.style.marginTop = '-200px';
        nsd.style.border = '1px solid black';
        nsd.style.display = 'none';
        nsd.style.zIndex = '10';
        nsd.id = 'wus';
        nsd.className = 'dialog';
        nsd.innerHTML = '<p class="nowindow">Loading</p>';

        document.body.appendChild(nsd);
    }
    
    wus_open = true;
    
    new Ajax.Updater('wus', '/static/webuistatus.html', {onSuccess:function() {
            setTimeout('wus_init()', 200);}});
    new Effect.Appear('wus');
    
    wus_query();
}

function wus_init() {
    if (speeds.get('wus') != 1000) {
        document.getElementById('wus_1000').style.textDecoration = 'none';
        document.getElementById('wus_' + speeds.get('wus')).style.textDecoration
            = 'underline';
    }

    draggable("wus");
}

function wus_addrequest(url) {
    var objDiv = document.getElementById('wus_requests');
    
    if (objDiv != null) {
        var p = document.createElement('p');
        p.innerHTML = '<code>' + url + '</code> ' + new Date().toString();


        objDiv.appendChild(p);
        objDiv.scrollTop = objDiv.scrollHeight;    
    }
}

function wus_query() {
    new Ajax.Request('/dynamic/clients',
    {onSuccess:wus_handler, onFailure:errFunc, onException:excFunc});
}

function wus_close() {
    wus_open = false;
    
    new Effect.Fade('wus');
}

function wus_handler(transport) {
    if (!wus_open) {
        return;
    }
    
    var data = eval('(' + transport.responseText + ')');

    removeElements('wus_clients');
    
    for (var i = 0; i < data.length; i++) {
        var client = data[i];
        var tr = document.createElement('tr');
        var td = document.createElement('td');
        td.innerHTML = client.ip;
        tr.appendChild(td);
        
        var secs = Math.floor(client.time / 1000);
        var mins = Math.floor(client.time / 60000)
        
        td = document.createElement('td');
        td.innerHTML = (mins > 0 ? mins + ' minute' + (mins != 1 ? 's' : '') :
            secs + ' second' + (secs != 1 ? 's' : '')) + ' ago';
        tr.appendChild(td);
        
        td = document.createElement('td');
        td.innerHTML = client.eventCount;
        tr.appendChild(td);
        
        document.getElementById('wus_clients').appendChild(tr);
    }
    
    document.getElementById('wus_last').innerHTML = 'Last updated: ' + new Date().toString();
    
    setTimeout('wus_query()', speeds.get('wus'));
}

function nsd_show() {
    if (!enabled) {
        return;
    }
    
    if (document.getElementById('nsd') == null) {
        var nsd = document.createElement('div');
        nsd.style.position = 'absolute';
        nsd.style.top = '50%';
        nsd.style.height = '300px';
        nsd.style.width = '500px';
        nsd.style.left = '50%';
        nsd.style.marginLeft = '-250px';
        nsd.style.marginTop = '-150px';
        nsd.style.border = '1px solid black';
        nsd.style.display = 'none';
        nsd.style.zIndex = '10';
        nsd.id = 'nsd';
        nsd.className = 'dialog';
        nsd.innerHTML = '<p class="nowindow">Loading</p>';

        document.body.appendChild(nsd);
    }

    new Ajax.Updater('nsd', '/static/newserverdialog.html', {onSuccess:function() {
            setTimeout('draggable("nsd")', 200);}});
    new Ajax.Request('/dynamic/getprofiles',
    {onFailure:errFunc, onSuccess:handlerFunc, onException:excFunc});
    new Effect.Appear('nsd');
}

function draggable(what) {
    new Draggable(what, {handle:document.getElementById(what).firstChild,
        starteffect:null,endeffect:null});
}

function nsd_ok() {
    var server = document.getElementById('nsd_server').value;
    var port = document.getElementById('nsd_port').value;
    var password = document.getElementById('nsd_password').value;
    var profile = document.getElementById('nsd_profile').value;

    if (!/^[^\s]+$/.test(server)) {
        alert("Server name cannot contain spaces");
        new Effect.Pulsate('nsd_server', {pulses: 3});
        return;
    }

    if (!/^[0-9]+$/.test(port) || port < 1 || port > 65535) {
        alert("Port must be a number between 1 and 65535");
        new Effect.Pulsate('nsd_port', {pulses: 3});
        return;
    }

    new Ajax.Request('/dynamic/newserver',
    {parameters:{server:server, port:port, password:password,
            profile:profile}, onFailure:errFunc});
    nsd_cancel();
}

function nsd_cancel() {
    new Effect.Fade('nsd');
}

function profiles_clear() {
    var elements = document.getElementsByClassName('profilelist');

    for (var i = 0; i < elements.length; i++) {
        removeElements(elements[i].id);
    }
}

function profiles_add(profile) {
    var elements = document.getElementsByClassName('profilelist');

    for (var i = 0; i < elements.length; i++) {
        var option = document.createElement('option');
        option.innerHTML = profile;
        option.value = profile;
        elements[i].appendChild(option);
    }
}

function removeElements(id) {
    var element = document.getElementById(id);
    if (element.hasChildNodes()) {
        while (element.childNodes.length > 0) {
            element.removeChild(element.firstChild );
        }
    }
}

function nicklist_show() {
    var nicklist = document.getElementById('nicklist');

    if (nicklist.style.display != 'block') {
        document.getElementById('content').style.right = '240px';
        nicklist.style.display = 'block';
        new Ajax.Request('/dynamic/nicklistrefresh',
        {parameters: {window: activeWindow}, onFailure: errFunc,
            onSuccess: handlerFunc})
    }
}

function nicklist_clear() {
    removeElements('nicklist');
}

function nicklist_add(nick) {
    var entry = document.createElement('li');
    entry.innerHTML = nick;

    document.getElementById('nicklist').appendChild(entry);
}

function nicklist_hide() {
    var nicklist = document.getElementById('nicklist');

    if (nicklist.style.display == 'block') {
        document.getElementById('content').style.right = '15px';
        nicklist.style.display = 'none';
    }
}

function inputarea_show() {
    var ia = document.getElementById('inputarea');

    if (ia.style.display != 'block') {
        document.getElementById('content').style.bottom = '65px';
        document.getElementById('nicklist').style.bottom = '65px';
        ia.style.display = 'block';
    }
}

function inputarea_hide() {
    var ia = document.getElementById('inputarea');

    if (ia.style.display == 'block') {
        document.getElementById('content').style.bottom = '40px';
        document.getElementById('nicklist').style.bottom = '40px';
        ia.style.display = 'none';
    }
}

function input_settext(text) {
    document.getElementById('input').value = text;
}

function input_keydown(e) {
    var keynum;

    if (window.event) {
        keynum = e.keyCode;
    } else if (e.which) {
        keynum = e.which;
    }
    
    if (keynum == 13) {
        keynum = 10;
    }

    var control = e.ctrlKey;
    var shift = e.shiftKey;
    var alt = e.altKey;
    var el = document.getElementById('input');

    if (keynum == 10 && !control) {
        new Ajax.Request('/dynamic/input',
        {parameters:{input:document.getElementById('input').value,
                clientID:clientID, window:activeWindow}, onFailure:errFunc, onException:excFunc});
        el.value = '';
    } else if (keynum == 9 && !control) {
        new Ajax.Request('/dynamic/tab',
        {parameters:{input:el.value, selstart:el.selectionStart,
                selend:el.selectionEnd, clientID:clientID, window:activeWindow}, onFailure:errFunc, onException:excFunc});
        return false;
    } else if (keynum == 38 || keynum == 40) {
        // up/down
        new Ajax.Request('/dynamic/key' + ((keynum == 38) ? 'up' : 'down'),
        {parameters:{input:el.value, selstart:el.selectionStart,
                selend:el.selectionEnd, clientID:clientID, window:activeWindow}, onFailure:errFunc, onException:excFunc});
        return false;
    } else if (control && (keynum == 10 || keynum == 66 || keynum == 70 || keynum == 73
        || keynum == 75 || keynum == 79 || keynum == 85)) {
        new Ajax.Request('/dynamic/key',
        {parameters:{input:el.value, selstart:el.selectionStart,
                selend:el.selectionEnd, clientID:clientID, key:keynum, ctrl:control,
                shift:shift, alt:alt, window:activeWindow}, onFailure:errFunc, onException:excFunc});
        return false;
    }

    return true;
}

function input_setcaret(pos) {
    var el = document.getElementById('input');
    el.selectionStart = pos;
    el.selectionEnd = pos;
}

function window_clear(id) {
    if (activeWindow == id) {
        removeElements('content');
    }
    
    windows.get(id).lines = [];
}

function window_addline(id, line) {
    var p = document.createElement('p');
    p.innerHTML = line;
    
    if (activeWindow == id) {
        document.getElementById('content').appendChild(p);
        var objDiv = document.getElementById('content');
        objDiv.scrollTop = objDiv.scrollHeight;
    }
    
    windows.get(id).lines[windows.get(id).lines.length] = p;
}

function window_create(window, parent) {
    treeview_add(window.name, window.id, window.type, parent == null ? null : parent.id);
    windows.set(window.id, window);
    windows.get(window.id).lines = [];
    window_show(window.id);
    
    new Ajax.Request('/dynamic/windowrefresh',
    {parameters:{window:window.id}, onFailure:errFunc,
        onException:excFunc, onSuccess: handlerFunc})
}

function window_show(id) {
    treeview_setactive(id);
    title_settext(windows.get(id).title)

    var className = document.getElementById(id).className;

    if (className == 'channel')  {
        nicklist_show();
    } else {
        nicklist_hide();
    }

    if (className == 'server' || className == 'channel' || className == 'input'
        || className == 'query') {
        inputarea_show();
    } else {
        inputarea_hide();
    }
    
    removeElements('content');
    var objDiv = document.getElementById('content');
    
    windows.get(id).lines.forEach(function(x) {
        objDiv.appendChild(x);
    });
    objDiv.scrollTop = objDiv.scrollHeight;
}

function title_settext(newText) {
    var title = newText + " - DMDirc web interface";
    document.getElementById('title').innerHTML = new String(title).escapeHTML();
    document.title = title;
}

function statusbar_settext(newText) {
    document.getElementById('statusbar_main').innerHTML = new String(newText).escapeHTML();
    new Effect.Highlight('statusbar_main', {endcolor: "#c0c0c0", restorecolor: "#c0c0c0"});
}

function link_hyperlink(url) {
    statusbar_settext('Opening ' + url + '...');
    window.open(url);
}

function link_channel(channel) {
    new Ajax.Request('/dynamic/joinchannel',
    {parameters:{clientID:clientID, source:activeWindow, channel:channel},
        onFailure:errFunc, onException:excFunc});
}

function link_query(user) {
    new Ajax.Request('/dynamic/openquery',
    {parameters:{clientID:clientID, source:activeWindow, target:user},
        onFailure:errFunc, onException:excFunc});
}

function doUpdate() {
    new Ajax.Request('/dynamic/feed',
    {parameters:{clientID:clientID}, method: 'GET',
        onSuccess:updateHandlerFunc, onFailure:updateErrFunc, onException:updateExcFunc});
}

function updateHandlerFunc(transport) {
    handlerFunc(transport);
    doUpdate();
}

function handlerFunc(transport) {
    enabled = true;
    
    var data = eval('(' + transport.responseText + ')');

    for (var i = 0; i < data.length; i++) {
        var event = data[i];

        if (event.type == 'statusbar') {
            statusbar_settext(event.arg1);
        } else if (event.type == 'clearprofiles') {
            profiles_clear();
        } else if (event.type == 'addprofile') {
            profiles_add(event.arg1);
        } else if (event.type == 'newwindow') {
            window_create(event.arg1);
        } else if (event.type == 'newchildwindow') {
            window_create(event.arg1[1], event.arg1[0]);
        } else if (event.type == 'clearwindow') {
            window_clear(event.arg1);
        } else if (event.type == 'lineadded') {
            window_addline(event.arg1.window, event.arg1.message);
        } else if (event.type == 'settext') {
            input_settext(event.arg1);
        } else if (event.type == 'clearnicklist') {
            nicklist_clear();
        } else if (event.type == 'addnicklist') {
            nicklist_add(event.arg1);
        } else if (event.type == 'setcaret') {
            input_setcaret(event.arg1);
        } else {
            statusbar_settext("Unknown event type: " + event.type);
        }
    }
}

function updateErrFunc(transport) {
    errFunc(transport);
    doUpdate();
}

function errFunc(transport) {
    statusbar_settext('Error while perfoming remote call...');
    alert(transport.status + "\n" + transport.statusText + "\n" + transport.responseText);
}

function updateExcFunc(request, exception) {
    excFunc(request, exception);
}

function excFunc(request, exception) {
    //enabled = false;
    statusbar_settext('An exception occured while updating. Perhaps the client shutdown?');
}

function callInProgress (xmlhttp) {
    if (xmlhttp.readyState == 0 || xmlhttp.readyState == 4) {
        return false;
    } else {
        return true;
    }
}
    
Ajax.Responders.register({
    onCreate: function(request) {        
        if (wus_open) {
            wus_addrequest(request.url);
        }
    }
});