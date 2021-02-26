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

    public FFprobe ffprobe
    public FFmpeg ffmpeg

    String videoCodec;
    String audioCodec
    String format


    Converter(File ffprobe, File ffmpeg, String videoCodec, String audioCodec, String format) {
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

        def probe = ffprobe.probe(source.getAbsolutePath())
        //def audio = probe.streams.find { it.codec_type == FFmpegStream.CodecType.AUDIO }
        def video = probe.streams.find { it.codec_type == FFmpegStream.CodecType.VIDEO }


        FFmpegBuilder builder = new FFmpegBuilder()
        builder.setInput(source.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(target.getAbsolutePath())
                .setFormat(format)
                .setAudioChannels(2)
                .setAudioCodec(audioCodec)
                .setVideoCodec(videoCodec)
                .setVideoFrameRate(24, 1)
                .setVideoResolution(video.width, video.height)
                .done()

        def exec = new FFmpegExecutor(ffmpeg, ffprobe)
        exec.createJob(builder, listener(probe)).run()
    }

    static void main(String[] args) {


        convert(new File("examples/ccnotworking1.avi"), new File("examples/ccconv1.mp4"))
    }

    public static ProgressListener listener(FFmpegProbeResult inp) {

        def total = inp.streams.first().nb_frames
        def start = System.currentTimeMillis()

        new ProgressListener() {
            @Override
            public void progress(Progress progress) {

                //

                double percentage = (100 / total) * progress.frame / 100

                def time = (System.currentTimeMillis() - start)
                def frames = progress.frame

                def all = (total * time) / frames
                long remaining = (all - time) as long
                if (remaining < 0) remaining = 0.1;
                //frames = zeit
                //total = x

                def amount = (percentage * 100).round(2)
                def text = [
                        fixDouble(amount, 3, 2),
                        FFmpegUtils.toTimecode(remaining, TimeUnit.MILLISECONDS).toString().padRight(12, "0"),
                        fixDouble(progress.speed, 3, 2)
                ].join(" - ")
                def textLength = text.size() + 3
                def area = "".padRight((int) ((length - textLength) / 100) * amount, "#").padRight((length - textLength), ".")
                def message = "[" + area + "] " + text
                System.out.print(message + '\r');

                /*// Print out interesting information about the progress
                System.out.println(String.format(
                        "[%s] status:%s frame:%d time:%s ms rem:%s msfps:%.0f speed:%.2fx",
                        percentage * 100,
                        progress.status,
                        progress.frame,
                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                        FFmpegUtils.toTimecode(remaining, TimeUnit.MILLISECONDS),
                        progress.fps.doubleValue(),
                        progress.speed
                ));*/
            }
        }
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
