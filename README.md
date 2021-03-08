# About
Lenny is a automatic movie detection and conversion tool.
It detects unsupported video files and converts them to a different format.

# Flow
- Search for next video file by detecting mime type with Tika
- Detect current streams with ffprobe
- Compare the streams with supported formats
- If not supported, convert the video
- Go to start


# Why
Because I've got a whole collection of old movies in a not ChromeCast compatible
format, and I'm annoyed to convert them on my own by hand.

# How to use

## How the help looks like

``` bash
usage: lenny [-h] -s SOURCE -t TARGET -ffprobe FFPROBE -ffmpeg FFMPEG -sv [SUPPORTEDVIDEO [SUPPORTEDVIDEO ...]] -sa [SUPPORTEDAUDIO [SUPPORTEDAUDIO ...]] -sal [SUPPORTEDAUDIOLAYOUT [SUPPORTEDAUDIOLAYOUT ...]] -tv TARGETVIDEO
             -ta TARGETAUDIO [-tf TARGETFORMAT] [-do DELETEORIGINAL] [-to TIMEOUT]
```

## Parameters

|  Parameter         | Description                                                            |
|--------------------|------------------------------------------------------------------------|
| -h                 | Show some help info                                                    |
| -s <dir>           | Source directory (required)                                            |
| -t <dir>           | Target directory (required)                                            |
| -ffprobe <file>    | Path to FFprobe (defaults to /usr/bin/ffprobe)                         |
| -ffmpeg <file>     | Path to FFmpeg (defaults to /usr/bin/ffmpeg)                           |
| -sv <[]>           | List of supported video formats (defaults to [h264])                   |
| -sa <[]>           | List of supported audio formats (defaults to [acc, ac3, mp3])          |
| -sal <[]>          | List of supported audio layouts (defaults to [stereo])                 |
| -tv <audio format> | Target video to convert to (like h264)                                 |
| -ta <audio format> | Target audio to convert to (like acc)                                  |
| -do                | Delete original file after converting                                  |
| -to                | Timeout to stop afterwards                                             |
| -dc                | Copy files, even if there is no change                                 |
| -si                | Do not index all source files before starting                          |
| -bs                | Set the buffer size of the copy handler (defaults to 8192)             |
| -mk                | Set the marker to be used for the files (defaults to lenny)            |
| -ex                | Set phrases to mach against in file paths and exclude. Case sensitive! |

>What about spaces? Windows got some "special" handling of spaces in paths, therefore
> using single quotes (') around a path will help, you don't have to use double quotes.

# Todo
- Add a function to set the supported stream as primary and optionally remove all other unsupported strems
- Add a function to copy the stream, if the stream is in a supported format
