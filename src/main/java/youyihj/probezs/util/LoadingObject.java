package youyihj.probezs.util;

import youyihj.probezs.ProbeZS;

/**
 * @author youyihj
 */
public class LoadingObject<T> {
    private T value;
    private boolean alreadyLoaded;

    private LoadingObject(T value) {
        this.value = value;
    }

    public static <T> LoadingObject<T> of(T value) {
        LoadingObject<T> object = new LoadingObject<>(value);
        ProbeZS.addLoadingObject(object);
        return object;
    }

    public T get() {
        if (alreadyLoaded) {
            throw new IllegalStateException("Only accessible while game loading.");
        }
        return value;
    }

    public void set(T value) {
        if (alreadyLoaded) {
            throw new IllegalStateException("Only accessible while game loading.");
        }
        this.value = value;
    }

    public void setAlreadyLoaded() {
        alreadyLoaded = true;
        this.value = null;
    }
}
