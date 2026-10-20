package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.block.Video;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import io.github.lucasssoh.boussole.format.validate.VideoCatalog;

/** F032: each video exists on YouTube. Only checked when a catalog is given, as it needs the network. */
public final class VideoExistsRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        if (context.videos().isEmpty()) {
            return;
        }
        VideoCatalog catalog = context.videos().get();
        context.course().blocks()
                .filter(Video.class::isInstance)
                .map(Video.class::cast)
                .filter(video -> !catalog.exists(video.youtubeId()))
                .forEach(video -> diagnostics.report(Code.F032, video.position(),
                        "YouTube does not know the video `" + video.youtubeId() + "`"));
    }
}
