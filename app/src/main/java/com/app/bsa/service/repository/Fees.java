package com.app.bsa.service.repository;

import com.app.bsa.service.firestore.FeesDao;

public class Fees {

    public String Fee_level;
    public Double Fee_amount;

    public Fees(){}

    public Fees(FeesDao vDao){
        Fee_level = vDao.Fee_level;
        Fee_amount = vDao.Fee_amount;
    }
}
