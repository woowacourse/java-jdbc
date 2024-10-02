package com.interface21.webmvc.servlet.support;

import com.interface21.webmvc.servlet.ModelAndView;
import java.util.List;

public class ReturnValueAdapterRegistry {

    private final List<ReturnValueAdapter> returnValueAdapters = List.of(
            new ModelAndViewReturnValueAdapter(),
            new JspViewNameReturnValueAdapter()
    );

    public ModelAndView adapt(Object returnValue) {
        return returnValueAdapters.stream()
                .filter(adapter -> adapter.supports(returnValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported return value: " + returnValue))
                .adapt(returnValue);
    }
}
