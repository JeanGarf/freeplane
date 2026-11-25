package org.freeplane.main.application;

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
}
