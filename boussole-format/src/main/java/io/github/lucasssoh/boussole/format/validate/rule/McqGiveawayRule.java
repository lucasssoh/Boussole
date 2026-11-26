package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;
import java.util.regex.Pattern;

/**
 * P015: the right choice of an MCQ must not stand out from the wrong ones. A learner who sees that
 * only one choice gives a reason, or that one is twice as long as the others, picks it without
 * having understood. Two signs are checked: the right choices are the only ones giving a reason
 * (« : … », « car », « parce que »…), or they are much longer than every wrong one.
 */
public final class McqGiveawayRule implements ValidationRule {

    private static final Pattern MATH = Pattern.compile("\\$([^$]*)\\$");
    private static final Pattern MATH_NOISE = Pattern.compile("\\\\[a-zA-Z]+|[{}^_\\\\ ]");
    private static final Pattern MARKUP = Pattern.compile("[`*]");
    private static final Pattern CODE = Pattern.compile("`[^`]*`");
    private static final Pattern REASON = Pattern.compile(
            ":\\s+\\S"
                    + "|(?<![\\p{L}$])(?:car|donc|because|since|so that|c'est-à-dire)(?!\\p{L})"
                    + "|(?<!\\p{L})(?:parce qu|puisqu)(?:e(?!\\p{L})|['’])"
                    + "|\\s[—–]\\s",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final double LONGER = 1.5;
    private static final int LONGER_BY = 15;

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().blocks()
                .filter(McqBlock.class::isInstance)
                .map(McqBlock.class::cast)
                .forEach(mcq -> check(mcq, diagnostics));
    }

    private static void check(McqBlock mcq, DiagnosticCollector diagnostics) {
        List<String> right = visible(mcq.choices(), true);
        List<String> wrong = visible(mcq.choices(), false);
        if (right.isEmpty() || wrong.isEmpty()) {
            return;
        }
        boolean one = right.size() == 1;
        String which = one ? "the right choice of `" + mcq.id() + "` is" : "the right choices of `" + mcq.id() + "` are";
        List<String> rightWords = words(mcq.choices(), true);
        List<String> wrongWords = words(mcq.choices(), false);
        if (rightWords.stream().allMatch(McqGiveawayRule::givesReason) && wrongWords.stream().noneMatch(McqGiveawayRule::givesReason)) {
            diagnostics.report(DiagnosticCode.P015, mcq.position(), which + (one ? " the only one" : " the only ones")
                    + " to give a reason, which gives the answer away: give each wrong choice the reason a learner"
                    + " would wrongly give, in the same form, or move the reason into the solution");
            return;
        }
        int shortestRight = right.stream().mapToInt(McqGiveawayRule::length).min().orElseThrow();
        int longestWrong = wrong.stream().mapToInt(McqGiveawayRule::length).max().orElseThrow();
        if (shortestRight > LONGER * longestWrong && shortestRight - longestWrong >= LONGER_BY) {
            diagnostics.report(DiagnosticCode.P015, mcq.position(), which + " much longer than the wrong ones ("
                    + shortestRight + " characters against at most " + longestWrong + "), which gives the answer away:"
                    + " shorten it and move the explanation into the solution, or write the wrong choices as fully");
        }
    }

    private static List<String> visible(List<McqChoice> choices, boolean correct) {
        return choices.stream().filter(choice -> choice.correct() == correct).map(choice -> visible(choice.text())).toList();
    }

    /** The words of the choices, without their code, where a colon is syntax rather than a reason. */
    private static List<String> words(List<McqChoice> choices, boolean correct) {
        return choices.stream().filter(choice -> choice.correct() == correct)
                .map(choice -> visible(CODE.matcher(choice.text()).replaceAll(""))).toList();
    }

    /** The text as the learner reads it: a formula counts by its symbols, not by its LaTeX. */
    static String visible(String text) {
        String math = MATH.matcher(text).replaceAll(match -> MATH_NOISE.matcher(match.group(1)).replaceAll(""));
        return MARKUP.matcher(math).replaceAll("").strip();
    }

    private static boolean givesReason(String text) {
        return REASON.matcher(text).find();
    }

    private static int length(String text) {
        return text.codePointCount(0, text.length());
    }
}
