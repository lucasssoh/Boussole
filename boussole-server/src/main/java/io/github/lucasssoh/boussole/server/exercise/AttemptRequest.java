package io.github.lucasssoh.boussole.server.exercise;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.GapsResponse;
import io.github.lucasssoh.boussole.core.learn.check.LearnerResponse;
import io.github.lucasssoh.boussole.core.learn.check.MatchResponse;
import io.github.lucasssoh.boussole.core.learn.check.OrderResponse;
import io.github.lucasssoh.boussole.core.learn.check.SelfGrade;
import io.github.lucasssoh.boussole.core.learn.check.SelfGradeResponse;
import io.github.lucasssoh.boussole.core.learn.check.ValueResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A response sent by the interface, one kind per kind of question. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AttemptRequest.Choices.class, name = "choices"),
    @JsonSubTypes.Type(value = AttemptRequest.Value.class, name = "value"),
    @JsonSubTypes.Type(value = AttemptRequest.Gaps.class, name = "gaps"),
    @JsonSubTypes.Type(value = AttemptRequest.Order.class, name = "order"),
    @JsonSubTypes.Type(value = AttemptRequest.Match.class, name = "match"),
    @JsonSubTypes.Type(value = AttemptRequest.SelfGraded.class, name = "selfGrade")
})
sealed interface AttemptRequest {

    LearnerResponse toResponse();

    record Choices(Set<Integer> picked) implements AttemptRequest {
        @Override
        public LearnerResponse toResponse() {
            return new ChoicesResponse(picked);
        }
    }

    record Value(String value) implements AttemptRequest {
        @Override
        public LearnerResponse toResponse() {
            return new ValueResponse(value == null ? "" : value);
        }
    }

    record Gaps(Map<Integer, String> values) implements AttemptRequest {
        @Override
        public LearnerResponse toResponse() {
            return new GapsResponse(values);
        }
    }

    record Order(List<String> items) implements AttemptRequest {
        @Override
        public LearnerResponse toResponse() {
            return new OrderResponse(items);
        }
    }

    record Match(Map<String, String> pairs) implements AttemptRequest {
        @Override
        public LearnerResponse toResponse() {
            return new MatchResponse(pairs);
        }
    }

    /** {@code grade} is {@code got_it}, {@code partly} or {@code not_yet}. */
    record SelfGraded(String grade) implements AttemptRequest {
        @Override
        public LearnerResponse toResponse() {
            return new SelfGradeResponse(SelfGrade.valueOf(grade.toUpperCase(java.util.Locale.ROOT)));
        }
    }
}
