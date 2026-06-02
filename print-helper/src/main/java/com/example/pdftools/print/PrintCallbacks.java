package com.example.pdftools.print;

import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PageRange;

public final class PrintCallbacks {

    public interface LayoutCallback {
        void onLayoutFinished(PrintDocumentInfo info, boolean changed);
        void onLayoutFailed(CharSequence error);
    }

    public interface WriteCallback {
        void onWriteFinished(PageRange[] pages);
        void onWriteFailed(CharSequence error);
    }

    public static class MyLayoutResultCallback extends PrintDocumentAdapter.LayoutResultCallback {
        private final LayoutCallback callback;

        public MyLayoutResultCallback(LayoutCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
            callback.onLayoutFinished(info, changed);
        }

        @Override
        public void onLayoutFailed(CharSequence error) {
            callback.onLayoutFailed(error);
        }
    }

    public static class MyWriteResultCallback extends PrintDocumentAdapter.WriteResultCallback {
        private final WriteCallback callback;

        public MyWriteResultCallback(WriteCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onWriteFinished(PageRange[] pages) {
            callback.onWriteFinished(pages);
        }

        @Override
        public void onWriteFailed(CharSequence error) {
            callback.onWriteFailed(error);
        }
    }
}
