# AudioSolutions

AudioSolutions is a music management application which can be used to organize your music collection. You can set ID3 tags for MP3 files, rename a lot of files in one step by using various rules, search for audio files in your collection and so on. AudioSolutions provides many features related to audio data:
- Open and select files you want to work with
- Set audio data to these files by
- Save these files with audio data in AudioSolutions
- Use audio data for other actions, e. g. write ID3 tags or rename files 
- Search for audio files

However, AudioSolutions is not a music editing software which are able to edit the audio stream itself. There are many good software like Audacity for this kind of task.

## Screenshots

![Search](screenshots/audiosolutions-01-search.png?raw=true "Search by artist and album")  

![Search](screenshots/audiosolutions-02-rename.png?raw=true "Rename files")  

![Search](screenshots/audiosolutions-03-structure.png?raw=true "Read audio data by file structure")  

## Install

1. Install java:

```
sudo apt install openjdk-17-jdk
```

2. Download latest AudioSolutions version from /releases/.
3. Unpack the file into any directory or use opt.py:

```
sudo opt.py update audiosolutions --delete --keep /opt/audiosolutions/audiosolutions.ini audiosolutions-9.0.0-linux-x86_64.tar.xz
```

4. (optional) Install third party tools:

```
sudo apt install faac faad flac lame libmad0 libmpcdec6 mppenc vorbis-tools wavpack 
```

5. Run audiosolutions binary

