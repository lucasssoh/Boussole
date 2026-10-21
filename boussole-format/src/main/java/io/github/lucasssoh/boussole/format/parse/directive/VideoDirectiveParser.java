package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.SourceReference;
import io.github.lucasssoh.boussole.format.model.block.FileVideoSource;
import io.github.lucasssoh.boussole.format.model.block.VideoBlock;
import io.github.lucasssoh.boussole.format.model.block.VideoSource;
import io.github.lucasssoh.boussole.format.model.block.YoutubeVideoSource;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@code ::video[caption]{youtube=… start=m:ss end=m:ss}}, or {@code src=…} for a video file of
 * the sources folder instead of YouTube.
 */
final class VideoDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("video", DirectiveKind.LEAF, Set.of(), Set.of("youtube", "src", "start", "end"), DirectivePlaces.MEDIA);
    private static final Pattern YOUTUBE_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private static final Pattern TIME = Pattern.compile("(?:(?:(\\d+):)?(\\d{1,2}):)?(\\d+)");

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        VideoSource source = source(directive, context);
        Optional<Duration> start = time(directive, "start");
        Optional<Duration> end = time(directive, "end");
        if (start.isPresent() && end.isPresent() && end.get().compareTo(start.get()) <= 0) {
            throw new InvalidAttributeException("end", "must come after `start`");
        }
        return Optional.of(new VideoBlock(directive.label(), source, start, end, context.position(directive.line())));
    }

    private static VideoSource source(CheckedDirective directive, ParseContext context) {
        Optional<String> youtube = directive.attributes().text("youtube");
        Optional<String> file = directive.attributes().text("src");
        if (youtube.isPresent() == file.isPresent()) {
            throw new InvalidAttributeException("youtube", "or `src` is needed, one of them: `youtube=<video ID>` for YouTube,"
                    + " `src=<file>` for a video of the sources folder");
        }
        if (file.isPresent()) {
            FileVideoSource local = new FileVideoSource(new SourceReference(file.get(), OptionalInt.empty(), context.position(directive.line())));
            if (!local.playable()) {
                context.report(DiagnosticCode.F038, directive.line(), "`" + file.get() + "` may not play in a browser: convert it to "
                        + "MP4 (H.264) or WebM, for example with `ffmpeg -i " + file.get() + " out.mp4`");
            }
            return local;
        }
        if (!YOUTUBE_ID.matcher(youtube.get()).matches()) {
            throw new InvalidAttributeException("youtube", "must be the 11-character ID of a video, not `" + youtube.get() + "`");
        }
        return new YoutubeVideoSource(youtube.get());
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
