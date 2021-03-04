package de.jonasborn.lenny


import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream
import org.apache.tika.Tika

class Probe {

    public FFprobe ffprobe

    private List<String> supportedVideo = ["h264"]
    private List<String> supportedAudioFormat = ["aac", "ac3", "mp3"]
    private List<String> supportedAudioLayout = ["stereo"]

    private static Tika tika = new Tika()

    Probe(File ffprobe, List<String> supportedVideo, List<String> supportedAudioFormat, List<String> supportedAudioLayout) {
        this.ffprobe =  new FFprobe(ffprobe.getAbsolutePath());
        this.supportedVideo = supportedVideo
        this.supportedAudioFormat = supportedAudioFormat
        this.supportedAudioLayout = supportedAudioLayout
    }

    public   boolean isVideo(File file) {
        return tika.detect(file).contains("video")
    }

    public   Boolean isCompatible(File file) {
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

    public   boolean isVideoCompatible(File file) {
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
        def streams = probeResult.streams;
        boolean videoSupported = false;

        def t = Table.create().add("Index", "Stream", "Length", "Codec", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.VIDEO }.each {
            def supported = supportedVideo.contains(it.codec_name)
            if (supported) videoSupported = true
            t.addRow(it.index, "Video", it.duration_ts, it.codec_name, supported)
        }
        t.render()
        return videoSupported
    }


    public   boolean isAudioCompatible(File file) {
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
        def streams = probeResult.streams;
        boolean audioSupported = false;

        def t = Table.create().add("Index", "Stream", "Codec", "Channel", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.AUDIO }.each {
            def supported = supportedAudioFormat.contains(it.codec_name) && supportedAudioLayout.contains(it.channel_layout)
            if (supported) audioSupported = true
            t.addRow(it.index, "Audio", it.codec_name, it.channel_layout, supported)
        }
        t.render()
        return audioSupported
    }


    static void main(String[] args) {
        println Probe.isCompatible(new File("examples/ccnotworking1.avi"))
    }

}
