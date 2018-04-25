![Logo](Logo/horizontal.png)

## Description

Painless Music Player is a lightweight music player based on ExoPlayer and MediaStore library

Min API level 16

## Features

 - Artists browser
 - Albums browser
 - Genres browser
 - Tracks browser
 - Playlists browser
 - Live playlists (Recently played albums, recently scanned, random playlist)
 - List filters
 - Seek
 - Themes (Light, Dark and DayNight theme)
 - Equalizer and Bass Boost effects
 - Playback modes (suffle/repeat)
 - Removing from playlist
 - Deleting files, albums and playlist
 - Home screen widget
 - Notifies now playing for LastFM app
 - Android Wear app
 - Android Auto support
 - Remote controls support (headset, car audio)
 - RTL support
 
Missing features you might expect from a Music Player
 - No folder browser
 - No tag editor
 - No album art fetching from network (supports only built-in album art from MediaStore)
 - No tablet UI
 - No fast-forward/rewind (only seek supported)
 
## Supported formats

All formats that are supported by [ExoPlayer](https://google.github.io/ExoPlayer/supported-formats.html).

Does not support FLAC.

## Architecture

Mostly crap, recently I've been refactoring dependency injection and breaking into multi-layer architecture.
It still needs some DI reorganizing, better MVVM architecture and tests. It needs lots of tests.

## Screenshots

![screenshot](/screenshots/recent_activity.png?raw=true)
![screenshot](/screenshots/now_playing.png?raw=true)
![screenshot](/screenshots/navigation_drawer.png?raw=true)
![screenshot](/screenshots/albums.png?raw=true)
![screenshot](/screenshots/effects.png?raw=true)
![screenshot](/screenshots/notification.png?raw=true)
![screenshot](/screenshots/lock_screen.png?raw=true)
![screenshot](/screenshots/settings.png?raw=true)

## License

```
Copyright 2016 Yaroslav Mytkalyk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```
