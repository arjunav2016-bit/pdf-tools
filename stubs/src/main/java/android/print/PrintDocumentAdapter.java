package android.print;

public abstract class PrintDocumentAdapter {
    public static abstract class LayoutResultCallback {
        public LayoutResultCallback() {}
        public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {}
        public void onLayoutFailed(CharSequence error) {}
        public void onLayoutCancelled() {}
    }

    public static abstract class WriteResultCallback {
        public WriteResultCallback() {}
        public void onWriteFinished(PageRange[] pages) {}
        public void onWriteFailed(CharSequence error) {}
        public void onWriteCancelled() {}
    }
}
