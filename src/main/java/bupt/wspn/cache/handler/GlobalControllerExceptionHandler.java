package bupt.wspn.cache.handler;

import bupt.wspn.cache.model.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles all otherwise-unhandled exceptions for all controllers.
 * <p>
 * IMPORTANT:
 * You may want to disable this or alter it as it outputs a stack trace to the user, which can pose a security risk.
 * Consider altering this to include limited information on the console screen itself.
 */
@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    private static String ERROR_RETURN_MSG = "Exception occurs. Execution failed.";

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public String defaultExceptionHandler(final HttpServletRequest request, final Exception e) {
        final String msg = String.format("Exception occurs for uri:%s", request.getRequestURI());
        log.error(msg, e);
        return ResponseEntity.retryableFailEntity(ERROR_RETURN_MSG).toJSONString();
    }
}
