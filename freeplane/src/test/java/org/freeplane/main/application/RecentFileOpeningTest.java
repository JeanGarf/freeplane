package org.freeplane.main.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
}
