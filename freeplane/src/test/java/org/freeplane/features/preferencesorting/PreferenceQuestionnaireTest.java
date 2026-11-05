package org.freeplane.features.preferencesorting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PreferenceQuestionnaireTest {

	@Test
	public void finishesAfterLastNeededAnswer() {
		final PreferenceQuestionnaire questionnaire = new PreferenceQuestionnaire(2);

		assertThat(questionnaire.questionNumber()).isEqualTo(1);
		assertThat(questionnaire.canGoBack()).isFalse();
		assertThat(questionnaire.answer(0, 10)).isTrue();
		assertThat(questionnaire.sorting().ranking()).containsExactly(0, 1);
	}

	@Test
	public void goesBackToPreviousQuestionWithItsAnswer() {
		final PreferenceQuestionnaire questionnaire = new PreferenceQuestionnaire(3);
		final PreferencePair first = questionnaire.currentQuestion();
		assertThat(questionnaire.answer(first.first, 8)).isFalse();

		assertThat(questionnaire.questionNumber()).isEqualTo(2);
		assertThat(questionnaire.currentScoreOf(questionnaire.currentQuestion().first)).isNull();

		questionnaire.goBack();

		assertThat(questionnaire.currentQuestion()).isEqualTo(first);
		assertThat(questionnaire.currentScoreOf(first.first)).isEqualTo(8);
		assertThat(questionnaire.currentScoreOf(first.second)).isEqualTo(2);
	}

	@Test
	public void showsLaterAnswersAgainAfterGoingBack() {
		final PreferenceQuestionnaire questionnaire = new PreferenceQuestionnaire(3);
		questionnaire.answer(1, 10);
		final PreferencePair second = questionnaire.currentQuestion();
		questionnaire.answer(2, 10);
		questionnaire.goBack();
		questionnaire.goBack();

		assertThat(questionnaire.answer(1, 9)).isFalse();

		assertThat(questionnaire.currentQuestion()).isEqualTo(second);
		assertThat(questionnaire.currentScoreOf(2)).isEqualTo(10);
	}

	@Test
	public void refusesContradictoryAnswerAndKeepsQuestion() {
		final PreferenceQuestionnaire questionnaire = new PreferenceQuestionnaire(3);
		questionnaire.answer(1, 10);
		questionnaire.answer(2, 10);
		assertThat(questionnaire.answer(1, 10)).isTrue();
		questionnaire.goBack();
		questionnaire.goBack();

		assertThat(questionnaire.contradictionOf(0, 10)).containsExactly(1, 2, 0);
		assertThat(questionnaire.questionNumber()).isEqualTo(1);
	}
}
