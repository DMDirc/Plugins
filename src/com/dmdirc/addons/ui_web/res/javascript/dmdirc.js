var enabled = true;
var clientID = Math.ceil(Math.random() * 1000000000);
var activeWindow = null;
var windows = {};
var interval;

var treeview, nicklist;

function dmdirc_start() {
    if (interval) {
        clearInterval(interval);
    }

    treeview = new Treeview($('#treeview'));
    nicklist = new Nicklist($('#nicklist'));

    setTimeout(doUpdate, 100);
}

function log() {
    console && console.log && console.log.apply(console, arguments);
}

(function() {
    Treeview = function(element) {
        this.element = element;
    }

    var classOrder = ['globalwindow', 'server', 'raw', 'channel', 'query'];

    /**
     * Compares two treeview nodes by their class and text.
     *
     * @param a The first node to be compared
     * @param b The second node to be compared
     * @return -1 if the first node should be before the second, 0 if the
     * nodes are equal, and +1 otherwise
     */
    function compareTreeNodes(a, b) {
        var classA = classOrder.indexOf(a.attr('class')),
        classB = classOrder.indexOf(b.attr('class'));

        if (classA === classB) {
            var textA = a.text().toLowerCase(), textB = b.text().toLowerCase();

            if (textA === textB) {
                return 0;
            } else {
                return textA < textB ? -1 : +1;
            }
        } else {
            return classA < classB ? -1 : +1;
        }
    }

    $.extend(Treeview.prototype, {
        remove: function(id) {
            $('#' + id).remove();
        },

        add: function(name, id, type, parent) {
            var parentNode;

            if (parent) {
                parentNode = $('#' + parent);

                // Create a new child <ul/> if needed
                var children = parentNode.children('ul');

                if (children.length === 0) {
                    parentNode.append($('<ul/>'));
                    children = parentNode.children('ul');
                }

                parentNode = children;
            } else {
                parentNode = this.element;
            }

            var newNode = $('<li/>').attr('id', id).addClass(type);

            var wrapperNode = $('<div/>').css('cursor', 'pointer').text(name);
            wrapperNode.click(function() { window_show(id); });

            wrapperNode.appendTo(newNode);

            var previousElement;
            parentNode.children().each(function() {
                if (compareTreeNodes($(this), newNode) < 0) {
                    previousElement = $(this);
                }
            });

            previousElement && newNode.insertAfter(previousElement) || newNode.prependTo(parentNode);
        },

        setactive: function(id) {
            if (activeWindow) {
                document.getElementById(activeWindow).style.fontWeight = 'normal';
            }

            activeWindow = id;
            document.getElementById(activeWindow).style.fontWeight = 'bold';
        }
    });
})();

(function() {
    Nicklist = function(element) {
        this.element = element;
    };

    $.extend(Nicklist.prototype, {
        show: function(window) {
            if (this.element.css('display') !== 'block') {
                document.getElementById('content').style.right = '240px';
                this.element.css('display', 'block');
            }

            $.ajax('/dynamic/nicklistrefresh', {
                data: {window: window},
                error: errFunc,
                success: handlerFunc
            });
        },

        clear: function() {
            this.element.empty();
        },

        add: function(nick) {
            this.element.append($('<li>').text(nick));
        },

        hide: function() {
            if (this.element.css('display') === 'block') {
                document.getElementById('content').style.right = '15px';
                this.element.css('display', 'none');
            }
        }
    });
})();

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

    $('#nsd').load('/static/newserverdialog.html');

    $.ajax('/dynamic/getprofiles', {
        error: errFunc,
        success: handlerFunc
    });

    $('#nsd').show('slow');
}

function nsd_ok() {
    var server = document.getElementById('nsd_server').value;
    var port = document.getElementById('nsd_port').value;
    var password = document.getElementById('nsd_password').value;
    var profile = document.getElementById('nsd_profile').value;

    if (!/^[^\s]+$/.test(server)) {
        alert("Server name cannot contain spaces");
        // TODO: new Effect.Pulsate('nsd_server', {pulses: 3});
        return;
    }

    if (!/^[0-9]+$/.test(port) || port < 1 || port > 65535) {
        alert("Port must be a number between 1 and 65535");
        // TODO: new Effect.Pulsate('nsd_port', {pulses: 3});
        return;
    }

    $.ajax('/dynamic/newserver', {
        data: {
            server: server,
            port: port,
            password: password,
            profile: profile
        },
        error: errFunc
    });

    nsd_cancel();
}

function nsd_cancel() {
    $('#nsd').hide('slow');
}

