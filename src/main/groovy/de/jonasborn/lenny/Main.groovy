package de.jonasborn.lenny

import com.google.common.io.Files

class Main {

    static active = true

    static Parser parser
    static Converter converter
    static Probe probe


    static void main(String[] args) {
        if (System.getProperty("develop") != null) args = [
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

        //args = []*/

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
        while (active) {
            run()
            if (parser.timeout != null && System.currentTimeMillis() > (start + parser.timeout)) System.exit(1)
        }

    }

    static void run() {


        def source = Index.next(parser.source, {
            !it.name.contains(".lenny") && probe.isVideo(it)// && !probe.isCompatible(it)
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
            if (!newTargetDir.exists()) newTargetDir.mkdirs()

            def resultName = Files.getNameWithoutExtension(source.name) + ".lenny.mkv"
            def resultFile = new File(newTargetDir, resultName)
            def newTargetName = Files.getNameWithoutExtension(source.name) + ".lennyoriginal"

            def newTarget = new File(source.parentFile, newTargetName)
            if (newTarget.exists()) newTarget.delete()

            if (!probe.isCompatible(source, true)) {
                converter.convert(source, resultFile)
            } else {
                copyFile(source, resultFile)
            }

            if (parser.deleteOriginal) {
                source.delete()
            } else {
                Files.move(source, newTarget)
            }


        }


    }

    public static void copyFile(File src, File dst) throws IOException {
        try {
            InputStream ips = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            // Transfer bytes from in to out
            long expectedBytes = src.length(); // This is the number of bytes we expected to copy..
            long totalBytesCopied = 0; // This will track the total number of bytes we've copied
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = ips.read(buf)) > 0) {
                out.write(buf, 0, len);
                totalBytesCopied += len;
                int progress = (int) Math.round(((double) totalBytesCopied / (double) expectedBytes) * 100);
                System.out.print("Copy file " + src.name + " - " + progress + "%" + "\r");

            }
            System.out.println("");
            ips.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
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
