package com.estudos.slowquerydetector.interceptor;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class SlowQueryInspector implements StatementInspector {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(SlowQueryInspector.class);

    private static final ThreadLocal<Deque<InspectedStatement>> CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public String inspect(String sql) {
        Deque<InspectedStatement> stack = CONTEXT.get();
        stack.push(new InspectedStatement(sql, System.nanoTime()));
        if (log.isDebugEnabled()) {
            log.debug("Inspected SQL: {}", sql);
        }
        return sql;
    }

    public InspectedStatement peek() {
        return CONTEXT.get().peek();
    }

    public InspectedStatement consume() {
        Deque<InspectedStatement> stack = CONTEXT.get();
        InspectedStatement statement = stack.pollFirst();
        if (stack.isEmpty()) {
            CONTEXT.remove();
        }
        return statement;
    }

    public void clear() {
        CONTEXT.remove();
    }

    public record InspectedStatement(String sql, long startNanos) {
    }
}
