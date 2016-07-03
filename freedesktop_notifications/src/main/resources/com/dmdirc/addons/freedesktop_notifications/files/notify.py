#!/usr/bin/python
# -*- coding: utf-8 -*-

# Default Configuration (will be overridden by cmd line arguments)
APPLICATION    = ''        # name of the calling application (will be shown if now summary is available)
SUMMARY        = ''
BODY        = ''
ICON        = 'preferences-desktop-notification'
TIMEOUT        = 2000        # 0 means, never hide the message; value greater than 0 is the time in ms the message is shown

import sys
import getopt
import dbus

def main():
    dbus = DBus()
    args = CmdLine(sys.argv[1:])
    dbus.notify(app_name=args.getApplication(), app_icon=args.getIcon(), summary=args.getSummary(), body=args.getBody(), timeout=args.getTime())

class CmdLine():
    def __init__(self, argv):
        try:
            opts, args = getopt.getopt(argv, 'hs:a:i:t:', ['help', 'summary=', 'app=', 'icon=', 'time='])
        except getopt.GetoptError:
            usage()
            sys.exit(2)

        for opt, arg in opts:
            if opt in ('-h', '--help'):
                usage()
                sys.exit()
            elif opt in ('-s', '--summary'):
                self.__summary = arg
            elif opt in ('-a', '--app'):
                self.__app = arg
            elif opt in ('-i', '--icon'):
                self.__icon = arg
            elif opt in ('-t', '--time'):
                self.__time = arg

        if len(args) > 0:
            self.__body = ' '.join(args)

    def getApplication(self):
        try:
            return self.__app
        except AttributeError:
            return APPLICATION

    def getSummary(self):
        try:
            return self.__summary
        except AttributeError:
            return SUMMARY

    def getBody(self):
        try:
            return self.__body
        except AttributeError:
            return BODY

    def getIcon(self):
        try:
            return self.__icon
        except AttributeError:
            return ICON

    def getTime(self):
        try:
            return int(self.__time)
        except ValueError:
            return TIMEOUT
        except AttributeError:
            return TIMEOUT

def usage():
    """print usage message"""
    print 'Usage: notify.py [-a|--app=<application>] [-i|--icon=<icon name or path>] [-t|--time=<time>] [-s|--summary=<summary>] [<message>]'

class DBus():
    """Wrapper class for notify daemon dbus interface"""

    def __init__(self):
        self.__notify = dbus.SessionBus().get_object('org.freedesktop.Notifications', '/org/freedesktop/Notifications')
    
    def notify(self, app_name='', replaces_id=dbus.UInt32(), event_id='', app_icon='', summary='', body='', actions=dbus.Array(signature='s'), hints=dbus.Dictionary(signature='sv'), timeout=0):
        print self.__notify.Notify(app_name, replaces_id, app_icon, summary, body, actions, hints, timeout)

    def closeNotification(self, event_id):
        self.__notify.CloseNotification(event_id)

if __name__ == "__main__":
    main();
