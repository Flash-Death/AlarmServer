AlarmServer
Contributor: Erik Wenkel

Source: https://github.com/hessionb/WeatherAlarmv2

License: GPLv2 or later

License URI: http://www.gnu.org/licenses/gpl-2.0.html

Description

This Server was designed to run in conjunction with the WeatherAlarm App v2.  It takes in a url parameter called location that is a zip code (ie ?location=24060).  Without the argument the server will print out No Location in json format.

It then spawns two thread to request the current weather at that location from weather.com and weather.gov. The Server parses the html pages for the relevant information and displays it in json format which is sent back to the app.  

License

Copyright (C) 2013 Erik Wenkel

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.