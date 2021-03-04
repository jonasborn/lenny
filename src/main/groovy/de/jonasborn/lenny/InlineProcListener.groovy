package de.jonasborn.lenny

import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener

import java.util.function.Consumer


class InlineProcListener implements ProgressListener{

    Consumer<Progress> consumer;

    InlineProcListener(Consumer<Progress> consumer) {
        this.consumer = consumer
    }

    @Override
    void progress(Progress progress) {
        consumer.accept(progress)
    }
}
