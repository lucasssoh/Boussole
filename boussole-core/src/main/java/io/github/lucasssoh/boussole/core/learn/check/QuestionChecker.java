package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;

/** Checks a response to an MCQ or an exercise with the checker of its kind. */
public final class QuestionChecker {

    private final McqResponseChecker mcq = new McqResponseChecker();
    private final SingleAnswerResponseChecker single = new SingleAnswerResponseChecker();
    private final FillAnswerResponseChecker fill = new FillAnswerResponseChecker();
    private final OrderAnswerResponseChecker order = new OrderAnswerResponseChecker();
    private final MatchAnswerResponseChecker match = new MatchAnswerResponseChecker();
    private final SelfGradeResponseChecker selfGrade = new SelfGradeResponseChecker();

    public CheckResult check(McqBlock question, LearnerResponse response) {
        return mcq.checkResponse(question, response);
    }

    public CheckResult check(ExerciseBlock question, LearnerResponse response) {
        if (question.type() == ExerciseType.FREE) {
            return selfGrade.checkResponse(question, response);
        }
        Answer answer = question.answer()
                .orElseThrow(() -> new ResponseMismatchException("the answer of " + question.id() + " could not be read"));
        return switch (answer) {
            case SingleAnswer expected -> single.checkResponse(expected, response);
            case FillAnswer expected -> fill.checkResponse(expected, response);
            case OrderAnswer expected -> order.checkResponse(expected, response);
            case MatchAnswer expected -> match.checkResponse(expected, response);
        };
    }
}
