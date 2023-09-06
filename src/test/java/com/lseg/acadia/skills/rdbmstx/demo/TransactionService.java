package com.lseg.acadia.skills.rdbmstx.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    @Transactional
    public void insert() {

    }
}
