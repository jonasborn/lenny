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
                parser.targetChannels,
                parser.targetFormat
        )

        def start = System.currentTimeMillis()

        def total = -1;
        def excluded = 0;
        def pos = 0;
        if (!parser.skipIndex) {
            total = Index.countRecrusive(parser.source, {
                def ex = isExcluded(parser, it)
                if (ex) excluded++;
                if ((pos % 100) == 0) print "Indexing file $pos, $excluded files excluded".padRight(80) + "\r"
                pos++;
                return !it.name.contains("." + parser.marker) && probe.isVideo(it) && !ex
            })
        }
        println ""


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
            !it.name.contains("." + parser.marker) && probe.isVideo(it) && !isExcluded(parser, it)
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

            def targetFileName = Files.getNameWithoutExtension(source.name) + "." + parser.marker + ".mkv"
            def target = new File(targetDir, targetFileName)

            probe.printDetails(source, target);

            if (!probe.isCompatible(source)) {
                try {
                    converter.convert(source, target)
                } catch (Exception e) {
                    e.printStackTrace()
                    move(source, parser.marker + "broken")
                    return;
                }
            } else {
                Copier.copyFile(parser.bufferSize, source, target)
            }
            println ""
            probe.printResultDetails(target);

            if (parser.deleteOriginal) {
                source.delete()
            } else {
                move(source, parser.marker + "original")
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


    public static boolean isExcluded(Parser parser, File f) {
        def p = f.getAbsolutePath()
        return (parser.exclude.find { p.contains(it) } != null)
    }

}
