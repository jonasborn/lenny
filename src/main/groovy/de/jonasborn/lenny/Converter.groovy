package de.jonasborn.lenny

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFmpegUtils
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream
import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener

import java.util.concurrent.TimeUnit

class Converter {

    private static int length = 80

    Probe probe;

    public FFprobe ffprobe
    public FFmpeg ffmpeg

    String videoCodec;
    String audioCodec
    String format


    Converter(Probe probe, File ffprobe, File ffmpeg, String videoCodec, String audioCodec, String format) {
        this.probe = probe;
        this.ffprobe = new FFprobe(ffprobe.getAbsolutePath())
        this.ffmpeg = new FFmpeg(ffmpeg.getAbsolutePath())


        if (!ffprobe.exists()) {
            println "FFprobe was not found at " + ffprobe.getAbsolutePath()
            System.exit(1)
        }

        if (!ffmpeg.exists()) {
            println "FFmpeg was not found at " + ffmpeg.getAbsolutePath()
        }

        this.videoCodec = videoCodec
        this.audioCodec = audioCodec
        this.format = format
    }

    public void convert(File source, File target) {

        def probeResult = ffprobe.probe(source.getAbsolutePath())
        //def audio = probe.streams.find { it.codec_type == FFmpegStream.CodecType.AUDIO }
        def video = probeResult.streams.find { it.codec_type == FFmpegStream.CodecType.VIDEO }

        def currentVideo = videoCodec
        def currentAudio = audioCodec

        if (probe.isVideoCompatible(source, false)) currentVideo = "copy"


        FFmpegBuilder builder = new FFmpegBuilder()
        builder.setInput(source.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(target.getAbsolutePath())
                .setFormat(format)
                .setAudioChannels(2)
                .setAudioCodec(audioCodec)
                .setVideoCodec(currentVideo)
                .setVideoFrameRate(24, 1)
                .setVideoResolution(video.width, video.height)
                .done()

        def exec = new FFmpegExecutor(ffmpeg, ffprobe)
        exec.createJob(builder, listener(probeResult)).run()
    }

    static void main(String[] args) {


        convert(new File("examples/ccnotworking1.avi"), new File("examples/ccconv1.mp4"))
    }

    public static ProgressListener listener(FFmpegProbeResult inp) {
        def total = inp.getFormat().duration * TimeUnit.SECONDS.toNanos(1)

        new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                def currentTime = progress.out_time_ns
                double percentage = (currentTime / total);
                def amount = (percentage * 100).round(2)
                def text = [
                        fixDouble(percentage * 100, 3, 2) + "%",
                        convertTime((total - currentTime) as long),
                        fixDouble(progress.speed, 3, 2) + " fps"
                ].join(" - ")
                def textLength = text.size() + 3
                def area = "".padRight((int) ((length - textLength) / 100) * amount, "#").padRight((length - textLength), ".")
                def message = "[" + area + "] " + text
                System.out.print(message + '\r');

            }
        }
    }

    private static String convertTime(Long l) {
        if (l  < 1) l = 1;
        def s = FFmpegUtils.toTimecode(l, TimeUnit.NANOSECONDS).toString().padRight(12, "0")
        def spl = s.split("\\.")
        def end = spl[1];
        if (end.length() > 2) end = end.substring(0, 2);
        return spl[0] + "." + end
    }

    private static String fixDouble(double d, int left, int right) {
        def s = d.toString()
        if (!s.contains(".")) {
            s = s + ".00"
        }
        def p = s.split("\\.")
        def a = p[0].padLeft(left, "0")
        def b = p[1].padRight(right, "0").substring(0, right)
        return a + "." + b
    }

}
