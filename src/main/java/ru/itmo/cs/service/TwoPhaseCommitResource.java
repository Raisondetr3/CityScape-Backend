package ru.itmo.cs.service;

public interface TwoPhaseCommitResource {
    void prepare() throws Exception;
    void commit() throws Exception;
    void rollback() throws Exception;
}
