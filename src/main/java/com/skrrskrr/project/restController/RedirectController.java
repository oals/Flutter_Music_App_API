package com.skrrskrr.project.restController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;

@RestController
public class RedirectController {

    @GetMapping("/redirect/{shareId}/{shareItemId}")
    @ResponseBody
    public String redirectToApp( @PathVariable(required = false) String shareId, @PathVariable(required = false) String shareItemId) {

        // URL을 동적으로 설정
        String redirectUrl = "";
        if (Objects.equals(shareId, "1")) {
            redirectUrl = "myapp://open?playlistId=" + shareItemId;
        } else if (Objects.equals(shareId, "2")) {
            redirectUrl = "myapp://open?trackId=" + shareItemId;
        } else {
            redirectUrl = "https://example.com/no-data"; // 예외 처리: 값이 없을 경우
        }

        System.err.println(redirectUrl);

        // HTML 반환
        return """
            <html>
            <head><meta charset="UTF-8"></head>
            <body>
                <script>
                    setTimeout(function() {
                        window.location.href = '%s';
                    }, 500);
                </script>
            </body>
            </html>
            """.formatted(redirectUrl);
    }

}
