package de.jonasborn.lenny

import net.bramp.ffmpeg.FFmpegUtils
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream

import java.util.concurrent.TimeUnit

class Msg {

    public static void printVideoProbe(FFmpegProbeResult result, List<String> supportedVideo) {
        def t = Table.create().add("Index", "Stream", "Length", "Codec", "Supported").strong()
        def streams = result.streams;
        streams.findAll { it.codec_type == FFmpegStream.CodecType.VIDEO }.each {
            def supported = supportedVideo.contains(it.codec_name)
            t.addRow(it.index, "Video", it.duration, it.codec_name, supported)
        }
    }

    public static String convertTime(Long l) {
        if (l < 1) l = 1;
        def s = FFmpegUtils.toTimecode(l, TimeUnit.NANOSECONDS).toString().padRight(12, "0")
        if (!s.contains(".")) return s + ".00"
        def spl = s.split("\\.")
        def end = spl[1];
        if (end.length() > 2) end = end.substring(0, 2);
        return spl[0] + "." + end
    }

    public static String fixDouble(double d, int left, int right) {
        def s = d.toString()
        if (!s.contains(".")) {
            s = s + ".00"
        }
        def p = s.split("\\.")
        def a = p[0].padLeft(left, "0")
        def b = p[1].padRight(right, "0").substring(0, right)
        return a + "." + b
    }

    private static int length = 80

    public static void process(String title, int amount, List<Object> list) {
        if (amount > 100) amount = 100;
        def text = "- " +  list.join(" - ")
        def textLength = text.size() + 3 + title.length() + 2
        def area = "".padRight((int) ((length - textLength) / 100) * amount, "#").padRight((length - textLength), ".")
        def message = title.toUpperCase() + ": [" + area + "] " + text
        System.out.print(message + '\r');
    }



    static void main(String[] args) {
        process("CPY", 90, ["ABC"])
    }


}
