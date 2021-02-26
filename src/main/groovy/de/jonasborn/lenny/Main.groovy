package de.jonasborn.lenny

import com.google.common.io.Files

class Main {

    static active = true

    static Parser parser
    static Converter converter

    static void main(String[] args) {
        args = [
                "-s", "/home/jonas/git/lenny/examples",
                "-t", "/home/jonas/git/lenny/exout",
                "-ffprobe", "/usr/bin/ffprobe",
                "-ffmpeg", "/usr/bin/ffmpeg",
                "-tf", "mp4",
                "-ta", "ac3",
                "-tv", "h264",
                "-sal", "stereo",
                "-sa", "ac3,mp3,acc",
                "-sv", "h264"
        ]

        args = []

        Main.addShutdownHook {
            active = false
        }

        parser = new Parser(args)
        converter = new Converter(
                parser.ffprobe,
                parser.ffmpeg,
                parser.targetVideo,
                parser.targetAudio,
                parser.targetFormat
        )
        def start = System.currentTimeMillis()
        while (active) {
            run()
            if (parser.timeout != null && System.currentTimeMillis() > (start + parser.timeout)) System.exit(1)
        }

    }

    static void run() {


        def source = Index.next(parser.source, {
            !it.name.contains(".lenny") && Probe.isVideo(it) && !Probe.isCompatible(it)
        })

        if (source == null) {
            println "No source file was found, looks like I'm finished!"
            System.exit(0)
        }

        if (source != null) {
            def newTargetDir = new File(
                    parser.target,
                    source.parentFile.getAbsolutePath().replace(parser.source.getAbsolutePath(), "")
            )

            def resultName = Files.getNameWithoutExtension(source.name) + ".lenny.mkv"
            def resultFile = new File(newTargetDir, resultName)
            def newTargetName = Files.getNameWithoutExtension(source.name) + ".lennyoriginal"

            def newTarget = new File(source.parentFile, newTargetName)
            if (newTarget.exists()) newTarget.delete()

            converter.convert(source, resultFile)

            if (parser.deleteOriginal) {
                source.delete()
            } else {
                Files.move(source, newTarget)
            }
        }


    }
/*
    static void maina(String[] args) {
        def source = Index.next(new File("examples"), {
            println it
            println Probe.isVideo(it)
            println !it.name.contains(".lenny.")


            Probe.isVideo(it) &&
                    !it.name.contains(".lenny.") &&
                    !Probe.isCompatible(it)
        })

        if (source != null) {

            println "Found file " + source.getPath() + " - attempting to convert"

            def dir = source.parentFile
            def name = source.name
            def ext = Files.getFileExtension(name)

            def file = new File(dir, name + ".lenny." + ext)
            def target = new File(dir, name + ".mp4")

            Files.move(source, file)
            Converter.convert(file, target)
        }
        println source
    }*/

}
