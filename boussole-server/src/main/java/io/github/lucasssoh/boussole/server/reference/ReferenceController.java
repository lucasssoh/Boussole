package io.github.lucasssoh.boussole.server.reference;

import io.github.lucasssoh.boussole.core.reference.PagePreview;
import io.github.lucasssoh.boussole.core.reference.PreviewWebPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The web pages a course cites: may the interface show them beside the text? */
@RestController
class ReferenceController {

    private final PreviewWebPage previewWebPage;

    ReferenceController(PreviewWebPage previewWebPage) {
        this.previewWebPage = previewWebPage;
    }

    @GetMapping("/api/references/preview")
    PagePreviewView preview(@RequestParam String url) {
        return PagePreviewView.of(previewWebPage.preview(url));
    }

    /** {@code title} is null when the page has none, or did not answer. */
    record PagePreviewView(boolean reachable, boolean embeddable, String title) {

        static PagePreviewView of(PagePreview preview) {
            return new PagePreviewView(preview.reachable(), preview.embeddable(), preview.title().orElse(null));
        }
    }
}
