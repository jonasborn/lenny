package de.jonasborn.lenny


import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream
import org.apache.tika.Tika

class Probe {

    public static FFprobe ffprobe = new FFprobe(Config.config.ffprobe);

    private static List<String> chromeCastVideo = ["h264"]
    private static List<String> chromeCastAudioFormats = ["aac", "ac3", "mp3"]
    private static List<String> chromeCastAudioLayout = ["stereo"]

    private static Tika tika = new Tika()

    public static boolean isVideo(File file) {
        return tika.detect(file).contains("video")
    }

    public static Boolean isCompatible(File file) {
        if (file == null || !file.exists() || !isVideo(file)) return null

        try {
            Table.create().title("Checking codecs for " + file.name).render()

            boolean videoSupported = isVideoCompatible(file);
            boolean audioSupported = isAudioCompatible(file);

            println ""

            return (videoSupported && audioSupported)
        } catch (Exception e) {
            e.printStackTrace()
            return true
        }
    }

    public static boolean isVideoCompatible(File file) {
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
        def streams = probeResult.streams;
        boolean videoSupported = false;

        def t = Table.create().add("Stream", "Codec", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.VIDEO }.each {
            def supported = chromeCastVideo.contains(it.codec_name)
            if (supported) videoSupported = true
            t.addRow("Video", it.codec_name, supported)
        }
        t.render()
        return videoSupported
    }


    public static boolean isAudioCompatible(File file) {
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
        def streams = probeResult.streams;
        boolean audioSupported = false;

        def t = Table.create().add("Stream", "Codec", "Channel", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.AUDIO }.each {
            def supported = chromeCastAudioFormats.contains(it.codec_name) && chromeCastAudioLayout.contains(it.channel_layout)
            if (supported) audioSupported = true
            t.addRow("Audio", it.codec_name, it.channel_layout, supported)
        }
        t.render()
        return audioSupported
    }


    static void main(String[] args) {
        println Probe.isCompatible(new File("examples/ccnotworking1.avi"))
    }

}
