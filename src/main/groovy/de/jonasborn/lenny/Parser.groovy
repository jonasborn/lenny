package de.jonasborn.lenny

import net.sourceforge.argparse4j.ArgumentParsers
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
    String targetFormat
    boolean deleteOriginal
    Long timeout

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
                .required(false)

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
                .setDefault("ac3")
                .help("Target audio format to convert to")
                .required(true)

        parser.addArgument("-tf", "--targetformat")
                .setDefault("mp4")
                .help("Target container format to use")
                .required(false)

        parser.addArgument("-do", "--deleteoriginal")
                .setDefault(false)
                .help("Delete source file after convert")
                .required(false)

        parser.addArgument("-to", "--timeout")
                .setDefault(null)
                .help("Timeout to exit after")
                .required(false)

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
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
        this.targetFormat = ns.getString("targetformat")
        this.deleteOriginal = ns.getBoolean("deleteoriginal")
        this.timeout = ns.getLong("timeout")
    }

}
