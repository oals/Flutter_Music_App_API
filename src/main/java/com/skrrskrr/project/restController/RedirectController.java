package com.skrrskrr.project.restController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RedirectController {

    @GetMapping("/redirect")
    @ResponseBody
    public String redirectToApp(HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        return """
            <html>
            <head><meta charset="UTF-8"></head>
            <body>
                <a id="openApp" href="myapp://open?playlist_id=50">앱 열기</a>
                <script>
                    setTimeout(function() {
                        window.location.href = 'myapp://open?playlistId=50'; // 앱 실행
                    }, 500);
                </script>
            </body>
            </html>
            """;
    }

}
