package de.jonasborn.lenny

import com.google.common.io.Files

class Main {

    static active = true

    static Parser parser
    static Converter converter
    static Probe probe


    static void main(String[] args) {


        Main.addShutdownHook {
            active = false
        }

        parser = new Parser(args)

        if (!parser.target.exists()) parser.target.mkdirs()

        probe = new Probe(
                parser.ffprobe,
                parser.supportedVideo,
                parser.supportedAudio,
                parser.supportedAudioLayout
        )

        converter = new Converter(
                probe,
                parser.ffprobe,
                parser.ffmpeg,
                parser.targetVideo,
                parser.targetAudio,
                parser.targetFormat
        )

        def start = System.currentTimeMillis()

        def total = -1;
        def pos = 0;
        if (!parser.skipIndex) total = Index.list(parser.source).findAll {
            if ((pos % 100) == 0) println "Indexing file $pos" + "\r"
            pos++;
            return !it.name.contains(".lenny") && probe.isVideo(it) && !isExcluded(parser, it)
        }.size()

        def finished = 1;

        while (active) {
            Table.create().strong().add("Handling file " + finished + " of " + total).strong().render();
            run()
            if (parser.timeout != null && System.currentTimeMillis() > (start + parser.timeout)) System.exit(1)
            finished++;
        }

    }

    static void run() {

        def source = Index.next(parser.source, {
            !it.name.contains(".lenny") && probe.isVideo(it) && !isExcluded(parser, it)
        })

        if (source == null) {
            println "No source file was found, looks like I'm finished!"
            System.exit(0)
        }

        if (source != null) {

            def targetDir = new File(
                    parser.target,
                    source.parentFile.getAbsolutePath().replace(parser.source.getAbsolutePath(), "")
            )
            if (!targetDir.exists()) targetDir.mkdirs()

            def targetFileName = Files.getNameWithoutExtension(source.name) + ".lenny.mkv"
            def target = new File(targetDir, targetFileName)

            probe.printDetails(source, target);

            if (!probe.isCompatible(source)) {
                try {
                    converter.convert(source, target)
                } catch (Exception e) {
                    e.printStackTrace()
                    move(source, "lennybroken")
                    return;
                }
            } else {
                Copier.copyFile(parser.bufferSize, source, target)
            }

            if (parser.deleteOriginal) {
                source.delete()
            } else {
                move(source, "lennyoriginal")
            }

            println ""

        }


    }

    public static void move(File source, String suffix) {
        def sourceMoveTargetName = Files.getNameWithoutExtension(source.name) + "." + suffix
        def sourceMoveTarget = new File(source.parentFile, sourceMoveTargetName)
        if (sourceMoveTarget.exists()) sourceMoveTarget.delete()
        Files.move(source, sourceMoveTarget)
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

    public boolean isExcluded(Parser parser, File f) {
        def p = f.getAbsolutePath()
        return (parser.exclude.find {p.contains(it)} != null)
    }

}