function profiles_clear() {
    $('.profilelist').empty();
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

    if (keynum === 13) {
        keynum = 10;
    }

    var control = e.ctrlKey;
    var shift = e.shiftKey;
    var alt = e.altKey;
    var el = document.getElementById('input');

    if (keynum === 10 && !control) {
        $.ajax('/dynamic/input', {
            data: {
                input: el.value,
                clientID: clientID,
                window:activeWindow
            },
            error: errFunc
        });

        el.value = '';
    } else if (keynum === 9 && !control) {
        $.ajax('/dynamic/tab', {
            data: {
                input: el.value,
                selstart: el.selectionStart,
                selend: el.selectionEnd,
                clientID: clientID,
                window: activeWindow
            },
            error: errFunc
        });

        return false;
    } else if (keynum === 38 || keynum === 40) {
        // up/down
        $.ajax('/dynamic/key' + ((keynum === 38) ? 'up' : 'down'), {
            data: {
                input: el.value,
                selstart: el.selectionStart,
                selend: el.selectionEnd,
                clientID: clientID,
                window: activeWindow
            },
            error: errFunc
        });

        return false;
    } else if (control && (keynum === 10 || keynum === 66 || keynum === 70
               || keynum === 73 || keynum === 75 || keynum === 79 || keynum === 85)) {
        $.ajax('/dynamic/key', {
            data: {
                input: el.value,
                selstart: el.selectionStart,
                selend: el.selectionEnd,
                clientID: clientID,
                key: keynum,
                ctrl: control,
                shift: shift,
                alt: alt,
                window: activeWindow
            },
            error: errFunc
        });

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
    if (activeWindow === id) {
        $('#content').empty();
    }

    windows[id].lines = [];
}

function window_addline(id, line) {
    var p = document.createElement('p');
    p.innerHTML = line;

    if (activeWindow === id) {
        document.getElementById('content').appendChild(p);
        var objDiv = document.getElementById('content');
        objDiv.scrollTop = objDiv.scrollHeight;
    }

    windows[id].lines[windows[id].lines.length] = p;
}

function window_create(window, parent) {
    treeview.add(window.name, window.id, window.type, parent && parent.id);
    windows[window.id] = window;
    windows[window.id].lines = [];
    window_show(window.id);

    $.ajax('/dynamic/windowrefresh', {
        data: {window: window.id},
        error: errFunc,
        success: handlerFunc
    });
}

function window_close(id) {
    treeview.remove(id);
    delete windows[id];

    if (activeWindow == id) {
        // TODO: Focus another window, or reset textview/title/etc if there
        //       are no more windows
    }
}

function window_show(id) {
    treeview.setactive(id);
    title_settext(windows[id].title)

    var className = document.getElementById(id).className;

    if (className === 'channel')  {
        nicklist.show(activeWindow);
    } else {
        nicklist.hide();
    }

    if (className === 'server' || className === 'channel'
        || className === 'input' || className === 'query') {
        inputarea_show();
    } else {
        inputarea_hide();
    }

    $('#content').empty();
    var objDiv = document.getElementById('content');

    windows[id].lines.forEach(function(x) {
        objDiv.appendChild(x);
    });

    objDiv.scrollTop = objDiv.scrollHeight;
}

function title_settext(newText) {
    var title = newText + " - DMDirc web interface";
    $('#title').text(title);
    document.title = title;
}

function statusbar_settext(newText) {
    $('#statusbar_main').text(newText);
    //new Effect.Highlight('statusbar_main', {endcolor: "#c0c0c0", restorecolor: "#c0c0c0"});
}

function link_hyperlink(url) {
    statusbar_settext('Opening ' + url + '...');
    window.open(url);
}

function link_channel(channel) {
    $.ajax('/dynamic/joinchannel', {
        data: {
            clientID: clientID,
            source: activeWindow,
            channel: channel
        },
        error: errFunc
    });
}

function link_query(user) {
    $.ajax('/dynamic/openquery', {
        data: {
            clientID: clientID,
            source: activeWindow,
            target: user
        },
        error: errFunc
    });
}

function doUpdate() {
    $.ajax('/dynamic/feed', {
        data: {clientID: clientID},
        scriptCharset: 'UTF-8',
        success: updateHandlerFunc,
        error: updateErrFunc
    });
}

function updateHandlerFunc(transport) {
    try {
        handlerFunc(transport);
        doUpdate();
    } catch (ex) {
        log('Exception when handling update results', ex);
    }
}

function handlerFunc(data) {
    enabled = true;

    for (var i = 0; i < data.length; i++) {
        var event = data[i];

        if (event.type === 'statusbar') {
            statusbar_settext(event.arg1);
        } else if (event.type === 'clearprofiles') {
            profiles_clear();
        } else if (event.type === 'addprofile') {
            profiles_add(event.arg1);
        } else if (event.type === 'newwindow') {
            window_create(event.arg1);
        } else if (event.type === 'closewindow') {
            window_close(event.arg1);
        } else if (event.type === 'newchildwindow') {
            window_create(event.arg1[1], event.arg1[0]);
        } else if (event.type === 'clearwindow') {
            window_clear(event.arg1);
        } else if (event.type === 'lineadded') {
            window_addline(event.arg1.window, event.arg1.message);
        } else if (event.type === 'settext') {
            input_settext(event.arg1);
        } else if (event.type === 'clearnicklist') {
            nicklist.clear();
        } else if (event.type === 'addnicklist') {
            nicklist.add(event.arg1);
        } else if (event.type === 'setcaret') {
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

    log('Error when performing remote call, status: ', transport.status,
        ' text: ', transport.statusText, ' response: ', transport.responseText);
}