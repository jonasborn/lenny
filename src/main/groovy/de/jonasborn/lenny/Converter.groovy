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
        if (video == null) throw new StreamNotFoundException("No video file found!")


        def currentVideo = videoCodec
        def currentAudio = audioCodec

        if (probe.isVideoCompatible(source)) currentVideo = "copy"

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
                .setPreset("ultrafast")
                .done()

        def exec = new FFmpegExecutor(ffmpeg, ffprobe)
        exec.createJob(builder, listener(probeResult)).run()
    }

    static void main(String[] args) {
        //convert(new File("examples/ccnotworking1.avi"), new File("examples/ccconv1.mp4"))
    }

    public static ProgressListener listener(FFmpegProbeResult inp) {
        def total = inp.getFormat().duration * TimeUnit.SECONDS.toNanos(1)

        new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                def currentTime = progress.out_time_ns
                double percentage = (currentTime / total);
                def text = [
                        Msg.fixDouble(percentage * 100, 3, 2) + "%",
                        Msg.convertTime((total - currentTime) as long),
                        Msg.fixDouble(progress.speed, 3, 2) + " fps"
                ]
                Msg.process("CON", (percentage * 100) as int, text)


            }
        }
    }


}
