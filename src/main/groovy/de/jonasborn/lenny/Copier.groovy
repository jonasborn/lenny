package de.jonasborn.lenny

class Copier {

    public static void copyFile(int bufferSize, File src, File dst) throws IOException {
        try {
            InputStream ips = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            // Transfer bytes from in to out
            long expectedBytes = src.length(); // This is the number of bytes we expected to copy..
            long totalBytesCopied = 0; // This will track the total number of bytes we've copied
            byte[] buf = new byte[bufferSize];
            int len = 0;
            while ((len = ips.read(buf)) > 0) {
                out.write(buf, 0, len);
                totalBytesCopied += len;
                int progress = (int) Math.round(((double) totalBytesCopied / (double) expectedBytes) * 100)
                Msg.process("cpy", progress, [progress.toString().padLeft(3) + "%"])
            }
            System.out.println("");
            ips.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
