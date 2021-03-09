package de.jonasborn.lenny

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace

class Parser {

    File source
    File target
    File ffprobe
    File ffmpeg
    List<String> supportedVideo
    List<String> supportedAudio
    List<String> supportedAudioLayout
    String targetVideo
    String targetAudio
    int targetChannels
    String targetFormat
    boolean deleteOriginal
    Long timeout
    boolean docopy;
    int bufferSize;
    boolean skipIndex;
    String marker;
    List<String> exclude

    public Parser(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("lenny").build()
                .defaultHelp(true)
                .description("Detect and convert videos to ChromeCast supported ones using ffmpeg");

        parser.addArgument("-s", "--source")
                .setDefault("")
                .required(true)
                .help("Source directory")

        parser.addArgument("-t", "--target")
                .setDefault(null)
                .help("Target directory")
                .required(true)

        parser.addArgument("-ffprobe")
                .setDefault("/usr/bin/ffprobe")
                .help("Path to the ffprobe")
                .required(true)

        parser.addArgument("-ffmpeg")
                .setDefault("/usr/bin/ffmpeg")
                .help("Path to the ffmpeg")
                .required(true)

        parser.addArgument("-sv", "--supportedvideo")
                .nargs("*")
                .setDefault(["h264"])
                .help("A list of all supported video formats")
                .required(false)

        parser.addArgument("-sa", "--supportedaudio")
                .nargs("*")
                .setDefault(["aac", "ac3", "mp3"])
                .help("A list of all supported audio formats")
                .required(false)

        parser.addArgument("-sal", "--supportedaudiolayout")
                .nargs("*")
                .setDefault(["stereo"])
                .help("A list of supported audio layouts")
                .required(false)

        parser.addArgument("-tv", "--targetvideo")
                .setDefault("h264")
                .help("Target video format to convert to")
                .required(true)

        parser.addArgument("-ta", "--targetaudio")
                .setDefault("aac")
                .help("Target audio format to convert to")
                .required(true)

        parser.addArgument("-tc", "--targetchannels")
                .setDefault(2)
                .help("Target channel amount")
                .type(Integer.class)
                .required(true);

        parser.addArgument("-tf", "--targetformat")
                .setDefault("mp4")
                .help("Target container format to use")
                .required(false)

        parser.addArgument("-do", "--deleteoriginal")
                .setDefault(false)
                .help("Delete source file after convert")
                .required(false)
                .action(Arguments.storeTrue())

        parser.addArgument("-to", "--timeout")
                .setDefault(null)
                .help("Timeout to exit after")
                .required(false)

        parser.addArgument("-dc", "--docopy")
                .setDefault(null)
                .help("Do copy if no need to convert")
                .required(false)
                .action(Arguments.storeTrue())

        parser.addArgument("-si", "--skipindex")
                .setDefault(null)
                .help("Do not search for all files before starting")
                .required(false)
                .action(Arguments.storeTrue())


        parser.addArgument("-bs", "--buffersize")
                .setDefault(8192)
                .help("Size of the copy fugger")
                .required(false)
                .type(Integer.class)

        parser.addArgument("-mk", "--marker")
                .setDefault("lenny")
                .help("Name of the marker used to mark original and new files")
                .required(false)

        parser.addArgument("-ex", "--exclude")
                .setDefault([])
                .help("A list of string used to exclude files if their path contains one of them")
                .required(false)
                .nargs("*")

        List<String> newArgs = []
        String last = ""
        args.each {
            if (it.startsWith("'")) {
                last = it.substring(1, it.length())
            } else if (it.endsWith("'")) {
                newArgs.add(last + " " + it.substring(0, it.length() - 1))
                last = null
            } else if (it.startsWith("-")) {
                if (last != null && last.length() > 0) newArgs.add(last.substring(0, last.length() - 1))
                last = null
                newArgs.add(it)
            } else if (last != null) {
                last = last + " " + it
            } else {
                newArgs.add(it)
            }
        }

        Namespace ns = null;
        try {
            ns = parser.parseArgs(newArgs as String[]);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }


        this.source = new File(ns.getString("source"))
        this.target = new File(ns.getString("target"))
        this.ffprobe = new File(ns.getString("ffprobe"))
        this.ffmpeg = new File(ns.getString("ffmpeg"))
        this.supportedVideo = ns.getList("supportedvideo")
        this.supportedAudio = ns.getList("supportedaudio")
        this.supportedAudioLayout = ns.getList("supportedaudiolayout")
        this.targetVideo = ns.getString("targetvideo")
        this.targetAudio = ns.getString("targetaudio")
        this.targetChannels = ns.getInt("targetchannels")
        this.targetFormat = ns.getString("targetformat")
        this.deleteOriginal = ns.getBoolean("deleteoriginal")
        this.timeout = ns.getLong("timeout")
        this.docopy = ns.getBoolean("docopy")
        this.bufferSize = ns.getInt("buffersize")
        this.skipIndex = ns.getBoolean("skipindex")
        this.marker = ns.getString("marker")
        this.exclude = ns.getList("exclude")
    }

}
