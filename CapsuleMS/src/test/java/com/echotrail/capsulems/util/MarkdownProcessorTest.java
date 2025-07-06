package com.echotrail.capsulems.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MarkdownProcessorTest {

    private final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    @Test
    void toHtml() {
        String markdown = "**bold**";
        String expectedHtml = "<p><strong>bold</strong></p>\n";
        String actualHtml = markdownProcessor.toHtml(markdown);
        assertThat(actualHtml).isEqualTo(expectedHtml);
    }
}
