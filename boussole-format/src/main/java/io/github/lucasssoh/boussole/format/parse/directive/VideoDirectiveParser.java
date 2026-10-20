package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.VideoBlock;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/** {@code ::video[caption]{youtube=… start=m:ss end=m:ss}}. */
final class VideoDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("video", DirectiveKind.LEAF, Set.of("youtube"), Set.of("start", "end"), DirectivePlaces.MEDIA);
    private static final Pattern YOUTUBE_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private static final Pattern TIME = Pattern.compile("(?:(?:(\\d+):)?(\\d{1,2}):)?(\\d+)");

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        String youtube = directive.attributes().required("youtube");
        if (!YOUTUBE_ID.matcher(youtube).matches()) {
            throw new InvalidAttributeException("youtube", "must be the 11-character ID of a video, not `" + youtube + "`");
        }
        Optional<Duration> start = time(directive, "start");
        Optional<Duration> end = time(directive, "end");
        if (start.isPresent() && end.isPresent() && end.get().compareTo(start.get()) <= 0) {
            throw new InvalidAttributeException("end", "must come after `start`");
        }
        return Optional.of(new VideoBlock(directive.label(), youtube, start, end, context.position(directive.line())));
    }

    /** Reads {@code 130}, {@code 2:10} or {@code 1:02:10}. */
    private static Optional<Duration> time(CheckedDirective directive, String name) {
        return directive.attributes().text(name).map(value -> {
            var matcher = TIME.matcher(value);
            if (!matcher.matches()) {
                throw new InvalidAttributeException(name, "must be seconds or m:ss, not `" + value + "`");
            }
            long hours = matcher.group(1) == null ? 0 : Long.parseLong(matcher.group(1));
            long minutes = matcher.group(2) == null ? 0 : Long.parseLong(matcher.group(2));
            long seconds = Long.parseLong(matcher.group(3));
            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        });
    }
}
