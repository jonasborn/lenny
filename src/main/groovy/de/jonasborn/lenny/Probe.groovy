package de.jonasborn.lenny

import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
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
        this.ffprobe = new FFprobe(ffprobe.getAbsolutePath());
        this.supportedVideo = supportedVideo
        this.supportedAudioFormat = supportedAudioFormat
        this.supportedAudioLayout = supportedAudioLayout
    }

    public boolean isVideo(File file) {
        if (!file.exists()) return false;
        return tika.detect(file).contains("video")
    }

    public Boolean isCompatible(File file) {
        if (file == null || !file.exists() || !isVideo(file)) return null

        try {
            boolean videoSupported = isVideoCompatible(file);
            boolean audioSupported = isAudioCompatible(file);
            return (videoSupported && audioSupported)
        } catch (Exception e) {
            e.printStackTrace()
            return true
        }
    }

    public boolean isVideoCompatible(File file) {
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
        def streams = probeResult.streams;
        return streams.findAll { it.codec_type == FFmpegStream.CodecType.VIDEO }
                .find { supportedVideo.contains(it.codec_name) }
    }


    public boolean isAudioCompatible(File file) {
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
        def streams = probeResult.streams;

        return streams.findAll { it.codec_type == FFmpegStream.CodecType.AUDIO }
                .find { supportedAudioFormat.contains(it.codec_name) && supportedAudioLayout.contains(it.channel_layout) }
    }

    public void printDetails(File source, File target) {
        if (!source.exists()) return;
        FFmpegProbeResult probeResult = ffprobe.probe(source.getAbsolutePath());
        def format = probeResult.format
        def streams = probeResult.streams

        Table.create(TextAlignment.CENTER).strong().add("Source file details").strong().render()
        Table.create().add(source.getAbsolutePath()).normal().render()

        Table.create(TextAlignment.CENTER).strong().add("Format details").strong().render()
        def t = Table.create().add("Format", "Duration", "Bit rate").normal()
        t.add(format.format_name, format.duration, format.bit_rate)
        t.normal().render()

        Table.create(TextAlignment.CENTER).strong().add("Video details").strong().render()
        t = Table.create().add("Index", "Stream", "Length", "Codec", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.VIDEO }.each {
            def supported = supportedVideo.contains(it.codec_name)
            t.addRow(it.index, "Video", it.duration, it.codec_name, supported)
        }
        t.render()

        Table.create(TextAlignment.CENTER).strong().add("Audio details").strong().render()
        t = Table.create().add("Index", "Stream", "Codec", "Channel", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.AUDIO }.each {
            def supported = supportedAudioFormat.contains(it.codec_name) && supportedAudioLayout.contains(it.channel_layout)
            t.addRow(it.index, "Audio", it.codec_name, it.channel_layout, supported)
        }
        t.render()

        Table.create(TextAlignment.CENTER).strong().add("Target file details").strong().render()
        Table.create().add(target.getAbsolutePath()).normal().render()
    }

    public void printResultDetails(File target) {
        if (!target.exists()) return;

        FFmpegProbeResult probeResult = ffprobe.probe(target.getAbsolutePath());
        def format = probeResult.format
        def streams = probeResult.streams

        Table.create(TextAlignment.CENTER).strong().add("Result file details").strong().render()
        Table.create().add(target.getAbsolutePath()).normal().render()

        Table.create(TextAlignment.CENTER).strong().add("Format details").strong().render()
        def t = Table.create().add("Format", "Duration", "Bit rate").normal()
        t.add(format.format_name, format.duration, format.bit_rate)
        t.normal().render()

        Table.create(TextAlignment.CENTER).strong().add("Video details").strong().render()
        t = Table.create().add("Index", "Stream", "Length", "Codec", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.VIDEO }.each {
            def supported = supportedVideo.contains(it.codec_name)
            t.addRow(it.index, "Video", it.duration, it.codec_name, supported)
        }
        t.render()

        Table.create(TextAlignment.CENTER).strong().add("Audio details").strong().render()
        t = Table.create().add("Index", "Stream", "Codec", "Channel", "Supported").strong()
        streams.findAll { it.codec_type == FFmpegStream.CodecType.AUDIO }.each {
            def supported = supportedAudioFormat.contains(it.codec_name) && supportedAudioLayout.contains(it.channel_layout)
            t.addRow(it.index, "Audio", it.codec_name, it.channel_layout, supported)
        }
        t.render()
    }

    static void main(String[] args) {


    }

}
