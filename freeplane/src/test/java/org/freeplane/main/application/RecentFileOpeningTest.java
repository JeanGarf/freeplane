package org.freeplane.main.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.freeplane.main.application.RecentFileOpening.Choice;
import org.junit.Test;

public class RecentFileOpeningTest {
    @Test
    public void successfulOpeningDoesNotAskAnything() {
        AtomicInteger questions = new AtomicInteger();
        boolean remove = RecentFileOpening.shouldRemoveFromList(() -> {
        }, e -> {
            questions.incrementAndGet();
            return Choice.REMOVE;
        });
        assertFalse(remove);
        assertEquals(0, questions.get());
    }

    @Test
    public void removeChoiceRemovesFromList() {
        assertTrue(RecentFileOpening.shouldRemoveFromList(() -> {
            throw new IOException();
        }, e -> Choice.REMOVE));
    }

    @Test
    public void keepChoiceKeepsInList() {
        assertFalse(RecentFileOpening.shouldRemoveFromList(() -> {
            throw new IOException();
        }, e -> Choice.KEEP));
    }

    @Test
    public void retryOpensAgainUntilItSucceeds() {
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger questions = new AtomicInteger();
        boolean remove = RecentFileOpening.shouldRemoveFromList(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IOException();
            }
        }, e -> {
            questions.incrementAndGet();
            return Choice.RETRY;
        });
        assertFalse(remove);
        assertEquals(3, attempts.get());
        assertEquals(2, questions.get());
    }

    @Test
    public void retryThenRemove() {
        AtomicInteger questions = new AtomicInteger();
        boolean remove = RecentFileOpening.shouldRemoveFromList(() -> {
            throw new IOException();
        }, e -> questions.incrementAndGet() < 2 ? Choice.RETRY : Choice.REMOVE);
        assertTrue(remove);
    }

    @Test
    public void optionIndexesMapToChoicesAndClosingKeeps() {
        assertEquals(Choice.RETRY, Choice.ofOptionIndex(0));
        assertEquals(Choice.REMOVE, Choice.ofOptionIndex(1));
        assertEquals(Choice.KEEP, Choice.ofOptionIndex(2));
        assertEquals(Choice.KEEP, Choice.ofOptionIndex(-1));
    }

    private static class Session {
        final Set<String> unavailable = new HashSet<>();
        final List<String> opened = new ArrayList<>();
        final List<String> asked = new ArrayList<>();
        final List<String> removed = new ArrayList<>();

        void reopen(List<String> files, java.util.function.Function<String, Choice> answer) {
            RecentFileOpening.reopenAll(files, file -> {
                if (unavailable.contains(file))
                    throw new IOException(file);
            }, (file, e) -> {
                asked.add(file);
                return answer.apply(file);
            }, opened::add, removed::add);
        }
    }

    @Test
    public void reopenAllOpensEverythingWithoutQuestionWhenAvailable() {
        Session session = new Session();
        session.reopen(Arrays.asList("a", "b", "c"), file -> Choice.KEEP);
        assertEquals(Arrays.asList("a", "b", "c"), session.opened);
        assertEquals(0, session.asked.size());
    }

    @Test
    public void retryReopensAllFilesBecomingAvailableWithoutFurtherQuestions() {
        Session session = new Session();
        session.unavailable.addAll(Arrays.asList("a", "b", "c"));
        session.reopen(Arrays.asList("a", "b", "c"), file -> {
            session.unavailable.clear();
            return Choice.RETRY;
        });
        assertEquals(Arrays.asList("a"), session.asked);
        assertEquals(Arrays.asList("a", "b", "c"), session.opened);
    }

    @Test
    public void questionIsAskedOnlyForFilesStillUnavailableAfterRetry() {
        Session session = new Session();
        session.unavailable.addAll(Arrays.asList("a", "b", "c"));
        session.reopen(Arrays.asList("a", "b", "c"), file -> {
            session.unavailable.removeAll(Arrays.asList("a", "b"));
            return file.equals("a") ? Choice.RETRY : Choice.KEEP;
        });
        assertEquals(Arrays.asList("a", "c"), session.asked);
        assertEquals(Arrays.asList("a", "b"), session.opened);
    }

    @Test
    public void keepOrRemoveDoesNotRetryOtherFiles() {
        Session session = new Session();
        session.unavailable.addAll(Arrays.asList("a", "b"));
        session.reopen(Arrays.asList("a", "b"), file -> file.equals("a") ? Choice.REMOVE : Choice.KEEP);
        assertEquals(Arrays.asList("a", "b"), session.asked);
        assertEquals(Arrays.asList("a"), session.removed);
        assertEquals(0, session.opened.size());
    }
}
