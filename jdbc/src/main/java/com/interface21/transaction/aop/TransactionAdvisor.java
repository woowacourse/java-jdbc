package com.interface21.transaction.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

public class TransactionAdvisor implements PointcutAdvisor {

    private final TransactionPointcut transactionPointcut;
    private final TransactionAdvice transactionAdvice;

    public TransactionAdvisor(final TransactionPointcut transactionPointcut, final TransactionAdvice transactionAdvice) {
        this.transactionPointcut = transactionPointcut;
        this.transactionAdvice = transactionAdvice;
    }

    @Override
    public Pointcut getPointcut() {
        return transactionPointcut;
    }

    @Override
    public Advice getAdvice() {
        return transactionAdvice;
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
}
