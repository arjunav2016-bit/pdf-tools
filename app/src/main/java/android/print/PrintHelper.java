package android.print;

public class PrintHelper {
    public interface LayoutCallback {
        void onLayoutFinished(PrintDocumentInfo info, boolean changed);
        void onLayoutFailed(CharSequence error);
    }

    public interface WriteCallback {
        void onWriteFinished(PageRange[] pages);
        void onWriteFailed(CharSequence error);
    }

    public static PrintDocumentAdapter.LayoutResultCallback createLayoutCallback(final LayoutCallback callback) {
        return new PrintDocumentAdapter.LayoutResultCallback() {
            @Override
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                callback.onLayoutFinished(info, changed);
            }

            @Override
            public void onLayoutFailed(CharSequence error) {
                callback.onLayoutFailed(error);
            }
        };
    }

    public static PrintDocumentAdapter.WriteResultCallback createWriteCallback(final WriteCallback callback) {
        return new PrintDocumentAdapter.WriteResultCallback() {
            @Override
            public void onWriteFinished(PageRange[] pages) {
                callback.onWriteFinished(pages);
            }

            @Override
            public void onWriteFailed(CharSequence error) {
                callback.onWriteFailed(error);
            }
        };
    }
}
