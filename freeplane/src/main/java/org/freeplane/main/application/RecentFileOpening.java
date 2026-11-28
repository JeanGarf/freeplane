package org.freeplane.main.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

class RecentFileOpening {
    enum Choice {
        RETRY, REMOVE, KEEP;

        static Choice ofOptionIndex(int index) {
            Choice[] choices = values();
            return index >= 0 && index < choices.length ? choices[index] : KEEP;
        }
    }

    interface Opener {
        void open() throws Exception;
    }

    static boolean shouldRemoveFromList(Opener opener, Function<Exception, Choice> failureHandler) {
        while (true) {
            try {
                opener.open();
                return false;
            }
            catch (Exception e) {
                Choice choice = failureHandler.apply(e);
                if (choice != Choice.RETRY) {
                    return choice == Choice.REMOVE;
                }
            }
        }
    }

    interface FileOpener<T> {
        void open(T file) throws Exception;
    }

    static <T> void reopenAll(List<T> files, FileOpener<T> opener, BiFunction<T, Exception, Choice> failureHandler,
                              Consumer<T> onOpened, Consumer<T> onRemoved) {
        final Map<T, Exception> failures = new LinkedHashMap<>();
        List<T> pending = new ArrayList<>(files);
        boolean attempt = true;
        while (true) {
            if (attempt) {
                failures.clear();
                for (T file : pending) {
                    try {
                        opener.open(file);
                        onOpened.accept(file);
                    }
                    catch (Exception e) {
                        failures.put(file, e);
                    }
                }
                pending = new ArrayList<>(failures.keySet());
            }
            if (pending.isEmpty())
                return;
            final T first = pending.remove(0);
            final Choice choice = failureHandler.apply(first, failures.get(first));
            if (choice == Choice.RETRY) {
                pending.add(0, first);
                attempt = true;
            }
            else {
                if (choice == Choice.REMOVE)
                    onRemoved.accept(first);
                attempt = false;
            }
        }
    }
}
